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

import cd.go.contrib.elasticagent.Agent;
import cd.go.contrib.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ShouldAssignWorkRequestTest {

    @Test
    public void shouldDeserializeFromJSON() {
        String json = """
                {
                  "environment": "prod",
                  "agent": {
                    "agent_id": "42",
                    "agent_state": "Idle",
                    "build_state": "Idle",
                    "config_state": "Enabled"
                  },
                  "elastic_agent_profile_properties": {
                    "key1": "value1",
                    "key2": "value2"
                  },
                  "cluster_profile_properties": {
                    "go_server_url": "go-server-url"
                  },
                  "job_identifier": {
                    "pipeline_name": "test-pipeline",
                    "pipeline_counter": 1,
                    "pipeline_label": "Test Pipeline",
                    "stage_name": "test-stage",
                    "stage_counter": "1",
                    "job_name": "test-job",
                    "job_id": 100
                  }
                }""";

        ShouldAssignWorkRequest request = ShouldAssignWorkRequest.fromJSON(json);
        assertThat(request.environment()).isEqualTo("prod");
        assertThat(request.agent()).isEqualTo(new Agent("42", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled));
        HashMap<String, String> expectedElasticAgentProperties = new HashMap<>();
        expectedElasticAgentProperties.put("key1", "value1");
        expectedElasticAgentProperties.put("key2", "value2");
        assertThat(request.elasticProfileProperties()).isEqualTo(expectedElasticAgentProperties);

        HashMap<String, String> clusterProfileConfigurations = new HashMap<>();
        clusterProfileConfigurations.put("go_server_url", "go-server-url");
        ClusterProfileProperties expectedClusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);
        assertThat(request.clusterProfileProperties()).isEqualTo(expectedClusterProfileProperties);

        JobIdentifier expectedJobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
        JobIdentifier actualJobIdentifier = request.jobIdentifier();

        assertThat(actualJobIdentifier).isEqualTo(expectedJobIdentifier);
    }
}
