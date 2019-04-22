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
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.executors.ClusterStatusReportExecutor;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class ClusterStatusReportRequest {
    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public ClusterStatusReportRequest() {
    }

    public ClusterStatusReportRequest(Map<String, String> clusterProfileConfigurations) {
        this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);
    }

    public ClusterProfileProperties clusterProfileProperties() {
        return clusterProfileProperties;
    }

    public static ClusterStatusReportRequest fromJSON(String json) {
        return GSON.fromJson(json, ClusterStatusReportRequest.class);
    }

    public ClusterStatusReportExecutor executor() throws IOException {
        return new ClusterStatusReportExecutor(this, PluginStatusReportViewBuilder.instance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterStatusReportRequest that = (ClusterStatusReportRequest) o;
        return Objects.equals(clusterProfileProperties, that.clusterProfileProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterProfileProperties);
    }

    @Override
    public String toString() {
        return "ClusterStatusReportRequest{" +
                "clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }
}
