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
import cd.go.contrib.elasticagent.model.JobIdentifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAgentRequestTest {

    @Test
    public void shouldDeserializeFromJSON() throws Exception {
        String json = "{\n" +
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

        CreateAgentRequest request = CreateAgentRequest.fromJSON(json);
        assertThat(request.autoRegisterKey()).isEqualTo("secret-key");
        assertThat(request.environment()).isEqualTo("prod");
        HashMap<String, String> expectedElasticAgentProperties = new HashMap<>();
        expectedElasticAgentProperties.put("key1", "value1");
        expectedElasticAgentProperties.put("key2", "value2");
        assertThat(request.properties()).isEqualTo(expectedElasticAgentProperties);

        HashMap<String, String> clusterProfileConfigurations = new HashMap<>();
        clusterProfileConfigurations.put("go_server_url", "go-server-url");
        ClusterProfileProperties expectedClusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);
        assertThat(request.clusterProfileProperties()).isEqualTo(expectedClusterProfileProperties);

        JobIdentifier expectedJobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
        JobIdentifier actualJobIdentifier = request.jobIdentifier();

        assertThat(actualJobIdentifier).isEqualTo(expectedJobIdentifier);
    }
}
