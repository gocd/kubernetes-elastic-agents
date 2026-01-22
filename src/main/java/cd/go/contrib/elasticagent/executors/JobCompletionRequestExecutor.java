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

import java.util.List;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;

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
    public GoPluginApiResponse execute() {
        String elasticAgentId = jobCompletionRequest.getElasticAgentId();
        ClusterProfileProperties clusterProfileProperties = jobCompletionRequest.clusterProfileProperties();
        if (!clusterProfileProperties.getEnableAgentReuse()) {
            // Agent reuse disabled - immediately clean up the pod and agent, as it was only valid for this job.
            Agent agent = new Agent();
            agent.setElasticAgentId(elasticAgentId);

            LOG.info("[Job Completion] Terminating elastic agent with id {} on job completion {}.", elasticAgentId, jobCompletionRequest.jobIdentifier());

            List<Agent> agents = List.of(agent);
            pluginRequest.disableAgents(agents);
            agentInstances.terminate(agent.elasticAgentId(), clusterProfileProperties);
            pluginRequest.deleteAgents(agents);
        } else {
            // Agent reuse enabled - mark the pod/agent as idle and leave it for reuse by other jobs or eventual cleanup.
            KubernetesInstance updated = agentInstances.updateAgent(
                    elasticAgentId,
                    instance -> instance.toBuilder().agentState(KubernetesInstance.AgentState.Idle).build());
            if (updated != null) {
                LOG.info("[Job Completion] Received job completion for agent ID {}. It is now marked Idle.", elasticAgentId);
            } else {
                // This is unlikely to happen. This means the agent just
                // completed a job, but is not present in the plugin's
                // in-memory view of agents. If this agent continues running,
                // it will eventually be found by the periodic call to refresh
                // all agents, put in an Unknown state, and then terminated
                // after a timeout.
                // Alternatively, this could register the instance and put it
                // in an idle state.
                LOG.warn("[Job Completion] Received job completion for agent ID {}, which is not known to this plugin.", elasticAgentId);
            }
        }
        return DefaultGoPluginApiResponse.success("");
    }
}
