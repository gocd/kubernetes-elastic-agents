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


import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.*;

/**
 * Represents a map of {@link Agent#elasticAgentId()} to the {@link Agent} for easy lookups
 */
public class Agents {

    // Filter for agents that can be disabled safely
    private static final Predicate<Agent> AGENT_IDLE_PREDICATE = metadata -> {
        Agent.AgentState agentState = metadata.agentState();
        return metadata.configState().equals(Agent.ConfigState.Enabled) && (agentState.equals(Agent.AgentState.Idle) || agentState.equals(Agent.AgentState.Missing) || agentState.equals(Agent.AgentState.LostContact));
    };
    // Filter for agents that can be terminated safely
    private static final Predicate<Agent> AGENT_DISABLED_PREDICATE = metadata -> {
        Agent.AgentState agentState = metadata.agentState();
        return metadata.configState().equals(Agent.ConfigState.Disabled) && (agentState.equals(Agent.AgentState.Idle) || agentState.equals(Agent.AgentState.Missing) || agentState.equals(Agent.AgentState.LostContact));
    };
    private final Map<String, Agent> agents = new HashMap<>();

    public Agents() {
    }

    public Agents(Collection<Agent> toCopy) {
        addAll(toCopy);
    }

    public void addAll(Collection<Agent> toAdd) {
        for (Agent agent : toAdd) {
            add(agent);
        }
    }

    public void addAll(Agents agents) {
        addAll(agents.agents());
    }

    public Collection<Agent> findInstancesToDisable() {
        return FluentIterable.from(agents.values()).filter(AGENT_IDLE_PREDICATE).toList();
    }

    public Collection<Agent> findInstancesToTerminate() {
        return FluentIterable.from(agents.values()).filter(AGENT_DISABLED_PREDICATE).toList();
    }

    public Set<String> agentIds() {
        return new LinkedHashSet<>(agents.keySet());
    }

    public boolean containsAgentWithId(String agentId) {
        return agents.containsKey(agentId);
    }

    public Collection<Agent> agents() {
        return new ArrayList<>(agents.values());
    }

    public void add(Agent agent) {
        agents.put(agent.elasticAgentId(), agent);
    }

}
