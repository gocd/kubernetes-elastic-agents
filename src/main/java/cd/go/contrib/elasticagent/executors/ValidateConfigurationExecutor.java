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

import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.ServerRequestFailedException;
import cd.go.contrib.elasticagent.model.Field;
import cd.go.contrib.elasticagent.model.ServerInfo;
import cd.go.contrib.elasticagent.requests.ValidatePluginSettingsRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetPluginConfigurationExecutor.*;
import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ValidateConfigurationExecutor implements RequestExecutor {
    private final ValidatePluginSettingsRequest validatePluginSettingsRequest;
    private PluginRequest pluginRequest;
    private final KubernetesClientFactory factory;
    private List<Map<String, String>> result = new ArrayList<>();

    public ValidateConfigurationExecutor(ValidatePluginSettingsRequest validatePluginSettingsRequest, PluginRequest pluginRequest) {
        this(validatePluginSettingsRequest, pluginRequest, KubernetesClientFactory.instance());
    }

    ValidateConfigurationExecutor(ValidatePluginSettingsRequest validatePluginSettingsRequest, PluginRequest pluginRequest, KubernetesClientFactory factory) {
        this.validatePluginSettingsRequest = validatePluginSettingsRequest;
        this.pluginRequest = pluginRequest;
        this.factory = factory;
    }

    public GoPluginApiResponse execute() throws ServerRequestFailedException {
        LOG.debug("Validating plugin settings.");

        for (Map.Entry<String, Field> entry : FIELDS.entrySet()) {
            Field field = entry.getValue();
            Map<String, String> validationError = field.validate(validatePluginSettingsRequest.get(entry.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        validateGoServerUrl();
        validateNamespaceExistence();

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private void validateNamespaceExistence() {
        final String namespace = validatePluginSettingsRequest.getPluginSettingsMap().getNamespace();
        try {
            final KubernetesClient client = factory.client(validatePluginSettingsRequest.getPluginSettingsMap());
            final List<Namespace> namespaceList = client.namespaces().list().getItems();

            if (namespaceList.stream().anyMatch(n -> n.getMetadata().getName().equals(namespace))) {
                return;
            }

            result.add(error(NAMESPACE.key(), format("Namespace `{0}` does not exist in you cluster. Run \"kubectl create namespace {1}\" to create a namespace.", namespace, namespace)));
        } catch (Exception e) {
            LOG.error(format("Failed to validate namespace existence: {0} Please check plugin log for more detail.", namespace), e);
            result.add(error(NAMESPACE.key(), format("Failed to validate namespace existence: {0} Please check plugin log for more detail.", namespace)));
        }
    }

    private void validateGoServerUrl() {
        if (isBlank(validatePluginSettingsRequest.get(GO_SERVER_URL.key()))) {
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
