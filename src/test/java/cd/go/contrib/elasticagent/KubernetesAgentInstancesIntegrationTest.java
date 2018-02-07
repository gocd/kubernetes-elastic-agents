/*
 * Copyright 2017 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagent;

import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.internal.PodOperationsImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PRIVILEGED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class KubernetesAgentInstancesIntegrationTest {

    @Mock
    private KubernetesClientFactory mockedKubernetesClientFactory;

    @Mock
    private PluginRequest mockedPluginRequest;

    private KubernetesAgentInstances kubernetesAgentInstances;
    private PluginSettings settings;
    private CreateAgentRequest createAgentRequest;

    @Mock
    private KubernetesClient mockKubernetesClient;

    @Mock
    private PodOperationsImpl pods;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        kubernetesAgentInstances = new KubernetesAgentInstances(mockedKubernetesClientFactory);
        when(mockedKubernetesClientFactory.kubernetes(any())).thenReturn(mockKubernetesClient);

        when(pods.inNamespace(Constants.KUBERNETES_NAMESPACE)).thenReturn(pods);

        when(pods.create(any())).thenAnswer((Answer<Pod>) invocation -> {
            Object[] args = invocation.getArguments();
            return (Pod) args[0];
        });

        when(pods.list()).thenReturn(new PodList());
        when(mockKubernetesClient.pods()).thenReturn(pods);

        createAgentRequest = CreateAgentRequestMother.defaultCreateAgentRequest();
        settings = PluginSettingsMother.defaultPluginSettings();
    }

    @Test
    public void shouldCreateKubernetesPodForCreateAgentRequest() throws Exception {
        KubernetesInstance kubernetesInstance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);

        assertTrue(kubernetesAgentInstances.instanceExists(kubernetesInstance));
    }

    @Test
    public void shouldCreateKubernetesPodWithContainerSpecification() throws Exception {
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        KubernetesInstance instance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        List<Container> containers = elasticAgentPod.getSpec().getContainers();
        assertThat(containers.size(), is(1));

        Container gocdAgentContainer = containers.get(0);

        assertThat(gocdAgentContainer.getName(), is(instance.name()));

        assertThat(gocdAgentContainer.getImage(), is("gocd/custom-gocd-agent-alpine:latest"));
        assertThat(gocdAgentContainer.getImagePullPolicy(), is("IfNotPresent"));
        assertThat(gocdAgentContainer.getSecurityContext().getPrivileged(), is(false));
    }

    @Test
    public void shouldCreateKubernetesPodWithPrivilegedMod() throws Exception {
        createAgentRequest.properties().put(PRIVILEGED.getKey(), "true");
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        KubernetesInstance instance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        List<Container> containers = elasticAgentPod.getSpec().getContainers();
        assertThat(containers.size(), is(1));

        Container gocdAgentContainer = containers.get(0);

        assertThat(gocdAgentContainer.getName(), is(instance.name()));
        assertThat(gocdAgentContainer.getSecurityContext().getPrivileged(), is(true));
    }

    @Test
    public void shouldCreateKubernetesPodWithResourcesLimitSpecificationOnGoCDAgentContainer() throws Exception {
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        List<Container> containers = elasticAgentPod.getSpec().getContainers();
        assertThat(containers.size(), is(1));

        Container gocdAgentContainer = containers.get(0);

        ResourceRequirements resources = gocdAgentContainer.getResources();

        assertThat(resources.getLimits().get("memory").getAmount(), is(String.valueOf(1024 * 1024 * 1024)));
        assertThat(resources.getLimits().get("cpu").getAmount(), is("2"));
    }

    @Test
    public void shouldCreateKubernetesPodWithPodMetadata() throws Exception {
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        KubernetesInstance instance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());

        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());
        assertThat(elasticAgentPod.getMetadata().getName(), is(instance.name()));
    }

    @Test
    public void shouldCreateKubernetesPodWithTimeStamp() throws Exception {
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());

        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());
        assertNotNull(elasticAgentPod.getMetadata().getCreationTimestamp());
    }

    @Test
    public void shouldCreateKubernetesPodWithGoCDElasticAgentContainerContainingEnvironmentVariables() throws Exception {
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        KubernetesInstance instance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        ArrayList<EnvVar> expectedEnvVars = new ArrayList<>();
        expectedEnvVars.add(new EnvVar("GO_EA_SERVER_URL", settings.getGoServerUrl(), null));

        expectedEnvVars.add(new EnvVar("ENV1", "VALUE1", null));
        expectedEnvVars.add(new EnvVar("ENV2", "VALUE2", null));

        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_KEY", createAgentRequest.autoRegisterKey(), null));
        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_ENVIRONMENT", createAgentRequest.environment(), null));
        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID", instance.name(), null));
        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID", Constants.PLUGIN_ID, null));

        List<Container> containers = elasticAgentPod.getSpec().getContainers();
        assertThat(containers.size(), is(1));

        assertThat(containers.get(0).getEnv(), is(expectedEnvVars));
    }

    @Test
    public void shouldCreateKubernetesPodWithPodAnnotations() throws Exception {
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());

        Map<String, String> expectedAnnotations = new HashMap<>();
        expectedAnnotations.putAll(createAgentRequest.properties());
        expectedAnnotations.put(Constants.JOB_IDENTIFIER_LABEL_KEY, new Gson().toJson(createAgentRequest.jobIdentifier()));
        assertThat(elasticAgentPod.getMetadata().getAnnotations(), is(expectedAnnotations));
    }

    @Test
    public void shouldCreateKubernetesPodWithPodLabels() throws Exception {
        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());

        HashMap<String, String> labels = new HashMap<>();
        labels.put(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        labels.put(Constants.JOB_ID_LABEL_KEY, createAgentRequest.jobIdentifier().getJobId().toString());
        labels.put(Constants.KUBERNETES_POD_KIND_LABEL_KEY, Constants.KUBERNETES_POD_KIND_LABEL_VALUE);
        labels.put(Constants.ENVIRONMENT_LABEL_KEY, createAgentRequest.environment());

        assertThat(elasticAgentPod.getMetadata().getLabels(), is(labels));
    }

    //Tests Using Pod Yaml

    @Test
    public void usingPodYamlConfigurations_shouldCreateKubernetesPodForCreateAgentRequest() throws Exception {
        createAgentRequest = CreateAgentRequestMother.createAgentRequestUsingPodYaml();
        KubernetesInstance kubernetesInstance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);

        assertTrue(kubernetesAgentInstances.instanceExists(kubernetesInstance));
    }

    @Test
    public void usingPodYamlConfigurations_shouldCreateKubernetesPodWithContainerSpecification() throws Exception {
        createAgentRequest = CreateAgentRequestMother.createAgentRequestUsingPodYaml();

        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        List<Container> containers = elasticAgentPod.getSpec().getContainers();
        assertThat(containers.size(), is(1));

        Container gocdAgentContainer = containers.get(0);

        assertThat(gocdAgentContainer.getName(), is("gocd-agent-container"));
        assertThat(gocdAgentContainer.getImage(), is("gocd/gocd-agent-alpine-3.5:v17.12.0"));
        assertThat(gocdAgentContainer.getImagePullPolicy(), is("Always"));
    }

    @Test
    public void usingPodYamlConfigurations_shouldCreateKubernetesPodWithPodMetadata() throws Exception {
        createAgentRequest = CreateAgentRequestMother.createAgentRequestUsingPodYaml();

        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        KubernetesInstance instance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());

        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());
        assertThat(elasticAgentPod.getMetadata().getName(), is("test-pod-yaml"));

        assertThat(elasticAgentPod.getMetadata().getName(), is(instance.name()));
    }

    @Test
    public void usingPodYamlConfigurations_shouldCreateKubernetesPodWithTimestamp() throws Exception {
        createAgentRequest = CreateAgentRequestMother.createAgentRequestUsingPodYaml();

        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());

        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());
        assertNotNull(elasticAgentPod.getMetadata().getCreationTimestamp());
    }

    @Test
    public void usingPodYamlConfigurations_shouldCreateKubernetesPodWithGoCDElasticAgentContainerContainingEnvironmentVariables() throws Exception {
        createAgentRequest = CreateAgentRequestMother.createAgentRequestUsingPodYaml();

        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        KubernetesInstance instance = kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        ArrayList<EnvVar> expectedEnvVars = new ArrayList<>();
        expectedEnvVars.add(new EnvVar("DEMO_ENV", "DEMO_FANCY_VALUE", null));

        expectedEnvVars.add(new EnvVar("GO_EA_SERVER_URL", settings.getGoServerUrl(), null));
        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_KEY", createAgentRequest.autoRegisterKey(), null));
        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_ENVIRONMENT", createAgentRequest.environment(), null));
        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID", instance.name(), null));
        expectedEnvVars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID", Constants.PLUGIN_ID, null));

        List<Container> containers = elasticAgentPod.getSpec().getContainers();
        assertThat(containers.size(), is(1));

        assertThat(containers.get(0).getEnv(), is(expectedEnvVars));
    }

    @Test
    public void usingPodYamlConfigurations_shouldCreateKubernetesPodWithPodAnnotations() throws Exception {
        createAgentRequest = CreateAgentRequestMother.createAgentRequestUsingPodYaml();

        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());

        HashMap<String, String> expectedAnnotations = new HashMap<>();
        expectedAnnotations.putAll(createAgentRequest.properties());
        expectedAnnotations.put("annotation-key", "my-fancy-annotation-value");
        expectedAnnotations.put(Constants.JOB_IDENTIFIER_LABEL_KEY, new Gson().toJson(createAgentRequest.jobIdentifier()));

        assertThat(elasticAgentPod.getMetadata().getAnnotations(), is(expectedAnnotations));
    }

    @Test
    public void usingPodYamlConfigurations_shouldCreateKubernetesPodWithPodLabels() throws Exception {
        createAgentRequest = CreateAgentRequestMother.createAgentRequestUsingPodYaml();

        ArgumentCaptor<Pod> argumentCaptor = ArgumentCaptor.forClass(Pod.class);
        kubernetesAgentInstances.create(createAgentRequest, settings, mockedPluginRequest);
        verify(pods).create(argumentCaptor.capture());
        Pod elasticAgentPod = argumentCaptor.getValue();

        assertNotNull(elasticAgentPod.getMetadata());

        HashMap<String, String> labels = new HashMap<>();
        labels.put(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        labels.put(Constants.JOB_ID_LABEL_KEY, createAgentRequest.jobIdentifier().getJobId().toString());
        labels.put(Constants.KUBERNETES_POD_KIND_LABEL_KEY, Constants.KUBERNETES_POD_KIND_LABEL_VALUE);
        labels.put(Constants.ENVIRONMENT_LABEL_KEY, createAgentRequest.environment());

        labels.put("app", "gocd-agent");

        assertThat(elasticAgentPod.getMetadata().getLabels(), is(labels));
    }

}
