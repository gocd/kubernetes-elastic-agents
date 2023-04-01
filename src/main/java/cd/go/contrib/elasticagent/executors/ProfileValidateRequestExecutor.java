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

import cd.go.contrib.elasticagent.KubernetesInstanceFactory;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.model.Metadata;
import cd.go.contrib.elasticagent.requests.ProfileValidateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.fabric8.kubernetes.api.model.Pod;

import java.io.IOException;
import java.util.*;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.*;
import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static cd.go.contrib.elasticagent.utils.Util.isBlank;
import static java.text.MessageFormat.format;

public class ProfileValidateRequestExecutor implements RequestExecutor {
    private final ProfileValidateRequest request;

    public ProfileValidateRequestExecutor(ProfileValidateRequest request) {
        this.request = request;
    }

    @Override
    public GoPluginApiResponse execute() {
        LOG.debug("Validating elastic profile.");
        ArrayList<Map<String, String>> result = new ArrayList<>();
        List<String> knownFields = new ArrayList<>();

        for (Metadata field : GetProfileMetadataExecutor.FIELDS) {
            knownFields.add(field.getKey());

            Map<String, String> validationError = field.validate(request.getProperties().get(field.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        Set<String> set = new HashSet<>(request.getProperties().keySet());
        set.removeAll(knownFields);

        if (!set.isEmpty()) {
            for (String key : set) {
                LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
                validationError.put("key", key);
                validationError.put("message", "Is an unknown property");
                result.add(validationError);
            }
        }

        String podSpecType = request.getProperties().get(POD_SPEC_TYPE.getKey());

        if (podSpecType != null) {
            switch (podSpecType) {
                case "properties":
                    validateConfigPropertiesYaml(new HashMap<>(request.getProperties()), result);
                    break;
                case "remote":
                    validateRemoteFileSpec(new HashMap<>(request.getProperties()), result);
                    break;
                case "yaml":
                    validatePodYaml(new HashMap<>(request.getProperties()), result);
                    break;
                default:
                    LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
                    validationError.put("key", "PodSpecType");
                    validationError.put("message", "Should be one of `properties`, `remote`, `yaml`.");
                    result.add(validationError);
            }
        } else {

            boolean isSpecifiedUsingPodYaml = Boolean.valueOf(new HashMap<>(request.getProperties()).get(SPECIFIED_USING_POD_CONFIGURATION.getKey()));

            if (isSpecifiedUsingPodYaml) {
                validatePodYaml(new HashMap<>(request.getProperties()), result);
            } else {
                validateConfigPropertiesYaml(new HashMap<>(request.getProperties()), result);
            }
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private void validateRemoteFileSpec(HashMap<String, String> properties, ArrayList<Map<String, String>> result) {
        if (isBlank(properties.get(REMOTE_FILE.getKey()))) {
            addNotBlankError(result, REMOTE_FILE.getKey(), "RemoteFile");
        }
        if (isBlank(properties.get(REMOTE_FILE_TYPE.getKey()))) {
            addNotBlankError(result, REMOTE_FILE_TYPE.getKey(), "RemoteFileType");
        }
    }

    private void validateConfigPropertiesYaml(HashMap<String, String> properties, ArrayList<Map<String, String>> result) {
        String key = IMAGE.getKey();
        if (isBlank(properties.get(key))) {
            addNotBlankError(result, key, "Image");
        }
    }

    private void validatePodYaml(HashMap<String, String> properties, ArrayList<Map<String, String>> result) {
        String key = POD_CONFIGURATION.getKey();
        String podYaml = properties.get(key);
        if (isBlank(podYaml)) {
            addNotBlankError(result, key, "Pod Configuration");
            return;
        }
        Pod pod = new Pod();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            pod = mapper.readValue(KubernetesInstanceFactory.getTemplatizedPodSpec(podYaml), Pod.class);
        } catch (IOException e) {
            addError(result, key, "Invalid Pod Yaml.");
            return;
        }

        if (!isBlank(pod.getMetadata().getGenerateName())) {
            addError(result, key, "Invalid Pod Yaml. generateName field is not supported by GoCD. Please use {{ POD_POSTFIX }} instead.");
        }
    }

    private void addNotBlankError(ArrayList<Map<String, String>> result, String key, String value) {
        addError(result, key, format("{0} must not be blank.", value));
    }

    private void addError(ArrayList<Map<String, String>> result, String key, String message) {
        LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
        validationError.put("key", key);
        validationError.put("message", message);
        result.add(validationError);
    }
}
