/*
 * Copyright 2018 ThoughtWorks, Inc.
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
import cd.go.contrib.elasticagent.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

public class JobCompletionRequestExecutorTest {

    @Mock
    private PluginRequest mockPluginRequest;
    @Mock
    private AgentInstances<KubernetesInstance> mockAgentInstances;

    @Captor
    private ArgumentCaptor<List<Agent>> agentsArgumentCaptor;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void shouldTerminateElasticAgentOnJobCompletion() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier(100L);
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        String elasticAgentId = "agent-1";
        JobCompletionRequest request = new JobCompletionRequest(elasticAgentId, jobIdentifier, new HashMap<>(), clusterProfileProperties);
        JobCompletionRequestExecutor executor = new JobCompletionRequestExecutor(request, mockAgentInstances, mockPluginRequest);

        GoPluginApiResponse response = executor.execute();

        InOrder inOrder = inOrder(mockPluginRequest, mockAgentInstances);

        inOrder.verify(mockPluginRequest).disableAgents(agentsArgumentCaptor.capture());
        List<Agent> agentsToDisabled = agentsArgumentCaptor.getValue();
        assertEquals(1, agentsToDisabled.size());
        assertEquals(elasticAgentId, agentsToDisabled.get(0).elasticAgentId());
        inOrder.verify(mockAgentInstances).terminate(elasticAgentId, clusterProfileProperties);
        inOrder.verify(mockPluginRequest).deleteAgents(agentsArgumentCaptor.capture());
        List<Agent> agentsToDelete = agentsArgumentCaptor.getValue();

        assertEquals(agentsToDisabled, agentsToDelete);

        assertEquals(200, response.responseCode());
        assertTrue(response.responseBody().isEmpty());

        verifyNoMoreInteractions(mockPluginRequest);
    }
}
