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

package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.*;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagent.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ShouldAssignWorkRequestExecutorTest extends BaseTest {
    @Mock
    KubernetesClientFactory factory;
    private AgentInstances<KubernetesInstance> agentInstances;
    private KubernetesInstance instance;
    private Map<String, String> properties = new HashMap<>();
    @Mock
    private KubernetesClient mockedClient;
    @Mock
    private MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> mockedOperation;
    private String environment = "QA";

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(factory.client(any(PluginSettings.class))).thenReturn(mockedClient);
        when(mockedClient.pods()).thenReturn(mockedOperation);
        when(mockedOperation.create(any(Pod.class))).thenAnswer(new Answer<Pod>() {
            @Override
            public Pod answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Pod) args[0];
            }
        });

        agentInstances = new KubernetesAgentInstances(factory);
        properties.put("foo", "bar");
        properties.put("Image", "gocdcontrib/ubuntu-docker-elastic-agent");
        instance = agentInstances.create(new CreateAgentRequest(UUID.randomUUID().toString(), properties, environment, new JobIdentifier(100L)), createSettings(), null);
    }

    @Test
    public void shouldAssignWorkWhenJobIdMatchesPodId() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), environment, properties, jobIdentifier);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("true"));
    }

    @Test
    public void shouldNotAssignWorkWhenJobIdDiffersFromPodId() throws Exception {
        long mismatchingJobId = 200L;
        JobIdentifier jobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", mismatchingJobId);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), "FooEnv", properties, jobIdentifier);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }
}
