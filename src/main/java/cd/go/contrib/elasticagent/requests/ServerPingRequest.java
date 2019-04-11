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

package cd.go.contrib.elasticagent.requests;

import cd.go.contrib.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagent.KubernetesAgentInstances;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.executors.ServerPingRequestExecutor;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class ServerPingRequest {
    @Expose
    @SerializedName("all_cluster_profile_properties")
    private List<ClusterProfileProperties> allClusterProfileProperties = new ArrayList<>();

    public ServerPingRequest() {
    }

    public ServerPingRequest(List<Map<String, String>> allClusterProfileProperties) {
        this.allClusterProfileProperties = allClusterProfileProperties.stream()
                .map(clusterProfile -> ClusterProfileProperties.fromConfiguration(clusterProfile))
                .collect(Collectors.toList());
    }

    public List<ClusterProfileProperties> allClusterProfileProperties() {
        return allClusterProfileProperties;
    }

    public static ServerPingRequest fromJSON(String json) {
        return GSON.fromJson(json, ServerPingRequest.class);
    }

    @Override
    public String toString() {
        return "ServerPingRequest{" +
                "allClusterProfileProperties=" + allClusterProfileProperties +
                '}';
    }

    public ServerPingRequestExecutor executor(Map<String, KubernetesAgentInstances> clusterSpecificAgentInstances, PluginRequest pluginRequest) {
        return new ServerPingRequestExecutor(this, clusterSpecificAgentInstances, pluginRequest);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerPingRequest that = (ServerPingRequest) o;
        return Objects.equals(allClusterProfileProperties, that.allClusterProfileProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allClusterProfileProperties);
    }
}
