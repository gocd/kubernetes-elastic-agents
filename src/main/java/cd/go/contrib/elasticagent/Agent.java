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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

/**
 * Represents an Agent.
 * See https://plugin-api.go.cd/current/elastic-agents/#the-elastic-agent-object for more details.
 */
public class Agent {

    public static final Type AGENT_METADATA_LIST_TYPE = new TypeToken<ArrayList<Agent>>() {
    }.getType();
    @Expose
    @SerializedName("agent_id")
    private String agentId;
    @Expose
    @SerializedName("agent_state")
    private AgentState agentState;
    @Expose
    @SerializedName("build_state")
    private BuildState buildState;
    @Expose
    @SerializedName("config_state")
    private ConfigState configState;

    // Public constructor needed for JSON de-serialization
    public Agent() {
    }

    // Used in tests
    public Agent(String agentId, AgentState agentState, BuildState buildState, ConfigState configState) {
        this.agentId = agentId;
        this.agentState = agentState;
        this.buildState = buildState;
        this.configState = configState;
    }

    public static List<Agent> fromJSONArray(String json) {
        return GSON.fromJson(json, AGENT_METADATA_LIST_TYPE);
    }

    public static String toJSONArray(Collection<Agent> metadata) {
        return GSON.toJson(metadata);
    }

    public String elasticAgentId() {
        return agentId;
    }

    public void setElasticAgentId(String agentId) {
        this.agentId = agentId;
    }

    public AgentState agentState() {
        return agentState;
    }

    public BuildState buildState() {
        return buildState;
    }

    public ConfigState configState() {
        return configState;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "agentId='" + agentId + '\'' +
                ", agentState='" + agentState + '\'' +
                ", buildState='" + buildState + '\'' +
                ", configState='" + configState + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        if (agentId != null ? !agentId.equals(agent.agentId) : agent.agentId != null) return false;
        if (agentState != agent.agentState) return false;
        if (buildState != agent.buildState) return false;
        return configState == agent.configState;

    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (agentState != null ? agentState.hashCode() : 0);
        result = 31 * result + (buildState != null ? buildState.hashCode() : 0);
        result = 31 * result + (configState != null ? configState.hashCode() : 0);
        return result;
    }

    public enum AgentState {
        Idle, Building, LostContact, Missing, Unknown
    }

    public enum BuildState {
        Idle, Building, Cancelled, Unknown
    }

    public enum ConfigState {
        Pending, Enabled, Disabled
    }
}
