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
import cd.go.contrib.elasticagent.JobIdentifierMother;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class AgentStatusReportRequestTest {
    @Test
    public void shouldDeserializeFromJSON() {
        JsonObject jobIdentifierJson = JobIdentifierMother.getJson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("elastic_agent_id", "some-id");
        jsonObject.add("job_identifier", jobIdentifierJson);

        AgentStatusReportRequest agentStatusReportRequest = AgentStatusReportRequest.fromJSON(jsonObject.toString());

        AgentStatusReportRequest expected = new AgentStatusReportRequest("some-id", JobIdentifierMother.get(), null);
        assertThat(agentStatusReportRequest).isEqualTo(expected);
    }

    @Test
    public void shouldDeserializeFromJSONWithClusterProfile() {
        JsonObject jobIdentifierJson = JobIdentifierMother.getJson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("elastic_agent_id", "some-id");
        jsonObject.add("job_identifier", jobIdentifierJson);
        JsonObject clusterJSON = new JsonObject();
        clusterJSON.addProperty("go_server_url", "https://foo.com/go");
        jsonObject.add("cluster_profile_properties", clusterJSON);

        AgentStatusReportRequest agentStatusReportRequest = AgentStatusReportRequest.fromJSON(jsonObject.toString());

        HashMap<String, String> clusterProfileConfigurations = new HashMap<>();
        clusterProfileConfigurations.put("go_server_url", "https://foo.com/go");
        ClusterProfileProperties expectedClusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);

        AgentStatusReportRequest expected = new AgentStatusReportRequest("some-id", JobIdentifierMother.get(), expectedClusterProfileProperties);
        assertThat(agentStatusReportRequest).isEqualTo(expected);
    }

}
