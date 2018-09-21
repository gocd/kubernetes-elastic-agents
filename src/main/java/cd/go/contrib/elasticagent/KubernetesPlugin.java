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

import static cd.go.contrib.elasticagent.Constants.PLUGIN_IDENTIFIER;

@Extension
public class KubernetesPlugin implements GoPlugin {
    public static final Logger LOG = Logger.getLoggerFor(KubernetesPlugin.class);

    private PluginRequest pluginRequest;
    private AgentInstances<KubernetesInstance> agentInstances;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        pluginRequest = new PluginRequest(accessor);
        agentInstances = new KubernetesAgentInstances();
    }

    @Load
    public void onLoad(PluginContext ctx) {
        LOG.info("Loading plugin " + Util.pluginId() + " version " + Util.fullVersion());
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        try {
            switch (Request.fromString(request.requestName())) {
                case REQUEST_GET_CAPABILITIES:
                    return new GetCapabilitiesExecutor().execute();
                case PLUGIN_SETTINGS_GET_ICON:
                    return new GetPluginSettingsIconExecutor().execute();
                case PLUGIN_SETTINGS_GET_CONFIGURATION:
                    return new GetPluginConfigurationExecutor().execute();
                case PLUGIN_SETTINGS_GET_VIEW:
                    return new GetViewRequestExecutor().execute();
                case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                    return ValidatePluginSettingsRequest.fromJSON(request.requestBody()).executor(pluginRequest).execute();
                case REQUEST_GET_PROFILE_METADATA:
                    return new GetProfileMetadataExecutor().execute();
                case REQUEST_GET_PROFILE_VIEW:
                    return new GetProfileViewExecutor().execute();
                case REQUEST_VALIDATE_PROFILE:
                    return ProfileValidateRequest.fromJSON(request.requestBody()).executor().execute();
                case REQUEST_CREATE_AGENT:
                    return CreateAgentRequest.fromJSON(request.requestBody()).executor(agentInstances, pluginRequest).execute();
                case REQUEST_SHOULD_ASSIGN_WORK:
                    refreshInstances();
                    return ShouldAssignWorkRequest.fromJSON(request.requestBody()).executor(agentInstances).execute();
                case REQUEST_SERVER_PING:
                    refreshInstances();
                    return new ServerPingRequestExecutor(agentInstances, pluginRequest).execute();
                case REQUEST_JOB_COMPLETION:
                    refreshInstances();
                    return JobCompletionRequest.fromJSON(request.requestBody()).executor(agentInstances, pluginRequest).execute();
                case REQUEST_STATUS_REPORT:
                    refreshInstances();
                    return new StatusReportExecutor(pluginRequest).execute();
                case REQUEST_ELASTIC_AGENT_STATUS_REPORT:
                    refreshInstances();
                    return AgentStatusReportRequest.fromJSON(request.requestBody()).executor(pluginRequest).execute();
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
        } catch (Exception e) {
            LOG.error("Failed to handle request " + request.requestName(), e);
            return DefaultGoPluginApiResponse.error("Failed to handle request " + request.requestName());
        }
    }

    private void refreshInstances() {
        try {
            agentInstances.refreshAll(pluginRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }

}
