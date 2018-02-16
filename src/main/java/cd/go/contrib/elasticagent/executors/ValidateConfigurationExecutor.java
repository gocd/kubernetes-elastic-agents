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
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetPluginConfigurationExecutor.FIELDS;
import static cd.go.contrib.elasticagent.executors.GetPluginConfigurationExecutor.GO_SERVER_URL;
import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ValidateConfigurationExecutor implements RequestExecutor {
    private final ValidatePluginSettings settings;
    private PluginRequest pluginRequest;
    private List<Map<String, String>> result = new ArrayList<>();

    public ValidateConfigurationExecutor(ValidatePluginSettings settings, PluginRequest pluginRequest) {
        this.settings = settings;
        this.pluginRequest = pluginRequest;
    }

    public GoPluginApiResponse execute() throws ServerRequestFailedException {
        LOG.debug("Validating plugin settings.");

        for (Map.Entry<String, Field> entry : FIELDS.entrySet()) {
            Field field = entry.getValue();
            Map<String, String> validationError = field.validate(settings.get(entry.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        validateGoServerUrl();

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private void validateGoServerUrl() {
        if (isBlank(settings.get(GO_SERVER_URL.key()))) {
            ServerInfo severInfo = pluginRequest.getSeverInfo();
            if (isBlank(severInfo.getSecureSiteUrl())) {
                Map<String, String> error = error(GO_SERVER_URL.key(), "Secure site url is not configured. Please specify Go Server Url.");
                result.add(error);
            }
        }
    }

    private Map<String, String> error(String key, String errorMessage) {
        Map<String, String> error = new HashMap<>();
        error.put("key", key);
        error.put("message", errorMessage);
        return error;
    }
}
