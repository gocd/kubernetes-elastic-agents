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
import cd.go.contrib.elasticagent.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JobCompletionRequestExecutorTest {

    @Test
    public void withAgentReuseDisabledShouldTerminateAgent() throws Exception {
        String elasticAgentId = "agent-1";
        JobIdentifier jobIdentifier = new JobIdentifier(100L);

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setEnableAgentReuse(false);

        Agent agent = new Agent();
        agent.setElasticAgentId(elasticAgentId);
        List<Agent> agents = List.of(agent);

        KubernetesAgentInstances agentInstances = mock(KubernetesAgentInstances.class);
        PluginRequest pluginRequest = mock(PluginRequest.class);
        JobCompletionRequest request = new JobCompletionRequest(elasticAgentId, jobIdentifier, Collections.emptyMap(), clusterProfileProperties);
        JobCompletionRequestExecutor executor = new JobCompletionRequestExecutor(request, agentInstances, pluginRequest);
        GoPluginApiResponse response = executor.execute();

        verify(pluginRequest, times(1)).disableAgents(agents);
        verify(pluginRequest, times(1)).deleteAgents(agents);
        verify(agentInstances, times(1)).terminate(elasticAgentId, clusterProfileProperties);
        assertEquals(200, response.responseCode());
        assertTrue(response.responseBody().isEmpty());
    }

    @Test
    public void withAgentReuseEnabledShouldMarkInstanceIdle() throws Exception {
        String elasticAgentId = "agent-1";
        JobIdentifier jobIdentifier = new JobIdentifier(100L);

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setEnableAgentReuse(true);

        JobCompletionRequest request = new JobCompletionRequest(elasticAgentId, jobIdentifier, Collections.emptyMap(), clusterProfileProperties);

        KubernetesInstance instance = KubernetesInstance.builder().agentState(KubernetesInstance.AgentState.Building).build();
        KubernetesAgentInstances instances = new KubernetesAgentInstances(
                mock(KubernetesClientFactory.class),
                mock(KubernetesInstanceFactory.class),
                Map.of(elasticAgentId, instance));
        assertEquals(instances.find(elasticAgentId).getAgentState(), KubernetesInstance.AgentState.Building);
        PluginRequest pluginRequest = mock(PluginRequest.class);
        JobCompletionRequestExecutor executor = new JobCompletionRequestExecutor(request, instances, pluginRequest);
        GoPluginApiResponse response = executor.execute();

        assertEquals(instances.find(elasticAgentId).getAgentState(), KubernetesInstance.AgentState.Idle);
        assertEquals(200, response.responseCode());
        assertTrue(response.responseBody().isEmpty());
    }
}
