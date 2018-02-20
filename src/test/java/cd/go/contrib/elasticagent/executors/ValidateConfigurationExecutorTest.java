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
import cd.go.contrib.elasticagent.model.ServerInfo;
import cd.go.contrib.elasticagent.requests.ValidatePluginSettingsRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.fabric8.kubernetes.api.model.DoneableNamespace;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ValidateConfigurationExecutorTest {
    @Mock
    private PluginRequest pluginRequest;

    @Mock
    KubernetesClientFactory factory;
    @Mock
    private KubernetesClient client;
    @Mock
    private NonNamespaceOperation<Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>> mockedOperation;
    @Mock
    NamespaceList namespaceList;
    private ServerInfo serverInfo;

    @Before
    public void setUp() {
        initMocks(this);
        serverInfo = ServerInfo.fromJSON("{\n" +
                "\"server_id\": \"df0cb9be-2696-4689-8d46-1ef3c4e4447c\",\n" +
                "\"site_url\": \"http://example.com:8153/go\",\n" +
                "\"secure_site_url\": \"https://example.com:8154/go\"\n" +
                "}");
        when(pluginRequest.getSeverInfo()).thenReturn(serverInfo);
        when(factory.client(any())).thenReturn(client);
        when(client.namespaces()).thenReturn(mockedOperation);
        when(mockedOperation.list()).thenReturn(namespaceList);
    }

    @Test
    public void shouldValidateABadConfiguration() throws Exception {
        when(namespaceList.getItems()).thenReturn(getNamespaceList("default"));

        ValidatePluginSettingsRequest settings = new ValidatePluginSettingsRequest();
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest, factory).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\n" +
                "  {\n" +
                "    \"message\": \"Cluster URL must not be blank.\",\n" +
                "    \"key\": \"kubernetes_cluster_url\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"message\": \"Oauth token must not be blank.\",\n" +
                "    \"key\": \"oauth_token\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateAGoodConfiguration() throws Exception {
        when(namespaceList.getItems()).thenReturn(getNamespaceList("default"));

        ValidatePluginSettingsRequest settings = new ValidatePluginSettingsRequest();
        settings.put("go_server_url", "https://ci.example.com/go");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("oauth_token", "some-token");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, null, factory).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateGoServerUrl() throws Exception {
        when(namespaceList.getItems()).thenReturn(getNamespaceList("default"));

        ValidatePluginSettingsRequest settings = new ValidatePluginSettingsRequest();
        serverInfo.setSecureSiteUrl(null);
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("oauth_token", "some-token");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest, factory).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[" +
                "  {\n" +
                "    \"message\": \"Secure site url is not configured. Please specify Go Server Url.\",\n" +
                "    \"key\": \"go_server_url\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateGoServerHTTPSUrlFormat() throws Exception {
        when(namespaceList.getItems()).thenReturn(getNamespaceList("default"));

        ValidatePluginSettingsRequest settings = new ValidatePluginSettingsRequest();
        settings.put("go_server_url", "foo.com/go(");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("oauth_token", "some-token");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest, factory).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[" +
                "  {\n" +
                "    \"message\": \"GoCD server URL must be a valid HTTPs URL (https://example.com).\",\n" +
                "    \"key\": \"go_server_url\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateGoServerUrlFormat() throws Exception {
        when(namespaceList.getItems()).thenReturn(getNamespaceList("default"));

        ValidatePluginSettingsRequest settings = new ValidatePluginSettingsRequest();
        settings.put("go_server_url", "https://foo.com");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("oauth_token", "some-token");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest, factory).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[" +
                "  {\n" +
                "    \"message\": \"GoCD server URL must be in format https://<GO_SERVER_URL>:<GO_SERVER_PORT>/go.\",\n" +
                "    \"key\": \"go_server_url\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateOAuthTokenWhenAuthenticationStrategyIsSetToOauthToken() throws JSONException {
        when(namespaceList.getItems()).thenReturn(getNamespaceList("default"));

        ValidatePluginSettingsRequest settings = new ValidatePluginSettingsRequest();
        settings.put("go_server_url", "https://foo.com/go");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");

        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest, factory).execute();

        assertThat(response.responseCode(), is(200));

        JSONAssert.assertEquals("[" +
                "  {\n" +
                "    \"message\": \"Oauth token must not be blank.\",\n" +
                "    \"key\": \"oauth_token\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateNamespaceExistence() throws JSONException {
        when(namespaceList.getItems()).thenReturn(getNamespaceList("default"));

        ValidatePluginSettingsRequest settings = new ValidatePluginSettingsRequest();
        settings.put("go_server_url", "https://ci.example.com/go");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("oauth_token", "some-token");
        settings.put("namespace", "gocd");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, null, factory).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\n" +
                "  {\n" +
                "    \"message\": \"Namespace `gocd` does not exist in you cluster. Run \\\"kubectl create namespace gocd\\\" to create a namespace.\",\n" +
                "    \"key\": \"namespace\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    private List<Namespace> getNamespaceList(String... namespaces) {
        if (namespaces == null || namespaces.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(namespaces).stream()
                .map(namespaceName -> new NamespaceBuilder().withNewMetadata().withName("default").endMetadata().build())
                .collect(Collectors.toList());
    }
}
