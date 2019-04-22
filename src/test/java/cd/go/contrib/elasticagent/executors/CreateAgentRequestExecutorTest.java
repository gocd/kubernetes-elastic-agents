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

import cd.go.contrib.elasticagent.AgentInstances;
import cd.go.contrib.elasticagent.KubernetesAgentInstances;
import cd.go.contrib.elasticagent.KubernetesInstance;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CreateAgentRequestExecutorTest {
    @Test
    public void shouldAskDockerContainersToCreateAnAgent() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest();
        AgentInstances<KubernetesInstance> agentInstances = mock(KubernetesAgentInstances.class);
        PluginRequest pluginRequest = mock(PluginRequest.class);
        new CreateAgentRequestExecutor(request, agentInstances, pluginRequest).execute();

        verifyNoMoreInteractions(pluginRequest);
        verify(agentInstances).create(request, request.clusterProfileProperties(), pluginRequest);
    }
}
