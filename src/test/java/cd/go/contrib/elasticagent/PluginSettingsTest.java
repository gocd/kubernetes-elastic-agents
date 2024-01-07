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

package cd.go.contrib.elasticagent;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginSettingsTest {

    @Test
    public void shouldDeserializeFromJSON() {
        final Map<String, Object> pluginSettingsMap = new HashMap<>();
        pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
        pluginSettingsMap.put("auto_register_timeout", "13");
        pluginSettingsMap.put("pending_pods_count", "14");
        pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
        pluginSettingsMap.put("security_token", "foo-token");
        pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
        pluginSettingsMap.put("namespace", "gocd");
        pluginSettingsMap.put("cluster_request_timeout", "60000");

        PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));

        assertThat(pluginSettings.getGoServerUrl()).isEqualTo("https://foo.go.cd/go");
        assertThat(pluginSettings.getAutoRegisterTimeout()).isEqualTo(13);
        assertThat(pluginSettings.getMaxPendingPods()).isEqualTo(14);
        assertThat(pluginSettings.getClusterUrl()).isEqualTo("https://cloud.example.com");
        assertThat(pluginSettings.getCaCertData()).isEqualTo("foo-ca-certs");
        assertThat(pluginSettings.getSecurityToken()).isEqualTo("foo-token");
        assertThat(pluginSettings.getNamespace()).isEqualTo("gocd");
        assertThat(pluginSettings.getClusterRequestTimeout()).isEqualTo(60000);

    }

    @Test
    public void shouldHandleEmptyValuesForPendingPodsAndAutoRegisterTimeout() {
        final Map<String, Object> pluginSettingsMap = new HashMap<>();
        pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
        pluginSettingsMap.put("auto_register_timeout", "");
        pluginSettingsMap.put("pending_pods_count", null);
        pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
        pluginSettingsMap.put("security_token", "foo-token");
        pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
        pluginSettingsMap.put("namespace", "gocd");
        pluginSettingsMap.put("cluster_request_timeout", null);

        PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));

        assertThat(pluginSettings.getGoServerUrl()).isEqualTo("https://foo.go.cd/go");
        assertThat(pluginSettings.getAutoRegisterTimeout()).isEqualTo(10);
        assertThat(pluginSettings.getMaxPendingPods()).isEqualTo(10);
        assertThat(pluginSettings.getClusterUrl()).isEqualTo("https://cloud.example.com");
        assertThat(pluginSettings.getCaCertData()).isEqualTo("foo-ca-certs");
        assertThat(pluginSettings.getSecurityToken()).isEqualTo("foo-token");
        assertThat(pluginSettings.getNamespace()).isEqualTo("gocd");
        assertThat(pluginSettings.getClusterRequestTimeout()).isEqualTo(10000);
    }

    @Test
    public void shouldHaveDefaultValueAfterDeSerialization() {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{}");

        assertThat(pluginSettings.getGoServerUrl()).isNull();
        assertThat(pluginSettings.getAutoRegisterTimeout()).isEqualTo(10);
        assertThat(pluginSettings.getMaxPendingPods()).isEqualTo(10);
        assertThat(pluginSettings.getNamespace()).isNull();
        assertThat(pluginSettings.getClusterUrl()).isNull();
        assertThat(pluginSettings.getCaCertData()).isNull();
        assertThat(pluginSettings.getSecurityToken()).isNull();
        assertThat(pluginSettings.getClusterRequestTimeout()).isEqualTo(10000);
    }

    @Test
    public void shouldConsiderBlankClusterValuesAsNull() {
        final Map<String, Object> pluginSettingsMap = new HashMap<>();
        pluginSettingsMap.put("kubernetes_cluster_url", "   ");
        pluginSettingsMap.put("namespace", "   ");
        pluginSettingsMap.put("security_token", "   ");
        pluginSettingsMap.put("kubernetes_cluster_ca_cert", "   ");

        PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));

        assertThat(pluginSettings.getNamespace()).isNull();
        assertThat(pluginSettings.getClusterUrl()).isNull();
        assertThat(pluginSettings.getSecurityToken()).isNull();
        assertThat(pluginSettings.getCaCertData()).isNull();
    }
}
