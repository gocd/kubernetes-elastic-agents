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
import cd.go.contrib.elasticagent.model.ServerInfo;
import cd.go.contrib.elasticagent.requests.ClusterProfileValidateRequest;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterProfileValidateRequestExecutorTest {
    @Test
    public void shouldBarfWhenUnknownKeysArePassed() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("foo", "bar");
        properties.put("go_server_url", "https://foobar.com/go");
        properties.put("kubernetes_cluster_url", "something");
        properties.put("security_token", "something");
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(properties), mock(PluginRequest.class));
        String json = executor.execute().responseBody();

        JSONAssert.assertEquals("[{\"key\":\"foo\",\"message\":\"Is an unknown property\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeys() throws Exception {
        Map<String, String> properties = new HashMap<>();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(properties), pluginRequest);
        when(pluginRequest.getSeverInfo()).thenReturn(new ServerInfo());
        String json = executor.execute().responseBody();
        String expected = "[" +
                "  {" +
                "    \"message\":\"Secure site url is not configured. Please specify Go Server Url.\"," +
                "    \"key\":\"go_server_url\"" +
                "  }" +
                "]";

        JSONAssert.assertEquals(expected, json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldBarfWhenInvalidGoServerUrlIsPassed() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("go_server_url", "something");
        properties.put("kubernetes_cluster_url", "something");
        properties.put("security_token", "something");
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(properties), mock(PluginRequest.class));
        String json = executor.execute().responseBody();

        JSONAssert.assertEquals("[{\"key\":\"go_server_url\",\"message\":\"go_server_url must be a valid URL (https://example.com:8154/go).\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldNotFailWhenSecureSiteUrlIsConfiguredAndNoGoServerUrlIsSpecified() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("kubernetes_cluster_url", "something");
        properties.put("security_token", "something");

        PluginRequest pluginRequest = mock(PluginRequest.class);
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(properties), pluginRequest);
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setSecureSiteUrl("https://build.gocd.org/go");
        when(pluginRequest.getSeverInfo()).thenReturn(serverInfo);
        String json = executor.execute().responseBody();

        JSONAssert.assertEquals("[]", json, JSONCompareMode.NON_EXTENSIBLE);
    }
}
