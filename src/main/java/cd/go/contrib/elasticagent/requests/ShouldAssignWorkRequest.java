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
import cd.go.contrib.elasticagent.executors.ShouldAssignWorkRequestExecutor;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

/**
 * Represents the {@link Request#REQUEST_SHOULD_ASSIGN_WORK} message.
 */
public class ShouldAssignWorkRequest {
    @Expose
    @SerializedName("agent")
    private Agent agent;

    @Expose
    @SerializedName("environment")
    private String environment;

    @Expose
    @SerializedName("elastic_agent_profile_properties")
    private Map<String, String> elasticProfileProperties;

    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public ShouldAssignWorkRequest() {
    }

    public ShouldAssignWorkRequest(
            Agent agent,
            String environment,
            Map<String, String> elasticProfileProperties,
            JobIdentifier jobIdentifier,
            ClusterProfileProperties clusterProfileProperties) {
        this.agent = agent;
        this.environment = environment;
        this.elasticProfileProperties = elasticProfileProperties;
        this.jobIdentifier = jobIdentifier;
        this.clusterProfileProperties = clusterProfileProperties;
    }

    public static ShouldAssignWorkRequest fromJSON(String json) {
        return GSON.fromJson(json, ShouldAssignWorkRequest.class);
    }

    public Agent agent() {
        return agent;
    }

    public String environment() {
        return environment;
    }

    public Map<String, String> elasticProfileProperties() {
        return elasticProfileProperties;
    }

    public RequestExecutor executor(AgentInstances<KubernetesInstance> agentInstances) {
        return new ShouldAssignWorkRequestExecutor(this, agentInstances);
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public ClusterProfileProperties clusterProfileProperties() {
        return clusterProfileProperties;
    }

    @Override
    public String toString() {
        return "ShouldAssignWorkRequest{" +
                "agent=" + agent +
                ", environment='" + environment + '\'' +
                ", properties=" + elasticProfileProperties +
                ", jobIdentifier=" + jobIdentifier +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }
}
