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

import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.ServerRequestFailedException;
import cd.go.contrib.elasticagent.model.Field;
import cd.go.contrib.elasticagent.model.ServerInfo;
import cd.go.contrib.elasticagent.requests.ValidatePluginSettings;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class ValidateConfigurationExecutor implements RequestExecutor {
    private final ValidatePluginSettings settings;
    private PluginRequest pluginRequest;

    public ValidateConfigurationExecutor(ValidatePluginSettings settings, PluginRequest pluginRequest) {
        this.settings = settings;
        this.pluginRequest = pluginRequest;
    }

    public GoPluginApiResponse execute() throws ServerRequestFailedException {
        LOG.debug("Validating plugin settings.");
        ArrayList<Map<String, String>> result = new ArrayList<>();

        for (Map.Entry<String, Field> entry : GetPluginConfigurationExecutor.FIELDS.entrySet()) {
            Field field = entry.getValue();
            Map<String, String> validationError = field.validate(settings.get(entry.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        if(StringUtils.isBlank(settings.get("go_server_url"))) {
            ServerInfo severInfo = pluginRequest.getSeverInfo();
            if(StringUtils.isBlank(severInfo.getSecureSiteUrl())) {
                HashMap<String, String> error = new HashMap<>();
                error.put("key", "go_server_url");
                error.put("message", "Secure site url is not configured. Please specify Go Server Url.");
                result.add(error);
            }
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }
}
