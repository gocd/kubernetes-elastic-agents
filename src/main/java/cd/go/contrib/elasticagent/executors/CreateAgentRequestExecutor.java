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
import cd.go.contrib.elasticagent.ElasticProfileFactory;
import cd.go.contrib.elasticagent.ElasticProfileSettings;
import cd.go.contrib.elasticagent.KubernetesInstance;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class CreateAgentRequestExecutor implements RequestExecutor {
    private final AgentInstances<KubernetesInstance> agentInstances;
    private final PluginRequest pluginRequest;
    private final CreateAgentRequest request;
    private ElasticProfileFactory elasticProfileFactory;

    public CreateAgentRequestExecutor(CreateAgentRequest request, AgentInstances<KubernetesInstance> agentInstances, PluginRequest pluginRequest) {
        this.request = request;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
        this.elasticProfileFactory = ElasticProfileFactory.instance();
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        LOG.debug(format("[Create Agent] creating elastic agent for profile {0}", request.properties()));
        ElasticProfileSettings elasticProfileSettings = elasticProfileFactory.from(request.properties(), pluginRequest.getPluginSettings());
        agentInstances.create(request, elasticProfileSettings, pluginRequest);
        return new DefaultGoPluginApiResponse(200);
    }

}
