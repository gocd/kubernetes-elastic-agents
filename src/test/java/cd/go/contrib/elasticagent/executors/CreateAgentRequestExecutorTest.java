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
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class CreateAgentRequestExecutorTest {
    @Test
    public void shouldAskDockerContainersToCreateAnAgent() throws Exception {
        final HashMap<String, String> elasticAgentProfileProperties = new HashMap<>();
        elasticAgentProfileProperties.put("Image", "image1");
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties("http://go-server", "http://k8ssvc.url", "");
        final JobIdentifier jobIdentifier = new JobIdentifier("p1", 1L, "l1", "s1", "1", "j1", 1L);
        CreateAgentRequest request = new CreateAgentRequest("key1", elasticAgentProfileProperties, "env1", jobIdentifier, clusterProfileProperties);
        AgentInstances<KubernetesInstance> agentInstances = mock(KubernetesAgentInstances.class);
        PluginRequest pluginRequest = mock(PluginRequest.class);
        new CreateAgentRequestExecutor(request, agentInstances, pluginRequest).execute();

        verify(pluginRequest).appendToConsoleLog(eq(jobIdentifier), contains("Received request to create a pod for job"));
        verify(agentInstances).create(eq(request), eq(request.clusterProfileProperties()), eq(pluginRequest), any(ConsoleLogAppender.class));
    }

    @Test
    public void shouldLogErrorMessageToConsoleIfAgentCreateFails() throws Exception {
        final HashMap<String, String> elasticAgentProfileProperties = new HashMap<>();
        elasticAgentProfileProperties.put("Image", "image1");
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties("http://go-server", "http://k8ssvc.url", "");
        final JobIdentifier jobIdentifier = new JobIdentifier("p1", 1L, "l1", "s1", "1", "j1", 1L);
        CreateAgentRequest request = new CreateAgentRequest("key1", elasticAgentProfileProperties, "env1", jobIdentifier, clusterProfileProperties);
        AgentInstances<KubernetesInstance> agentInstances = mock(KubernetesAgentInstances.class);
        PluginRequest pluginRequest = mock(PluginRequest.class);

        when(agentInstances.create(any(), any(), any(), any())).thenThrow(new RuntimeException("Ouch!"));

        try {
            new CreateAgentRequestExecutor(request, agentInstances, pluginRequest).execute();
            fail("Should have thrown an exception.");
        } catch (Exception e) {
            // This is expected. Ignore.
        }
        verify(pluginRequest).appendToConsoleLog(any(), contains("Received request to create a pod for job"));
        verify(pluginRequest).appendToConsoleLog(any(), contains("Failed to create agent pod"));
    }
}
