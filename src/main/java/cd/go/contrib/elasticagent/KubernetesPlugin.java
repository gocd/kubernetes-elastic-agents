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

package cd.go.contrib.elasticagent;

import cd.go.contrib.elasticagent.executors.*;
import cd.go.contrib.elasticagent.requests.*;
import cd.go.contrib.elasticagent.utils.Util;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagent.Constants.PLUGIN_IDENTIFIER;

@Extension
public class KubernetesPlugin implements GoPlugin {
    public static final Logger LOG = Logger.getLoggerFor(KubernetesPlugin.class);

    private PluginRequest pluginRequest;
    private Map<String, KubernetesAgentInstances> clusterSpecificAgentInstances;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        pluginRequest = new PluginRequest(accessor);
        clusterSpecificAgentInstances = new HashMap<>();
    }

    @Load
    public void onLoad(PluginContext ctx) {
        LOG.info("Loading plugin " + Util.pluginId() + " version " + Util.fullVersion());
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        ClusterProfileProperties clusterProfileProperties;
        try {
            switch (Request.fromString(request.requestName())) {
                case REQUEST_GET_CAPABILITIES:
                    return new GetCapabilitiesExecutor().execute();
                case PLUGIN_SETTINGS_GET_ICON:
                    return new GetPluginSettingsIconExecutor().execute();
                case REQUEST_GET_ELASTIC_AGENT_PROFILE_METADATA:
                    return new GetProfileMetadataExecutor().execute();
                case REQUEST_GET_ELASTIC_AGENT_PROFILE_VIEW:
                    return new GetProfileViewExecutor().execute();
                case REQUEST_VALIDATE_ELASTIC_AGENT_PROFILE:
                    return ProfileValidateRequest.fromJSON(request.requestBody()).executor().execute();
                case REQUEST_GET_CLUSTER_PROFILE_METADATA:
                    return new GetClusterProfileMetadataExecutor().execute();
                case REQUEST_GET_CLUSTER_PROFILE_VIEW:
                    return new GetClusterProfileViewRequestExecutor().execute();
                case REQUEST_VALIDATE_CLUSTER_PROFILE_CONFIGURATION:
                    return ClusterProfileValidateRequest.fromJSON(request.requestBody()).executor(pluginRequest).execute();
                case REQUEST_CREATE_AGENT:
                    CreateAgentRequest createAgentRequest = CreateAgentRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = createAgentRequest.clusterProfileProperties();
                    return createAgentRequest.executor(getAgentInstancesFor(clusterProfileProperties), pluginRequest).execute();
                case REQUEST_SHOULD_ASSIGN_WORK:
                    ShouldAssignWorkRequest shouldAssignWorkRequest = ShouldAssignWorkRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = shouldAssignWorkRequest.clusterProfileProperties();
                    return shouldAssignWorkRequest.executor(getAgentInstancesFor(clusterProfileProperties)).execute();
                case REQUEST_SERVER_PING:
                    ServerPingRequest serverPingRequest = ServerPingRequest.fromJSON(request.requestBody());
                    List<ClusterProfileProperties> listOfClusterProfileProperties = serverPingRequest.allClusterProfileProperties();
                    refreshInstancesForAllClusters(listOfClusterProfileProperties);
                    return serverPingRequest.executor(clusterSpecificAgentInstances, pluginRequest).execute();
                case REQUEST_JOB_COMPLETION:
                    JobCompletionRequest jobCompletionRequest = JobCompletionRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = jobCompletionRequest.clusterProfileProperties();
                    return jobCompletionRequest.executor(getAgentInstancesFor(clusterProfileProperties), pluginRequest).execute();
                case REQUEST_CLUSTER_STATUS_REPORT:
                    ClusterStatusReportRequest clusterStatusReportRequest = ClusterStatusReportRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = clusterStatusReportRequest.clusterProfileProperties();
                    KubernetesClientFactory.instance().clearOutExistingClient();
                    refreshInstancesForCluster(clusterProfileProperties);
                    return clusterStatusReportRequest.executor().execute();
                case REQUEST_ELASTIC_AGENT_STATUS_REPORT:
                    AgentStatusReportRequest statusReportRequest = AgentStatusReportRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = statusReportRequest.clusterProfileProperties();
                    refreshInstancesForCluster(clusterProfileProperties);
                    return statusReportRequest.executor().execute();
                case REQUEST_CLUSTER_PROFILE_CHANGED:
                    return new DefaultGoPluginApiResponse(200);
                case REQUEST_MIGRATE_CONFIGURATION:
                    return MigrateConfigurationRequest.fromJSON(request.requestBody()).executor().execute();
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
        } catch (Exception e) {
            LOG.error("Failed to handle request " + request.requestName(), e);
            return DefaultGoPluginApiResponse.error("Failed to handle request " + request.requestName());
        }
    }

    private void refreshInstancesForAllClusters(List<ClusterProfileProperties> listOfClusterProfileProperties) throws Exception {
        for (ClusterProfileProperties clusterProfileProperties : listOfClusterProfileProperties) {
            refreshInstancesForCluster(clusterProfileProperties);
        }
    }

    private AgentInstances<KubernetesInstance> getAgentInstancesFor(ClusterProfileProperties clusterProfileProperties) throws Exception {
        KubernetesAgentInstances agentInstances = clusterSpecificAgentInstances.get(clusterProfileProperties.uuid());

        //initialize agent instances if those are null
        if (agentInstances == null) {
            refreshInstancesForCluster(clusterProfileProperties);
            agentInstances = clusterSpecificAgentInstances.get(clusterProfileProperties.uuid());
        }

        return agentInstances;
    }

    private void refreshInstancesForCluster(ClusterProfileProperties clusterProfileProperties) throws Exception {
        KubernetesAgentInstances kubernetesInstances = clusterSpecificAgentInstances.getOrDefault(clusterProfileProperties.uuid(), new KubernetesAgentInstances());
        kubernetesInstances.refreshAll(clusterProfileProperties);

        clusterSpecificAgentInstances.put(clusterProfileProperties.uuid(), kubernetesInstances);
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }
}
