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

package cd.go.contrib.elasticagent;

import cd.go.contrib.elasticagent.model.ServerInfo;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;

import java.util.Collection;

import static cd.go.contrib.elasticagent.Constants.*;
import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class PluginRequest {
    private final GoApplicationAccessor accessor;

    public PluginRequest(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    public ServerInfo getSeverInfo() throws ServerRequestFailedException {
        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_INFO, SERVER_INFO_API_VERSION, PLUGIN_IDENTIFIER);
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.serverInfo(response);
        }

        return ServerInfo.fromJSON(response.responseBody());
    }

    public PluginSettings getPluginSettings() throws ServerRequestFailedException {
        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_GET_PLUGIN_SETTINGS, API_VERSION, PLUGIN_IDENTIFIER);
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.getPluginSettings(response);
        }

        PluginSettings pluginSettings = PluginSettings.fromJSON(response.responseBody());
        if (pluginSettings == null) {
            throw new PluginSettingsNotConfiguredException();
        }

        return pluginSettings;
    }

    public Agents listAgents() throws ServerRequestFailedException {
        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_LIST_AGENTS, API_VERSION, PLUGIN_IDENTIFIER);
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.listAgents(response);
        }

        return new Agents(Agent.fromJSONArray(response.responseBody()));
    }

    public void disableAgents(Collection<Agent> toBeDisabled) throws ServerRequestFailedException {
        LOG.debug(format("[Server Ping] Disabling Agents: {0}", toBeDisabled.toString()));
        if (toBeDisabled.isEmpty()) {
            return;
        }

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DISABLE_AGENT, API_VERSION, PLUGIN_IDENTIFIER);
        request.setRequestBody(Agent.toJSONArray(toBeDisabled));

        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.disableAgents(response);
        }
    }

    public void deleteAgents(Collection<Agent> toBeDeleted) throws ServerRequestFailedException {
        LOG.debug(format("[Server Ping] Deleting Agents: {0}", toBeDeleted.toString()));
        if (toBeDeleted.isEmpty()) {
            return;
        }

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DELETE_AGENT, API_VERSION, PLUGIN_IDENTIFIER);
        request.setRequestBody(Agent.toJSONArray(toBeDeleted));
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.deleteAgents(response);
        }
    }
}
