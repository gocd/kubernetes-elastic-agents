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

import cd.go.contrib.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagent.Request;
import cd.go.contrib.elasticagent.executors.AgentStatusReportExecutor;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

/**
 * Represents the {@link Request#REQUEST_SHOULD_ASSIGN_WORK} message.
 */
public class AgentStatusReportRequest {
    @Expose
    @SerializedName("elastic_agent_id")
    private String elasticAgentId;

    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public AgentStatusReportRequest() {
    }

    public AgentStatusReportRequest(String elasticAgentId, JobIdentifier jobIdentifier, ClusterProfileProperties clusterProfileProperties) {
        this.elasticAgentId = elasticAgentId;
        this.jobIdentifier = jobIdentifier;
        this.clusterProfileProperties = clusterProfileProperties;
    }

    public static AgentStatusReportRequest fromJSON(String json) {
        return GSON.fromJson(json, AgentStatusReportRequest.class);
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public ClusterProfileProperties clusterProfileProperties() {
        return clusterProfileProperties;
    }

    public AgentStatusReportExecutor executor() {
        return new AgentStatusReportExecutor(this);
    }

    @Override
    public String toString() {
        return "AgentStatusReportRequest{" +
                "elasticAgentId='" + elasticAgentId + '\'' +
                ", jobIdentifier=" + jobIdentifier +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentStatusReportRequest that = (AgentStatusReportRequest) o;
        return Objects.equals(elasticAgentId, that.elasticAgentId) &&
                Objects.equals(jobIdentifier, that.jobIdentifier) &&
                Objects.equals(clusterProfileProperties, that.clusterProfileProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elasticAgentId, jobIdentifier, clusterProfileProperties);
    }
}
