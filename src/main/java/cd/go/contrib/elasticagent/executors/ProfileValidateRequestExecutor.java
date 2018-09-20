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
import cd.go.contrib.elasticagent.KubernetesInstanceFactory;
import cd.go.contrib.elasticagent.ElasticProfileSettings;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.PluginSettings;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.model.Metadata;
import cd.go.contrib.elasticagent.requests.ProfileValidateRequest;
import cd.go.contrib.elasticagent.utils.SettingsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.*;
import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static java.text.MessageFormat.format;

public class ProfileValidateRequestExecutor implements RequestExecutor {
    private final ProfileValidateRequest request;
    private PluginRequest pluginRequest;
    private final KubernetesClientFactory factory;

    public ProfileValidateRequestExecutor(ProfileValidateRequest request,PluginRequest pluginRequest,KubernetesClientFactory factory) { 
        this.request = request;
        this.pluginRequest = pluginRequest;
        this.factory = factory;
    }

    @Override
    public GoPluginApiResponse execute() {
        LOG.debug("Validating elastic profile.");
        ArrayList<Map<String, String>> result = new ArrayList<>();
        List<String> knownFields = new ArrayList<>();
        
        validateNamespaceExistence(new HashMap<>(request.getProperties()), result);

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

        boolean isSpecifiedUsingPodYaml = Boolean.valueOf(new HashMap<>(request.getProperties()).get(SPECIFIED_USING_POD_CONFIGURATION.getKey()));

        if (isSpecifiedUsingPodYaml) {
            validatePodYaml(new HashMap<>(request.getProperties()), result);
        } else {
            validateConfigPropertiesYaml(new HashMap<>(request.getProperties()), result);
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private void validateConfigPropertiesYaml(HashMap<String, String> properties, ArrayList<Map<String, String>> result) {
        String key = IMAGE.getKey();
        if (StringUtils.isBlank(properties.get(key))) {
            addNotBlankError(result, key, "Image");
        }
    }

    private void validatePodYaml(HashMap<String, String> properties, ArrayList<Map<String, String>> result) {
        String key = POD_CONFIGURATION.getKey();
        String podYaml = properties.get(key);
        if (StringUtils.isBlank(podYaml)) {
            addNotBlankError(result, key, "Pod Configuration");
            return;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            mapper.readValue(KubernetesInstanceFactory.getTemplatizedPodYamlString(podYaml), Pod.class);
        } catch (IOException e) {
            addError(result, key, "Invalid Pod Yaml.");
        }
    }
    
    private void validateNamespaceExistence(HashMap<String, String> properties, ArrayList<Map<String, String>> result) {
        final String namespace = properties.get(PROFILE_NAMESPACE.getKey());
        LOG.info("validateNamespaceExistence"+namespace);
        if(StringUtils.isNotBlank(namespace)) {
	        try {
	        	PluginSettings pluginSettings = pluginRequest.getPluginSettings();
	        	
	        	ElasticProfileSettings elasticProfileSettings = new ElasticProfileSettings();
	         	elasticProfileSettings.setNamespace(properties.get(PROFILE_NAMESPACE.getKey()));
	         	elasticProfileSettings.setSecurityToken(properties.get(PROFILE_SECURITY_TOKEN.getKey()));
	         	final String autoRegisterTimeout = properties.get(PROFILE_AUTO_REGISTER_TIMEOUT.getKey());
	         	if(StringUtils.isNotBlank(autoRegisterTimeout)) {
	         		elasticProfileSettings.setAutoRegisterTimeout(Integer.valueOf(autoRegisterTimeout));
	         	}
	         	
	         	elasticProfileSettings = SettingsUtil.mergeSettings(elasticProfileSettings, pluginSettings);
	            final KubernetesClient client = factory.createClientForElasticProfile(elasticProfileSettings);
	            final List<Namespace> namespaceList = client.namespaces().list().getItems();
	
	            if (namespaceList.stream().anyMatch(n -> n.getMetadata().getName().equals(namespace))) {
	                return;
	            }
	
	            addError(result,PROFILE_NAMESPACE.getKey(), format("Namespace `{0}` does not exist in you cluster. Run \"kubectl create namespace {1}\" to create a namespace.", namespace, namespace));
	        } catch (Exception e) {
	            String message = "Failed validation of plugin settings. The reasons could be - " +
	                    "Cluster Url is configured incorrectly or " +
	                    "the service account token might not have enough permissions to list namespaces or " +
	                    "incorrect CA certificate.";
	            LOG.error(message, e);
	            addError(result,PROFILE_NAMESPACE.getKey(), format(message + "Please check the plugin log for more details."));
	        }
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
