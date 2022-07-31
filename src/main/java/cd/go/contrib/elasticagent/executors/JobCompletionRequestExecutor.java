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
import cd.go.contrib.elasticagent.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;
import java.util.List;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class JobCompletionRequestExecutor implements RequestExecutor {
    private final JobCompletionRequest jobCompletionRequest;
    private final AgentInstances<KubernetesInstance> agentInstances;
    private final PluginRequest pluginRequest;

    public JobCompletionRequestExecutor(JobCompletionRequest jobCompletionRequest, AgentInstances<KubernetesInstance> agentInstances, PluginRequest pluginRequest) {
        this.jobCompletionRequest = jobCompletionRequest;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        ClusterProfileProperties clusterProfileProperties = jobCompletionRequest.clusterProfileProperties();

        String elasticAgentId = jobCompletionRequest.getElasticAgentId();

        Agent agent = new Agent();
        agent.setElasticAgentId(elasticAgentId);

        LOG.info(format("[Job Completion] Terminating elastic agent with id {0} on job completion {1}.", agent.elasticAgentId(), jobCompletionRequest.jobIdentifier()));

        List<Agent> agents = Arrays.asList(agent);
        pluginRequest.disableAgents(agents);
        agentInstances.terminate(agent.elasticAgentId(), clusterProfileProperties);
        pluginRequest.deleteAgents(agents);

        return DefaultGoPluginApiResponse.success("");
    }
}
