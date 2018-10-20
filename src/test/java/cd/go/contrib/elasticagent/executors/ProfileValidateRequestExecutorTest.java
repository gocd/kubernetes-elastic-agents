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

import cd.go.contrib.elasticagent.ElasticProfileFactory;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.PluginSettings;
import cd.go.contrib.elasticagent.requests.ProfileValidateRequest;
import io.fabric8.kubernetes.api.model.DoneableNamespace;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.google.gson.Gson;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.any;

import java.util.HashMap;
import java.util.Map;

public class ProfileValidateRequestExecutorTest {
	
    @Mock
    private KubernetesClientFactory kubernetesClientFactory;
    
    @Mock
    private ElasticProfileFactory elasticProfileFactory;
    
    @Mock
    private PluginRequest pluginRequest;
    
    @Mock
    private KubernetesClient client;
    
    @Mock
    private NonNamespaceOperation<Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>> mockedOperation;
    @Mock
    NamespaceList namespaceList;
    
    @Before
    public void setUp() {
        initMocks(this);
    }
    
    
    @Test
    public void shouldBarfWhenUnknownKeysArePassed() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("foo", "bar");
        properties.put("SpecifiedUsingPodConfiguration", "false");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"},{\"key\":\"foo\",\"message\":\"Is an unknown property\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeysForConfigProperties() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("SpecifiedUsingPodConfiguration", "false");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeysForPodConfiguration() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("SpecifiedUsingPodConfiguration", "true");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Pod Configuration must not be blank.\",\"key\":\"PodConfiguration\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidatePodConfigurationWhenSpecifiedAsYaml() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("SpecifiedUsingPodConfiguration", "true");
        properties.put("PodConfiguration", "this is my invalid fancy pod yaml!!");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Invalid Pod Yaml.\",\"key\":\"PodConfiguration\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldAllowPodYamlConfiguration() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("SpecifiedUsingPodConfiguration", "true");
        String podYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: pod-name\n" +
                "  labels:\n" +
                "    app: web\n" +
                "spec:\n" +
                "  containers:\n" +
                "    - name: gocd-agent-container\n" +
                "      image: gocd/fancy-agent-image:latest";

        properties.put("PodConfiguration", podYaml);
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[]", json, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void shouldAllowGinjaTemplatedPodYaml() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("SpecifiedUsingPodConfiguration", "true");
        String podYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: pod-name-prefix-{{ POD_POSTFIX }}\n" +
                "  labels:\n" +
                "    app: web\n" +
                "spec:\n" +
                "  containers:\n" +
                "    - name: gocd-agent-container-{{ CONTAINER_POSTFIX }}\n" +
                "      image: {{ GOCD_AGENT_IMAGE }}:{{ LATEST_VERSION }}";

        properties.put("PodConfiguration", podYaml);
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidatePodConfiguration() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("SpecifiedUsingPodConfiguration", "true");
        properties.put("PodConfiguration", "foobar");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Invalid Pod Yaml.\",\"key\":\"PodConfiguration\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }
    
    @Test
    public void shouldValidateNameSpaceConfiguration() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("SpecifiedUsingPodConfiguration", "true");
        properties.put("PodConfiguration", "foobar");
        properties.put("ProfileNamespace", "profileNameSpace");
        properties.put("SpecifiedUsingPodConfiguration", "true");
        String podYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: pod-name-prefix-{{ POD_POSTFIX }}\n" +
                "  labels:\n" +
                "    app: web\n" +
                "spec:\n" +
                "  containers:\n" +
                "    - name: gocd-agent-container-{{ CONTAINER_POSTFIX }}\n" +
                "      image: {{ GOCD_AGENT_IMAGE }}:{{ LATEST_VERSION }}";

        properties.put("PodConfiguration", podYaml);
        
        final Map<String, Object> pluginSettingsMap = new HashMap<>();
		pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
		pluginSettingsMap.put("auto_register_timeout", "13");
		pluginSettingsMap.put("pending_pods_count", 14);
		pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
		pluginSettingsMap.put("security_token", "foo-token");
		pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
		pluginSettingsMap.put("namespace", "gocd");

		PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));
        
        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(kubernetesClientFactory.createClientForElasticProfile(any())).thenReturn(client);
        when(client.namespaces()).thenReturn(mockedOperation);
        when(mockedOperation.list()).thenReturn(namespaceList);
        
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties),pluginRequest,kubernetesClientFactory,elasticProfileFactory);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"key\":\"ProfileNamespace\",\"message\":\"Namespace `profileNameSpace` does not exist in you cluster. Run \\\"kubectl create namespace profileNameSpace\\\" to create a namespace.\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }
}
