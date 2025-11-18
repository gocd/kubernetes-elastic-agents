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

import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagent.requests.ShouldAssignWorkRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class ClusterProfilePropertiesTest {
    @Test
    public void shouldGenerateSameUUIDForClusterProfileProperties() {
        HashMap<String, String> clusterProfileConfigurations = new HashMap<>();
        clusterProfileConfigurations.put("go_server_url", "go-server-url");
        ClusterProfileProperties profileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);

        assertThat(profileProperties.uuid()).isEqualTo(profileProperties.uuid());
    }

    @Test
    public void shouldGenerateSameUUIDForClusterProfilePropertiesEvenWithDifferentRequests() {
        String createAgentRequestJSON = """
                {
                  "auto_register_key": "secret-key",
                  "elastic_agent_profile_properties": {
                    "key1": "value1",
                    "key2": "value2"
                  },
                  "cluster_profile_properties": {
                    "go_server_url": "go-server-url"
                  },
                  "environment": "prod",
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

        CreateAgentRequest createAgentRequest = CreateAgentRequest.fromJSON(createAgentRequestJSON);

        String shouldAssignWorkJSON = """
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

        ShouldAssignWorkRequest shouldAssignWorkRequest = ShouldAssignWorkRequest.fromJSON(shouldAssignWorkJSON);
        assertThat(createAgentRequest.clusterProfileProperties().uuid()).isEqualTo(shouldAssignWorkRequest.clusterProfileProperties().uuid());
    }
}
