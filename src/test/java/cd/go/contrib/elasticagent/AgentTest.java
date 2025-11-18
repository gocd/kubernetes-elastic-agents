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

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class AgentTest {

    @Test
    public void shouldSerializeToJSON() throws Exception {
        Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
        String agentsJSON = Agent.toJSONArray(List.of(agent));

        JSONAssert.assertEquals("[{\"agent_id\":\"eeb9e0eb-1f12-4366-a5a5-59011810273b\",\"agent_state\":\"Building\",\"build_state\":\"Cancelled\",\"config_state\":\"Disabled\"}]", agentsJSON, true);
    }

    @Test
    public void shouldDeserializeFromJSON() {
        List<Agent> agents = Agent.fromJSONArray("[{\"agent_id\":\"eeb9e0eb-1f12-4366-a5a5-59011810273b\",\"agent_state\":\"Building\",\"build_state\":\"Cancelled\",\"config_state\":\"Disabled\"}]");
        assertThat(agents).hasSize(1);

        Agent agent = agents.get(0);

        assertThat(agent.elasticAgentId()).isEqualTo("eeb9e0eb-1f12-4366-a5a5-59011810273b");
        assertThat(agent.agentState()).isEqualTo(Agent.AgentState.Building);
        assertThat(agent.buildState()).isEqualTo(Agent.BuildState.Cancelled);
        assertThat(agent.configState()).isEqualTo(Agent.ConfigState.Disabled);
    }

    @Test
    public void agentsWithSameAttributesShouldBeEqual() {
        Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
        Agent agent2 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

        assertEquals(agent1, agent2);
    }

    @Test
    public void agentShouldEqualItself() {
        Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

        assertEquals(agent, agent);
    }

    @Test
    public void agentShouldNotEqualAnotherAgentWithDifferentAttributes() {
        Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

        assertNotEquals(new Agent(), agent);
    }

    @Test
    public void agentsWithSameAttributesShareSameHashCode() {
        Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
        Agent agent2 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

        assertThat(agent1.hashCode()).isEqualTo(agent2.hashCode());
    }

    @Test
    public void agentsWithDifferentAttributesDoNotShareSameHashCode() {
        Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
        Agent agent2 = new Agent();

        assertThat(agent1.hashCode()).isNotEqualTo(agent2.hashCode());
    }
}
