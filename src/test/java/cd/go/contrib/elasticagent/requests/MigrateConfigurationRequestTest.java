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

package cd.go.contrib.elasticagent.requests;

import cd.go.contrib.elasticagent.ClusterProfile;
import cd.go.contrib.elasticagent.ElasticAgentProfile;
import cd.go.contrib.elasticagent.PluginSettings;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateConfigurationRequestTest {

    @Test
    public void shouldCreateMigrationConfigRequestFromRequestBody() {
        String requestBody = "{" +
                "    \"plugin_settings\":{" +
                "        \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "        \"kubernetes_cluster_url\":\"https://my.kubernetes-cluster.com\", " +
                "        \"kubernetes_cluster_ca_cert\":\"ca-cert\"" +
                "    }," +
                "    \"cluster_profiles\":[" +
                "        {" +
                "            \"id\":\"cluster_profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"properties\":{" +
                "                \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "                \"kubernetes_cluster_url\":\"https://my.kubernetes-cluster.com\", " +
                "                \"kubernetes_cluster_ca_cert\":\"ca-cert\"" +
                "            }" +
                "         }" +
                "    ]," +
                "    \"elastic_agent_profiles\":[" +
                "        {" +
                "            \"id\":\"profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"cluster_profile_id\":\"cluster_profile_id\"," +
                "            \"properties\":{" +
                "                \"some_key\":\"some_value\"," +
                "                \"some_key2\":\"some_value2\"" +
                "            }" +
                "        }" +
                "    ]" +
                "}\n";

        MigrateConfigurationRequest request = MigrateConfigurationRequest.fromJSON(requestBody);

        PluginSettings pluginSettings = new PluginSettings("https://127.0.0.1:8154/go", "https://my.kubernetes-cluster.com", "ca-cert");

        ClusterProfile clusterProfile = new ClusterProfile();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId("plugin_id");
        clusterProfile.setClusterProfileProperties(pluginSettings);

        ElasticAgentProfile elasticAgentProfile = new ElasticAgentProfile();
        elasticAgentProfile.setId("profile_id");
        elasticAgentProfile.setPluginId("plugin_id");
        elasticAgentProfile.setClusterProfileId("cluster_profile_id");
        HashMap<String, String> properties = new HashMap<>();
        properties.put("some_key", "some_value");
        properties.put("some_key2", "some_value2");
        elasticAgentProfile.setProperties(properties);

        assertThat(pluginSettings).isEqualTo(request.getPluginSettings());
        assertThat(List.of(clusterProfile)).isEqualTo(request.getClusterProfiles());
        assertThat(List.of(elasticAgentProfile)).isEqualTo(request.getElasticAgentProfiles());
    }

    @Test
    public void shouldCreateMigrationConfigRequestWhenNoConfigurationsAreSpecified() {
        String requestBody = "{" +
                "    \"plugin_settings\":{}," +
                "    \"cluster_profiles\":[]," +
                "    \"elastic_agent_profiles\":[]" +
                "}\n";

        MigrateConfigurationRequest request = MigrateConfigurationRequest.fromJSON(requestBody);

        assertThat(new PluginSettings()).isEqualTo(request.getPluginSettings());
        assertThat(List.of()).isEqualTo(request.getClusterProfiles());
        assertThat(List.of()).isEqualTo(request.getElasticAgentProfiles());
    }

    @Test
    public void shouldSerializeToJSONFromMigrationConfigRequest() throws JSONException {
        PluginSettings pluginSettings = new PluginSettings("https://127.0.0.1:8154/go", "https://my.kubernetes-cluster.com", "ca-cert");

        ClusterProfile clusterProfile = new ClusterProfile();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId("plugin_id");
        clusterProfile.setClusterProfileProperties(pluginSettings);

        ElasticAgentProfile elasticAgentProfile = new ElasticAgentProfile();
        elasticAgentProfile.setId("profile_id");
        elasticAgentProfile.setPluginId("plugin_id");
        elasticAgentProfile.setClusterProfileId("cluster_profile_id");
        HashMap<String, String> properties = new HashMap<>();
        properties.put("some_key", "some_value");
        properties.put("some_key2", "some_value2");
        elasticAgentProfile.setProperties(properties);

        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, List.of(clusterProfile), List.of(elasticAgentProfile));

        String actual = request.toJSON();

        String expected = "{" +
                "    \"plugin_settings\":{" +
                "                \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "                \"kubernetes_cluster_url\":\"https://my.kubernetes-cluster.com\", " +
                "                \"kubernetes_cluster_ca_cert\":\"ca-cert\"" +
                "    }," +
                "    \"cluster_profiles\":[" +
                "        {" +
                "            \"id\":\"cluster_profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"properties\":{" +
                "                \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "                \"kubernetes_cluster_url\":\"https://my.kubernetes-cluster.com\", " +
                "                \"kubernetes_cluster_ca_cert\":\"ca-cert\"" +
                "            }" +
                "         }" +
                "    ]," +
                "    \"elastic_agent_profiles\":[" +
                "        {" +
                "            \"id\":\"profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"cluster_profile_id\":\"cluster_profile_id\"," +
                "            \"properties\":{" +
                "                \"some_key\":\"some_value\"," +
                "                \"some_key2\":\"some_value2\"" +
                "            }" +
                "        }" +
                "    ]" +
                "}\n";

        JSONAssert.assertEquals(expected, actual, false);
    }
}
