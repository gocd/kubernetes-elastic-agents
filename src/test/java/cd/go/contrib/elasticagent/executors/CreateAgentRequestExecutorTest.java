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
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import org.junit.Test;

import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_AUTO_REGISTER_TIMEOUT;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_KUBERNETES_CLUSTER_CA_CERT;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_KUBERNETES_CLUSTER_URL;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_NAMESPACE;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_SECURITY_TOKEN;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

public class CreateAgentRequestExecutorTest {
    @Test
    public void shouldAskDockerContainersToCreateAnAgent() throws Exception {
        CreateAgentRequest request = mock(CreateAgentRequest.class);
        AgentInstances<KubernetesInstance> agentInstances = mock(KubernetesAgentInstances.class);
        PluginRequest pluginRequest = mock(PluginRequest.class);
        
        final Map<String, String> elasticProfileProperties = new HashMap<>();
		elasticProfileProperties.put(PROFILE_NAMESPACE.getKey(), "namespace");
		elasticProfileProperties.put(PROFILE_SECURITY_TOKEN.getKey(), "securityToken");
		elasticProfileProperties.put(PROFILE_AUTO_REGISTER_TIMEOUT.getKey(), "10");
		elasticProfileProperties.put(PROFILE_KUBERNETES_CLUSTER_URL.getKey(), "https://kub.com");
		elasticProfileProperties.put(PROFILE_KUBERNETES_CLUSTER_CA_CERT.getKey(), "cacert");
		
		PluginSettings settings = PluginSettingsMother.defaultPluginSettings();
		
		when(request.properties()).thenReturn(elasticProfileProperties);
        
        ElasticProfileSettings elasticProfileSettings = new ElasticProfileSettings("https://foo.go.cd/go",10,10,"https://kub.com","securityToken","cacert","namespace");
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        new CreateAgentRequestExecutor(request, agentInstances, pluginRequest).execute();

        verify(agentInstances).create(request, elasticProfileSettings, pluginRequest);
    }
}
