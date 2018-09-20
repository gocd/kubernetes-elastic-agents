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

package cd.go.contrib.elasticagent.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

import cd.go.contrib.elasticagent.ElasticProfileSettings;
import cd.go.contrib.elasticagent.PluginSettings;
import cd.go.contrib.elasticagent.utils.SettingsUtil;

public class SettingsUtilTest {

	@Test
	public void shouldCopyPluginSettings() {
		ElasticProfileSettings elasticProfileSettings = new ElasticProfileSettings();
		final Map<String, Object> pluginSettingsMap = new HashMap<>();
		pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
		pluginSettingsMap.put("auto_register_timeout", "13");
		pluginSettingsMap.put("pending_pods_count", 14);
		pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
		pluginSettingsMap.put("security_token", "foo-token");
		pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
		pluginSettingsMap.put("namespace", "gocd");

		PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));

		ElasticProfileSettings mergedSettings = SettingsUtil.mergeSettings(elasticProfileSettings, pluginSettings);
		assertThat(mergedSettings.getClusterUrl(), equalTo(pluginSettings.getClusterUrl()));
		assertThat(mergedSettings.getGoServerUrl(), equalTo(pluginSettings.getGoServerUrl()));
		assertThat(mergedSettings.getClusterCACertData(), equalTo(pluginSettings.getCaCertData()));
		assertThat(mergedSettings.getNamespace(), equalTo(pluginSettings.getNamespace()));
		assertThat(mergedSettings.getSecurityToken(), equalTo(pluginSettings.getSecurityToken()));
		assertThat(mergedSettings.getAutoRegisterPeriod(), equalTo(pluginSettings.getAutoRegisterPeriod()));

	}

	@Test
	public void shouldCopyPluginSettingsAndRetainProfileSettings() {
		ElasticProfileSettings elasticProfileSettings = new ElasticProfileSettings();
		elasticProfileSettings.setNamespace("profileNamespace");
		elasticProfileSettings.setSecurityToken("securityToken");
		elasticProfileSettings.setAutoRegisterTimeout(new Integer(5));

		final Map<String, Object> pluginSettingsMap = new HashMap<>();
		pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
		pluginSettingsMap.put("auto_register_timeout", "13");
		pluginSettingsMap.put("pending_pods_count", 14);
		pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
		pluginSettingsMap.put("security_token", "foo-token");
		pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
		pluginSettingsMap.put("namespace", "gocd");

		PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));

		ElasticProfileSettings mergedSettings = SettingsUtil.mergeSettings(elasticProfileSettings, pluginSettings);
		assertThat(mergedSettings.getClusterUrl(), equalTo(pluginSettings.getClusterUrl()));
		assertThat(mergedSettings.getGoServerUrl(), equalTo(pluginSettings.getGoServerUrl()));
		assertThat(mergedSettings.getClusterCACertData(), equalTo(pluginSettings.getCaCertData()));
		assertThat(mergedSettings.getNamespace(), equalTo(elasticProfileSettings.getNamespace()));
		assertThat(mergedSettings.getSecurityToken(), equalTo(elasticProfileSettings.getSecurityToken()));
		assertThat(mergedSettings.getAutoRegisterPeriod(), equalTo(elasticProfileSettings.getAutoRegisterPeriod()));

	}
}
