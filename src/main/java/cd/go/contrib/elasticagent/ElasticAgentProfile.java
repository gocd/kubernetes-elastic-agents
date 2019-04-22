/*
 * Copyright 2019 ThoughtWorks, Inc.
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

public class ElasticAgentProfile {
    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("plugin_id")
    private String pluginId;

    @Expose
    @SerializedName("cluster_profile_id")
    private String clusterProfileId;

    @Expose
    @SerializedName("properties")
    private HashMap<String, String> properties;

    public static ElasticAgentProfile fromJSON(String json) {
        return GSON.fromJson(json, ElasticAgentProfile.class);
    }

    public String getId() {
        return id;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getClusterProfileId() {
        return clusterProfileId;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public void setClusterProfileId(String clusterProfileId) {
        this.clusterProfileId = clusterProfileId;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElasticAgentProfile that = (ElasticAgentProfile) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(clusterProfileId, that.clusterProfileId) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, clusterProfileId, properties);
    }

    @Override
    public String toString() {
        return "ElasticAgentProfile{" +
                "id='" + id + '\'' +
                ", pluginId='" + pluginId + '\'' +
                ", clusterProfileId='" + clusterProfileId + '\'' +
                ", properties=" + properties +
                '}';
    }
}
