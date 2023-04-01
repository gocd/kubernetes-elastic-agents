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

import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.model.Metadata;
import cd.go.contrib.elasticagent.model.ServerInfo;
import cd.go.contrib.elasticagent.requests.ClusterProfileValidateRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

import static cd.go.contrib.elasticagent.GoServerURLMetadata.GO_SERVER_URL;
import static cd.go.contrib.elasticagent.executors.GetClusterProfileMetadataExecutor.FIELDS;
import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static cd.go.contrib.elasticagent.utils.Util.isBlank;

public class ClusterProfileValidateRequestExecutor implements RequestExecutor {
    private ClusterProfileValidateRequest request;
    private PluginRequest pluginRequest;

    public ClusterProfileValidateRequestExecutor(ClusterProfileValidateRequest clusterProfileValidateRequest, PluginRequest pluginRequest) {
        this.request = clusterProfileValidateRequest;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        List<String> knownFields = new ArrayList<>();

        for (Metadata field : FIELDS) {
            knownFields.add(field.getKey());
            Map<String, String> validationError = field.validate(request.getProperties().get(field.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        validateGoServerUrl(result);
        Set<String> set = new HashSet<>(request.getProperties().keySet());
        set.removeAll(knownFields);

        if (!set.isEmpty()) {
            for (String key : set) {
                LinkedHashMap<String, String> validationError = validationError(key, "Is an unknown property");
                result.add(validationError);
            }
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private LinkedHashMap<String, String> validationError(String key, String message) {
        LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
        validationError.put("key", key);
        validationError.put("message", message);
        return validationError;
    }

    private void validateGoServerUrl(ArrayList<Map<String, String>> result) {
        if (isBlank(request.getProperties().get(GO_SERVER_URL))) {
            ServerInfo severInfo = pluginRequest.getSeverInfo();
            if (isBlank(severInfo.getSecureSiteUrl())) {
                Map<String, String> error = validationError(GO_SERVER_URL, "Secure site url is not configured. Please specify Go Server Url.");
                result.add(error);
            }
        }
    }
}
