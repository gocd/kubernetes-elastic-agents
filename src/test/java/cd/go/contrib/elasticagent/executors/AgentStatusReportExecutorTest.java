/*
 * Copyright 2022 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.model.reports.agent.KubernetesElasticAgent;
import cd.go.contrib.elasticagent.reports.StatusReportGenerationError;
import cd.go.contrib.elasticagent.requests.AgentStatusReportRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static cd.go.contrib.elasticagent.Constants.JOB_IDENTIFIER_LABEL_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class AgentStatusReportExecutorTest {

    private AgentStatusReportExecutor executor;
    private String elasticAgentId = "elastic-agent-id";

    @Mock
    private AgentStatusReportRequest statusReportRequest;

    @Mock
    private PluginRequest pluginRequest;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KubernetesClientFactory kubernetesClientFactory;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KubernetesClient client;

    @Mock
    private PluginStatusReportViewBuilder builder;

    @Mock
    private MixedOperation<Pod, PodList, PodResource> mockedOperation;

    @Mock
    private PodList podList;

    @Mock
    private MixedOperation<Event, EventList, Resource<Event>> events;

    @Mock
    private NonNamespaceOperation<Event, EventList, Resource<Event>> eventspace;

    @Mock
    private EventList eventsList;

    @Mock
    private PodResource podresource;

    @Mock
    private PodResource containerResource;

    @Mock
    private Template template;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        Pod pod = createDefaultPod();
        pod.getMetadata().setName(elasticAgentId);
        executor = new AgentStatusReportExecutor(statusReportRequest, kubernetesClientFactory, builder);

        when(client.pods()).thenReturn(mockedOperation);
        when(mockedOperation.list()).thenReturn(podList);
        when(podList.getItems()).thenReturn(Arrays.asList(pod));

        when(mockedOperation.withName(elasticAgentId)).thenReturn(podresource);
        when(podresource.inContainer("test-container")).thenReturn(containerResource);

        when(containerResource.getLog()).thenReturn("agent-logs");

        when(client.v1().events()).thenReturn(events);
        when(events.inAnyNamespace()).thenReturn(eventspace);
        when(eventspace.list()).thenReturn(eventsList);
        when(eventsList.getItems()).thenReturn(new ArrayList<>());
    }

    @Test
    public void shouldReturnAgentStatusReportBasedOnProvidedElasticAgentId() throws Exception {
        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn(elasticAgentId);

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();

        when(statusReportRequest.clusterProfileProperties()).thenReturn(clusterProfileProperties);
        when(kubernetesClientFactory.client(clusterProfileProperties).get()).thenReturn(client);

        when(builder.getTemplate("agent-status-report.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(KubernetesElasticAgent.class))).thenReturn("my-view");

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"view\":\"my-view\"}");
    }

    @Test
    public void shouldReturnErrorWhenPodForSpecifiedElasticAgentIdNotFound() throws Exception {
        when(podList.getItems()).thenReturn(new ArrayList<>()); // no matching pod for the specified elastic agent id

        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn(elasticAgentId);

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();

        when(statusReportRequest.clusterProfileProperties()).thenReturn(clusterProfileProperties);
        when(kubernetesClientFactory.client(clusterProfileProperties).get()).thenReturn(client);

        when(builder.getTemplate("error.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(StatusReportGenerationError.class))).thenReturn("my-error-view");

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"view\":\"my-error-view\"}");
    }

    @Test
    public void shouldReturnErrorWhenPodForSpecifiedJobIdentifierNotFound() throws Exception {
        when(client.pods()).thenThrow(new RuntimeException("Boom!")); //can not find pod for specified job identitier

        JobIdentifier jobIdentifier = new JobIdentifier("up42", 1L, "1", "up42_stage", "1", "job_name", 1L);
        when(statusReportRequest.getJobIdentifier()).thenReturn(jobIdentifier);
        when(statusReportRequest.getElasticAgentId()).thenReturn(null);

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();

        when(statusReportRequest.clusterProfileProperties()).thenReturn(clusterProfileProperties);
        when(kubernetesClientFactory.client(clusterProfileProperties).get()).thenReturn(client);

        when(builder.getTemplate("error.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(StatusReportGenerationError.class))).thenReturn("my-error-view");

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"view\":\"my-error-view\"}");
    }

    private Pod createDefaultPod() {
        Pod pod = new Pod();
        pod.setMetadata(new ObjectMeta());
        PodSpec spec = new PodSpec();
        spec.setContainers(Arrays.asList(aContainer()));
        pod.setSpec(spec);
        PodStatus status = new PodStatus();
        status.setContainerStatuses(Arrays.asList(aContainerStatus()));
        pod.setStatus(status);
        pod.getMetadata().setAnnotations(Collections.singletonMap(JOB_IDENTIFIER_LABEL_KEY, "{}"));
        return pod;
    }

    private Container aContainer() {
        Container container = new Container();
        container.setName("test-container");
        return container;
    }

    private ContainerStatus aContainerStatus() {
        ContainerStatus containerStatus = new ContainerStatus();
        containerStatus.setName("test-container");
        return containerStatus;
    }

}
