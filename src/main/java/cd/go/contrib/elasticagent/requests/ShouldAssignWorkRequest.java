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

import cd.go.contrib.elasticagent.Agent;
import cd.go.contrib.elasticagent.AgentInstances;
import cd.go.contrib.elasticagent.Request;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.executors.ShouldAssignWorkRequestExecutor;
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
    @SerializedName("properties")
    private Map<String, String> properties;

    public ShouldAssignWorkRequest(Agent agent, String environment, Map<String, String> properties) {
        this.agent = agent;
        this.environment = environment;
        this.properties = properties;
    }

    public ShouldAssignWorkRequest() {
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

    public Map<String, String> properties() {
        return properties;
    }

    public RequestExecutor executor(AgentInstances agentInstances) {
        return new ShouldAssignWorkRequestExecutor(this, agentInstances);
    }
}
