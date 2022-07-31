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

import cd.go.contrib.elasticagent.ClusterProfile;
import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.ElasticAgentProfile;
import cd.go.contrib.elasticagent.PluginSettings;
import cd.go.contrib.elasticagent.requests.MigrateConfigurationRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateConfigurationRequestExecutorTest {
    private PluginSettings pluginSettings;
    private ClusterProfile clusterProfile;
    private ElasticAgentProfile elasticAgentProfile;
    private HashMap<String, String> properties;

    @BeforeEach
    public void setUp() {
        pluginSettings = new PluginSettings("https://127.0.0.1:8154/go", "https://my.kubernetes-cluster.com", "ca-cert");

        clusterProfile = new ClusterProfile();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId(Constants.PLUGIN_ID);
        clusterProfile.setClusterProfileProperties(pluginSettings);

        elasticAgentProfile = new ElasticAgentProfile();
        elasticAgentProfile.setId("profile_id");
        elasticAgentProfile.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile.setClusterProfileId("cluster_profile_id");
        properties = new HashMap<>();
        properties.put("some_key", "some_value");
        properties.put("some_key2", "some_value2");
        elasticAgentProfile.setProperties(properties);
    }

    @Test
    public void shouldNotMigrateConfigWhenNoPluginSettingsAreConfigured() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(new PluginSettings(), Collections.singletonList(clusterProfile), Collections.singletonList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(new PluginSettings());
        assertThat(responseObject.getClusterProfiles()).isEqualTo(Collections.singletonList(clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(Collections.singletonList(elasticAgentProfile));
    }

    @Test
    public void shouldNotMigrateConfigWhenClusterProfileIsAlreadyConfigured() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Collections.singletonList(clusterProfile), Collections.singletonList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);
        assertThat(responseObject.getClusterProfiles()).isEqualTo(Collections.singletonList(clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(Collections.singletonList(elasticAgentProfile));
    }

    @Test
    public void shouldPopulateNoOpClusterProfileWithPluginSettingsConfigurations() throws Exception {
        ClusterProfile emptyClusterProfile = new ClusterProfile(String.format("no-op-cluster-for-%s", Constants.PLUGIN_ID), Constants.PLUGIN_ID, new PluginSettings());
        elasticAgentProfile.setClusterProfileId(emptyClusterProfile.getId());
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Collections.singletonList(emptyClusterProfile), Collections.singletonList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);
        List<ClusterProfile> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);

        assertThat(actualClusterProfile.getId()).isNotEqualTo(String.format("no-op-cluster-for-%s", Constants.PLUGIN_ID));
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual).isEqualTo(Collections.singletonList(this.clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(Collections.singletonList(elasticAgentProfile));

        assertThat(elasticAgentProfile.getClusterProfileId()).isEqualTo(actualClusterProfile.getId());
    }

    @Test
    public void shouldPopulateNoOpClusterProfileWithPluginSettingsConfigurations_WithoutChangingClusterProfileIdIfItsNotNoOp() throws Exception {
        String clusterProfileId = "i-renamed-no-op-cluster-to-something-else";
        ClusterProfile emptyClusterProfile = new ClusterProfile(clusterProfileId, Constants.PLUGIN_ID, new PluginSettings());
        elasticAgentProfile.setClusterProfileId(emptyClusterProfile.getId());
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Collections.singletonList(emptyClusterProfile), Collections.singletonList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);
        List<ClusterProfile> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);

        assertThat(actualClusterProfile.getId()).isEqualTo(clusterProfileId);
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual).isEqualTo(Collections.singletonList(this.clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(Collections.singletonList(elasticAgentProfile));

        assertThat(elasticAgentProfile.getClusterProfileId()).isEqualTo(clusterProfileId);
    }

    @Test
    public void shouldMigratePluginSettingsToClusterProfile_WhenNoElasticAgentProfilesAreConfigured() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Collections.emptyList(), Collections.emptyList());
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);
        List<ClusterProfile> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual).isEqualTo(Collections.singletonList(this.clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void ShouldMigrateEmptyClusterProfiles_WhenMultipleEmptyClusterProfilesExists() throws Exception {
        ClusterProfile emptyCluster1 = new ClusterProfile("cluster_profile_1", Constants.PLUGIN_ID, new PluginSettings());
        ClusterProfile emptyCluster2 = new ClusterProfile("cluster_profile_2", Constants.PLUGIN_ID, new PluginSettings());

        ElasticAgentProfile elasticAgentProfile1 = new ElasticAgentProfile();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(emptyCluster1.getId());

        ElasticAgentProfile elasticAgentProfile2 = new ElasticAgentProfile();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(emptyCluster2.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Arrays.asList(emptyCluster1, emptyCluster2), Arrays.asList(elasticAgentProfile1, elasticAgentProfile2));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(0).getId());
        assertThat(responseObject.getClusterProfiles().get(0)).isEqualTo(clusterProfile);

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(1).getId());
        assertThat(responseObject.getClusterProfiles().get(1)).isEqualTo(clusterProfile);

        assertThat(responseObject.getElasticAgentProfiles().get(0).getClusterProfileId()).isEqualTo(emptyCluster1.getId());
        assertThat(responseObject.getElasticAgentProfiles().get(1).getClusterProfileId()).isEqualTo(emptyCluster2.getId());
    }

    @Test
    public void ShouldNotMigrateEmptyAndUnassociatedClusterProfiles() throws Exception {
        ClusterProfile emptyCluster1 = new ClusterProfile("cluster_profile_1", Constants.PLUGIN_ID, new PluginSettings());
        ClusterProfile emptyCluster2 = new ClusterProfile("cluster_profile_2", Constants.PLUGIN_ID, new PluginSettings());

        ElasticAgentProfile elasticAgentProfile1 = new ElasticAgentProfile();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(emptyCluster1.getId());

        ElasticAgentProfile elasticAgentProfile2 = new ElasticAgentProfile();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(emptyCluster1.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Arrays.asList(emptyCluster1, emptyCluster2), Arrays.asList(elasticAgentProfile1, elasticAgentProfile2));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(0).getId());
        assertThat(responseObject.getClusterProfiles().get(0)).isEqualTo(clusterProfile);

        //verify cluster is empty.. not migrated
        assertThat(responseObject.getClusterProfiles().get(1)).isEqualTo(emptyCluster2);

        assertThat(responseObject.getElasticAgentProfiles().get(0).getClusterProfileId()).isEqualTo(emptyCluster1.getId());
        assertThat(responseObject.getElasticAgentProfiles().get(1).getClusterProfileId()).isEqualTo(emptyCluster1.getId());
    }

    @Test
    public void shouldNotMigrateConfigWhenMultipleClusterProfilesAreAlreadyMigrated() throws Exception {
        ClusterProfile cluster1 = new ClusterProfile("cluster_profile_1", Constants.PLUGIN_ID, pluginSettings);
        ClusterProfile cluster2 = new ClusterProfile("cluster_profile_2", Constants.PLUGIN_ID, pluginSettings);

        ElasticAgentProfile elasticAgentProfile1 = new ElasticAgentProfile();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(cluster1.getId());

        ElasticAgentProfile elasticAgentProfile2 = new ElasticAgentProfile();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(cluster2.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Arrays.asList(cluster1, cluster2), Arrays.asList(elasticAgentProfile1, elasticAgentProfile2));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);

        assertThat(responseObject.getClusterProfiles()).isEqualTo(Arrays.asList(cluster1, cluster2));

        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(Arrays.asList(elasticAgentProfile1, elasticAgentProfile2));
    }
}
