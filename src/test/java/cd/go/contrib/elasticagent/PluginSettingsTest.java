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

package cd.go.contrib.elasticagent;

import cd.go.contrib.elasticagent.model.AuthenticationStrategy;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

public class PluginSettingsTest {

    @Test
    public void shouldDeserializeFromJSON() {
        final Map<String, Object> pluginSettingsMap = new HashMap<>();
        pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
        pluginSettingsMap.put("auto_register_timeout", "13");
        pluginSettingsMap.put("pending_pods_count", 14);
        pluginSettingsMap.put("authentication_strategy", "ClUsTeR_certs");
        pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
        pluginSettingsMap.put("oauth_token", "foo-token");
        pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
        pluginSettingsMap.put("client_key_data", "client-key");
        pluginSettingsMap.put("client_cert_data", "client-cert");

        PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));

        assertThat(pluginSettings.getGoServerUrl(), is("https://foo.go.cd/go"));
        assertThat(pluginSettings.getAutoRegisterTimeout(), is(13));
        assertThat(pluginSettings.getMaxPendingPods(), is(14));
        assertThat(pluginSettings.getAuthenticationStrategy(), is(AuthenticationStrategy.CLUSTER_CERTS));
        assertThat(pluginSettings.getClusterUrl(), is("https://cloud.example.com"));
        assertThat(pluginSettings.getCaCertData(), is("foo-ca-certs"));
        assertThat(pluginSettings.getClientKeyData(), is("client-key"));
        assertThat(pluginSettings.getClientCertData(), is("client-cert"));
        assertThat(pluginSettings.getOauthToken(), is("foo-token"));

    }

    @Test
    public void shouldHaveDefaultValueAfterDeSerialization() {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{}");

        assertNull(pluginSettings.getGoServerUrl());
        assertThat(pluginSettings.getAutoRegisterTimeout(), is(10));
        assertThat(pluginSettings.getMaxPendingPods(), is(10));
        assertThat(pluginSettings.getAuthenticationStrategy(), is(AuthenticationStrategy.OAUTH_TOKEN));
        assertNull(pluginSettings.getClusterUrl());
        assertNull(pluginSettings.getCaCertData());
    }
}
