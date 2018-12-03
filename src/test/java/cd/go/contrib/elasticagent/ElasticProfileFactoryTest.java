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

import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_AUTO_REGISTER_TIMEOUT;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_KUBERNETES_CLUSTER_CA_CERT;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_KUBERNETES_CLUSTER_URL;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_NAMESPACE;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_SECURITY_TOKEN;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

import cd.go.contrib.elasticagent.ElasticProfileFactory;
import cd.go.contrib.elasticagent.ElasticProfileSettings;
import cd.go.contrib.elasticagent.PluginSettings;

public class ElasticProfileFactoryTest {
	
	ElasticProfileFactory factory = ElasticProfileFactory.instance();

	@Test
	public void shouldCopyPluginSettings() {
		final Map<String, Object> pluginSettingsMap = new HashMap<>();
		pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
		pluginSettingsMap.put("auto_register_timeout", "13");
		pluginSettingsMap.put("pending_pods_count", 14);
		pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
		pluginSettingsMap.put("security_token", "foo-token");
		pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
		pluginSettingsMap.put("namespace", "gocd");

		PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));
		
		final Map<String, String> elasticProfileProperties = new HashMap<>();
		ElasticProfileSettings elasticProfileSettings = factory.from(elasticProfileProperties, pluginSettings);
		
		assertThat(elasticProfileSettings.getClusterUrl(), equalTo(pluginSettings.getClusterUrl()));
		assertThat(elasticProfileSettings.getGoServerUrl(), equalTo(pluginSettings.getGoServerUrl()));
		assertThat(elasticProfileSettings.getClusterCACertData(), equalTo(pluginSettings.getCaCertData()));
		assertThat(elasticProfileSettings.getNamespace(), equalTo(pluginSettings.getNamespace()));
		assertThat(elasticProfileSettings.getSecurityToken(), equalTo(pluginSettings.getSecurityToken()));

	}

	@Test
	public void shouldCopyPluginSettingsAndRetainProfileSettings() {
		
		final Map<String, Object> pluginSettingsMap = new HashMap<>();
		pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
		pluginSettingsMap.put("auto_register_timeout", "13");
		pluginSettingsMap.put("pending_pods_count", 14);
		pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
		pluginSettingsMap.put("security_token", "foo-token");
		pluginSettingsMap.put("kubernetes_cluster_ca_cert", "foo-ca-certs");
		pluginSettingsMap.put("namespace", "gocd");
		
		PluginSettings pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));
		
		final Map<String, String> elasticProfileProperties = new HashMap<>();
		elasticProfileProperties.put(PROFILE_NAMESPACE.getKey(), "gocdProf");
		elasticProfileProperties.put(PROFILE_SECURITY_TOKEN.getKey(), "prof-token");
		elasticProfileProperties.put(PROFILE_AUTO_REGISTER_TIMEOUT.getKey(), "5");
		elasticProfileProperties.put(PROFILE_KUBERNETES_CLUSTER_URL.getKey(), "https://prof-cloud.example.com");
		elasticProfileProperties.put(PROFILE_KUBERNETES_CLUSTER_CA_CERT.getKey(), "prof-foo-ca-certs");
		
		ElasticProfileSettings elasticProfileSettings = factory.from(elasticProfileProperties, pluginSettings);
		
		assertThat(elasticProfileSettings.getClusterUrl(), equalTo(elasticProfileProperties.get(PROFILE_KUBERNETES_CLUSTER_URL.getKey())));
		assertThat(elasticProfileSettings.getGoServerUrl(), equalTo(pluginSettings.getGoServerUrl()));
		assertThat(elasticProfileSettings.getClusterCACertData(), equalTo(elasticProfileProperties.get(PROFILE_KUBERNETES_CLUSTER_CA_CERT.getKey())));
		assertThat(elasticProfileSettings.getNamespace(), equalTo(elasticProfileProperties.get(PROFILE_NAMESPACE.getKey())));
		assertThat(elasticProfileSettings.getSecurityToken(), equalTo(elasticProfileProperties.get(PROFILE_SECURITY_TOKEN.getKey())));

	}
}
