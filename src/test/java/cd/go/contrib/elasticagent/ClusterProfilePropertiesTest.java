/*
 * Copyright 2019 ThoughtWorks, Inc.
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
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ClusterProfilePropertiesTest {
    @Test
    public void shouldGenerateSameUUIDForClusterProfileProperties() {
        HashMap<String, String> clusterProfileConfigurations = new HashMap<>();
        clusterProfileConfigurations.put("go_server_url", "go-server-url");
        ClusterProfileProperties profileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);

        assertThat(profileProperties.uuid(), is(profileProperties.uuid()));
    }

    @Test
    public void shouldGenerateSameUUIDForClusterProfilePropertiesEvenWithDifferentRequests() {
        String createAgentRequestJSON = "{\n" +
                "  \"auto_register_key\": \"secret-key\",\n" +
                "  \"elastic_agent_profile_properties\": {\n" +
                "    \"key1\": \"value1\",\n" +
                "    \"key2\": \"value2\"\n" +
                "  },\n" +
                "  \"cluster_profile_properties\": {\n" +
                "    \"go_server_url\": \"go-server-url\"\n" +
                "  },\n" +
                "  \"environment\": \"prod\",\n" +
                "  \"job_identifier\": {\n" +
                "    \"pipeline_name\": \"test-pipeline\",\n" +
                "    \"pipeline_counter\": 1,\n" +
                "    \"pipeline_label\": \"Test Pipeline\",\n" +
                "    \"stage_name\": \"test-stage\",\n" +
                "    \"stage_counter\": \"1\",\n" +
                "    \"job_name\": \"test-job\",\n" +
                "    \"job_id\": 100\n" +
                "  }\n" +
                "}";

        CreateAgentRequest createAgentRequest = CreateAgentRequest.fromJSON(createAgentRequestJSON);

        String shouldAssignWorkJSON = "{\n" +
                "  \"environment\": \"prod\",\n" +
                "  \"agent\": {\n" +
                "    \"agent_id\": \"42\",\n" +
                "    \"agent_state\": \"Idle\",\n" +
                "    \"build_state\": \"Idle\",\n" +
                "    \"config_state\": \"Enabled\"\n" +
                "  },\n" +
                "  \"elastic_agent_profile_properties\": {\n" +
                "    \"key1\": \"value1\",\n" +
                "    \"key2\": \"value2\"\n" +
                "  },\n" +
                "  \"cluster_profile_properties\": {\n" +
                "    \"go_server_url\": \"go-server-url\"\n" +
                "  },\n" +
                "  \"job_identifier\": {\n" +
                "    \"pipeline_name\": \"test-pipeline\",\n" +
                "    \"pipeline_counter\": 1,\n" +
                "    \"pipeline_label\": \"Test Pipeline\",\n" +
                "    \"stage_name\": \"test-stage\",\n" +
                "    \"stage_counter\": \"1\",\n" +
                "    \"job_name\": \"test-job\",\n" +
                "    \"job_id\": 100\n" +
                "  }\n" +
                "}";

        ShouldAssignWorkRequest shouldAssignWorkRequest = ShouldAssignWorkRequest.fromJSON(shouldAssignWorkJSON);
        assertThat(createAgentRequest.clusterProfileProperties().uuid(), is(shouldAssignWorkRequest.clusterProfileProperties().uuid()));
    }
}
