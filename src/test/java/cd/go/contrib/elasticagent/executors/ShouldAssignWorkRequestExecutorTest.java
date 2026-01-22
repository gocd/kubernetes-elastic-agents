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

import cd.go.contrib.elasticagent.*;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagent.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class ShouldAssignWorkRequestExecutorTest extends BaseTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KubernetesClientFactory factory;

    private AgentInstances<KubernetesInstance> agentInstances;
    private KubernetesInstance instance;
    private Map<String, String> instanceElasticProperties;
    private ClusterProfileProperties instanceClusterProps;

    @Mock
    private KubernetesClient mockedClient;

    @Mock
    private ConsoleLogAppender consoleLogAppender;

    @Mock
    private PluginRequest pluginRequest;

    @Mock
    private MixedOperation<Pod, PodList, PodResource> mockedOperation;

    @Mock
    private PodResource mockedPodResource;

    private String environment = "QA";

    @BeforeEach
    public void setUp() {
        openMocks(this);
        when(factory.client(any()).get()).thenReturn(mockedClient);
        when(mockedClient.pods()).thenReturn(mockedOperation);

        final PodList podList = mock(PodList.class);
        when(mockedOperation.list()).thenReturn(podList);
        when(podList.getItems()).thenReturn(Collections.emptyList());

        when(mockedOperation.resource(any(Pod.class))).thenAnswer((Answer<PodResource>) invocation -> {
            Object[] args = invocation.getArguments();
            Pod pod = (Pod) args[0];

            when(mockedPodResource.create()).thenReturn(pod);

            return mockedPodResource;
        });

        agentInstances = new KubernetesAgentInstances(factory);
        instanceElasticProperties = Map.of("foo", "bar", "Image", "gocdcontrib/ubuntu-docker-elastic-agent");
        instanceClusterProps = createClusterProfileProperties();
        instance = agentInstances.requestCreateAgent(new CreateAgentRequest(UUID.randomUUID().toString(), instanceElasticProperties, environment, new JobIdentifier(100L)), instanceClusterProps, pluginRequest, consoleLogAppender).get();
    }

    @Test
    public void withAgentReuseDisabledShouldAssignWorkWhenJobIdMatchesPodId() {
        Long jobId = 100L;
        assertThat(jobId).isEqualTo(instance.getJobId());
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setEnableAgentReuse(false);
        JobIdentifier jobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(
                new Agent(instance.getPodName(), null, null, null),
                environment,
                instanceElasticProperties,
                jobIdentifier,
                clusterProfileProperties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("true");
    }

    @Test
    public void withAgentReuseDisabledShouldNotAssignWorkWhenJobIdDoesNotMatchPodId() {
        Long jobId = 333L;
        assertThat(jobId).isNotEqualTo(instance.getJobId());
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setEnableAgentReuse(false);
        JobIdentifier jobIdentifier = new JobIdentifier(
                "test-pipeline",
                1L,
                "Test Pipeline",
                "test-stage",
                "1",
                "test-job",
                jobId);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(
                new Agent(instance.getPodName(), null, null, null),
                environment,
                instanceElasticProperties,
                jobIdentifier,
                clusterProfileProperties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("false");
    }

    @Test
    public void withAgentReuseEnabledShouldAssignWorkWhenElasticInfoMatches() {
        Long jobId = 333L;
        assertThat(jobId).isNotEqualTo(instance.getJobId());
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setEnableAgentReuse(true);
        JobIdentifier jobIdentifier = new JobIdentifier(
                "test-pipeline",
                1L,
                "Test Pipeline",
                "test-stage",
                "1",
                "test-job",
                jobId);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(
                new Agent(instance.getPodName(), null, null, null),
                environment,
                instanceElasticProperties,
                jobIdentifier,
                clusterProfileProperties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("true");
    }

    @Test
    public void withAgentReuseEnabledShouldNotAssignWorkWhenElasticInfoDoesNotMatch() {
        Long jobId = 333L;
        assertThat(jobId).isNotEqualTo(instance.getJobId());

        ClusterProfileProperties requestClusterProps = new ClusterProfileProperties("http://foo:8154/go", null, null);
        requestClusterProps.setEnableAgentReuse(true);
        assertThat(requestClusterProps).isNotEqualTo(instanceClusterProps);

        Map<String, String> requestElasticProperties = Map.of("something", "different");
        assertThat(requestElasticProperties).isNotEqualTo(instanceElasticProperties);

        JobIdentifier jobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", jobId);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.getPodName(), null, null, null), environment, requestElasticProperties, jobIdentifier, requestClusterProps);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("false");
    }
}
