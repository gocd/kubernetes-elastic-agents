package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.PluginSettings;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.model.reports.agent.KubernetesElasticAgent;
import cd.go.contrib.elasticagent.model.reports.agent.StatusReportGenerationError;
import cd.go.contrib.elasticagent.requests.AgentStatusReportRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static cd.go.contrib.elasticagent.Constants.JOB_IDENTIFIER_LABEL_KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AgentStatusReportExecutorTest {

    private Pod pod;
    private AgentStatusReportExecutor executor;
    private String elasticAgentId = "elastic-agent-id";

    @Mock
    private AgentStatusReportRequest statusReportRequest;

    @Mock
    private PluginRequest pluginRequest;

    @Mock
    private KubernetesClientFactory kubernetesClientFactory;

    @Mock
    private KubernetesClient client;

    @Mock
    private PluginStatusReportViewBuilder builder;

    @Mock
    private MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> mockedOperation;

    @Mock
    private NonNamespaceOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> mockedNamespaceOperation;

    @Mock
    private PodList podList;

    @Mock
    private MixedOperation<Event, EventList, DoneableEvent, Resource<Event, DoneableEvent>> events;

    @Mock
    private NonNamespaceOperation<Event, EventList, DoneableEvent, Resource<Event, DoneableEvent>> eventspace;

    @Mock
    private EventList eventsList;

    @Mock
    private PodResource<Pod, DoneablePod> podresource;

    @Mock
    private Template template;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        pod = createDefaultPod();
        pod.getMetadata().setName(elasticAgentId);
        executor = new AgentStatusReportExecutor(statusReportRequest, pluginRequest, kubernetesClientFactory, builder);

        when(client.pods()).thenReturn(mockedOperation);
        when(mockedOperation.inNamespace(Constants.KUBERNETES_NAMESPACE)).thenReturn(mockedNamespaceOperation);
        when(mockedNamespaceOperation.list()).thenReturn(podList);
        when(podList.getItems()).thenReturn(Arrays.asList(pod));

        when(mockedNamespaceOperation.withName(elasticAgentId)).thenReturn(podresource);
        when(podresource.getLog()).thenReturn("agent-logs");

        when(client.events()).thenReturn(events);
        when(events.inAnyNamespace()).thenReturn(eventspace);
        when(eventspace.list()).thenReturn(eventsList);
        when(eventsList.getItems()).thenReturn(new ArrayList<>());
    }

    @Test
    public void shouldReturnAgentStatusReportBasedOnProvidedElasticAgentId() throws Exception {
        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn(elasticAgentId);

        PluginSettings pluginSettings = new PluginSettings();

        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(kubernetesClientFactory.client(pluginSettings)).thenReturn(client);

        when(builder.getTemplate("agent-status-report.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(KubernetesElasticAgent.class))).thenReturn("my-view");

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("{\"view\":\"my-view\"}"));
    }

    @Test
    public void shouldReturnErrorWhenPodForSpecifiedElasticAgentIdNotFound() throws Exception {
        when(podList.getItems()).thenReturn(new ArrayList<>()); // no matching pod for the specified elastic agent id

        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn(elasticAgentId);

        PluginSettings pluginSettings = new PluginSettings();

        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(kubernetesClientFactory.client(pluginSettings)).thenReturn(client);

        when(builder.getTemplate("error.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(StatusReportGenerationError.class))).thenReturn("my-error-view");

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("{\"view\":\"my-error-view\"}"));
    }

    @Test
    public void shouldReturnErrorWhenPodForSpecifiedJobIdentifierNotFound() throws Exception {
        when(client.pods()).thenThrow(new RuntimeException("Boom!")); //can not find pod for specified job identitier

        JobIdentifier jobIdentifier = new JobIdentifier("up42", 1L, "1", "up42_stage", "1", "job_name", 1L);
        when(statusReportRequest.getJobIdentifier()).thenReturn(jobIdentifier);
        when(statusReportRequest.getElasticAgentId()).thenReturn(null);

        PluginSettings pluginSettings = new PluginSettings();

        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(kubernetesClientFactory.client(pluginSettings)).thenReturn(client);

        when(builder.getTemplate("error.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(StatusReportGenerationError.class))).thenReturn("my-error-view");

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("{\"view\":\"my-error-view\"}"));
    }

    private Pod createDefaultPod() {
        Pod pod = new Pod();
        pod.setMetadata(new ObjectMeta());
        PodSpec spec = new PodSpec();
        spec.setContainers(Arrays.asList(new Container()));
        pod.setSpec(spec);
        PodStatus status = new PodStatus();
        status.setContainerStatuses(Arrays.asList(new ContainerStatus()));
        pod.setStatus(status);
        pod.getMetadata().setAnnotations(Collections.singletonMap(JOB_IDENTIFIER_LABEL_KEY, "{}"));
        return pod;
    }

}
