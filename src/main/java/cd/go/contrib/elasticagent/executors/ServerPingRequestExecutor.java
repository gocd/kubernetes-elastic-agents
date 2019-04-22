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
import cd.go.contrib.elasticagent.requests.ServerPingRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;

public class ServerPingRequestExecutor implements RequestExecutor {

    private final ServerPingRequest serverPingRequest;
    private final Map<String, KubernetesAgentInstances> clusterSpecificAgentInstances;
    private final PluginRequest pluginRequest;

    public ServerPingRequestExecutor(ServerPingRequest serverPingRequest, Map<String, KubernetesAgentInstances> clusterSpecificAgentInstances, PluginRequest pluginRequest) {
        this.serverPingRequest = serverPingRequest;
        this.clusterSpecificAgentInstances = clusterSpecificAgentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        List<ClusterProfileProperties> allClusterProfileProperties = serverPingRequest.allClusterProfileProperties();

        for (ClusterProfileProperties clusterProfileProperties : allClusterProfileProperties) {
            performCleanupForACluster(clusterProfileProperties, clusterSpecificAgentInstances.get(clusterProfileProperties.uuid()));
        }

        CheckForPossiblyMissingAgents();
        return DefaultGoPluginApiResponse.success("");
    }

    private void performCleanupForACluster(ClusterProfileProperties clusterProfileProperties, KubernetesAgentInstances kubernetesAgentInstances) throws Exception {
        Agents allAgents = pluginRequest.listAgents();
        Agents agentsToDisable = kubernetesAgentInstances.instancesCreatedAfterTimeout(clusterProfileProperties, allAgents);
        disableIdleAgents(agentsToDisable);

        allAgents = pluginRequest.listAgents();
        terminateDisabledAgents(allAgents, clusterProfileProperties, kubernetesAgentInstances);

        kubernetesAgentInstances.terminateUnregisteredInstances(clusterProfileProperties, allAgents);
    }

    private void CheckForPossiblyMissingAgents() throws Exception {
        Collection<Agent> allAgents = pluginRequest.listAgents().agents();

        List<Agent> missingAgents = allAgents.stream().filter(agent -> clusterSpecificAgentInstances.values().stream()
                .noneMatch(instances -> instances.hasInstance(agent.elasticAgentId()))).collect(Collectors.toList());

        if (!missingAgents.isEmpty()) {
            List<String> missingAgentIds = missingAgents.stream().map(Agent::elasticAgentId).collect(Collectors.toList());
            LOG.warn("[Server Ping] Was expecting a containers with IDs " + missingAgentIds + ", but it was missing! Removing missing agents from config.");
            pluginRequest.disableAgents(missingAgents);
            pluginRequest.deleteAgents(missingAgents);
        }
    }

    private void disableIdleAgents(Agents agents) throws ServerRequestFailedException {
        Collection<Agent> instancesToDisable = agents.findInstancesToDisable();
        if (!instancesToDisable.isEmpty()) {
            pluginRequest.disableAgents(instancesToDisable);
        }
    }

    private void terminateDisabledAgents(Agents agents, ClusterProfileProperties clusterProfileProperties, KubernetesAgentInstances dockerContainers) throws Exception {
        Collection<Agent> toBeDeleted = agents.findInstancesToTerminate();

        for (Agent agent : toBeDeleted) {
            dockerContainers.terminate(agent.elasticAgentId(), clusterProfileProperties);
        }

        pluginRequest.deleteAgents(toBeDeleted);
    }
}
