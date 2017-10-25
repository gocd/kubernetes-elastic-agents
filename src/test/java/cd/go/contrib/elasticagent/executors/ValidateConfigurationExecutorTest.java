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
import cd.go.contrib.elasticagent.model.ServerInfo;
import cd.go.contrib.elasticagent.requests.ValidatePluginSettings;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ValidateConfigurationExecutorTest {

    @Mock
    private PluginRequest pluginRequest;
    private ServerInfo serverInfo;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        serverInfo = ServerInfo.fromJSON("{\n" +
                "\"server_id\": \"df0cb9be-2696-4689-8d46-1ef3c4e4447c\",\n" +
                "\"site_url\": \"http://example.com:8153/go\",\n" +
                "\"secure_site_url\": \"https://example.com:8154/go\"\n" +
                "}");
        when(pluginRequest.getSeverInfo()).thenReturn(serverInfo);
    }

    @Test
    public void shouldValidateABadConfiguration() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\n" +
                "  {\n" +
                "    \"message\": \"Agent auto-register Timeout (in minutes) must be a positive integer.\",\n" +
                "    \"key\": \"auto_register_timeout\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"message\": \"Kubernetes Cluster URL must not be blank.\",\n" +
                "    \"key\": \"kubernetes_cluster_url\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateAGoodConfiguration() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("go_server_url", "https://ci.example.com/go");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, null).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateSecureSiteUrl() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        serverInfo.setSecureSiteUrl(null);
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest).execute();

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
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("go_server_url", "foo.com/go(");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[" +
                "  {\n" +
                "    \"message\": \"Go Server URL must be a valid HTTPs URL (https://example.com).\",\n" +
                "    \"key\": \"go_server_url\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateGoServerUrlFormat() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("go_server_url", "https://foo.com");
        settings.put("kubernetes_cluster_url", "https://cluster.example.com");
        settings.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings, pluginRequest).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[" +
                "  {\n" +
                "    \"message\": \"Go Server URL must be in format https://<GO_SERVER_URL>:<GO_SERVER_PORT>/go.\",\n" +
                "    \"key\": \"go_server_url\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }
}
