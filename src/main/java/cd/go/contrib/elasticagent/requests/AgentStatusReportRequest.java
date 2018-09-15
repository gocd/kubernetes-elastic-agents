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

package cd.go.contrib.elasticagent.requests;

import cd.go.contrib.elasticagent.AgentInstances;
import cd.go.contrib.elasticagent.KubernetesInstance;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.Request;
import cd.go.contrib.elasticagent.executors.AgentStatusReportExecutor;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

import java.util.Map;

/**
 * Represents the {@link Request#REQUEST_SHOULD_ASSIGN_WORK} message.
 */
public class AgentStatusReportRequest {
    @Expose
    @SerializedName("elastic_agent_id")
    private String elasticAgentId;
    
    @Expose
    @SerializedName("properties")
    private Map<String, String> properties;

    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    public AgentStatusReportRequest() {
    }

    public AgentStatusReportRequest(String elasticAgentId, Map<String, String> properties,JobIdentifier jobIdentifier) {
        this.elasticAgentId = elasticAgentId;
        this.properties = properties;
        this.jobIdentifier = jobIdentifier;
    }

    public static AgentStatusReportRequest fromJSON(String json) {
    	AgentStatusReportRequest request = GSON.fromJson(json, AgentStatusReportRequest.class);
    	System.out.println("AgentStatusReportRequest"+json);
        return request;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }
    

    public Map<String, String> properties() {
		return properties;
	}

	public AgentStatusReportExecutor executor(AgentInstances<KubernetesInstance> agentInstances,PluginRequest pluginRequest) {
        return new AgentStatusReportExecutor(agentInstances,this, pluginRequest);
    }
}
