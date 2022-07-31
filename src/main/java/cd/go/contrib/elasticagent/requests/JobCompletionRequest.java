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

import cd.go.contrib.elasticagent.*;
import cd.go.contrib.elasticagent.executors.JobCompletionRequestExecutor;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class JobCompletionRequest {
    @Expose
    @SerializedName("elastic_agent_id")
    private String elasticAgentId;
    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("elastic_agent_profile_properties")
    private Map<String, String> properties;

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public JobCompletionRequest() {
    }

    public JobCompletionRequest(String elasticAgentId, JobIdentifier jobIdentifier, Map<String, String> properties, ClusterProfileProperties clusterProfileProperties) {
        this.elasticAgentId = elasticAgentId;
        this.jobIdentifier = jobIdentifier;
        this.properties = properties;
        this.clusterProfileProperties = clusterProfileProperties;
    }

    public static JobCompletionRequest fromJSON(String json) {
        return GSON.fromJson(json, JobCompletionRequest.class);
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public RequestExecutor executor(AgentInstances<KubernetesInstance> agentInstances, PluginRequest pluginRequest) {
        return new JobCompletionRequestExecutor(this, agentInstances, pluginRequest);
    }

    public Map<String, String> properties() {
        return properties;
    }

    public ClusterProfileProperties clusterProfileProperties() {
        return clusterProfileProperties;
    }

    @Override
    public String toString() {
        return "JobCompletionRequest{" +
                "elasticAgentId='" + elasticAgentId + '\'' +
                ", jobIdentifier=" + jobIdentifier +
                ", properties=" + properties +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }
}
