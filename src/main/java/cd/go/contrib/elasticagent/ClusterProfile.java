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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Objects;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class ClusterProfile {
    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("plugin_id")
    private String pluginId;

    @Expose
    @SerializedName("properties")
    private ClusterProfileProperties clusterProfileProperties;


    public ClusterProfile() {
    }

    public ClusterProfile(String id, String pluginId, PluginSettings pluginSettings) {
        this.id = id;
        this.pluginId = pluginId;
        setClusterProfileProperties(pluginSettings);
    }

    public static ClusterProfile fromJSON(String json) {
        return GSON.fromJson(json, ClusterProfile.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterProfile that = (ClusterProfile) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(clusterProfileProperties, that.clusterProfileProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, clusterProfileProperties);
    }

    @Override
    public String toString() {
        return "ClusterProfile{" +
                "id='" + id + '\'' +
                ", pluginId='" + pluginId + '\'' +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getPluginId() {
        return pluginId;
    }

    public ClusterProfileProperties getClusterProfileProperties() {
        return clusterProfileProperties;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public void setClusterProfileProperties(ClusterProfileProperties clusterProfileProperties) {
        this.clusterProfileProperties = clusterProfileProperties;
    }

    public void setClusterProfileProperties(PluginSettings pluginSettings) {
        this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(GSON.fromJson(GSON.toJson(pluginSettings), HashMap.class));
    }
}
