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

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


public class ClusterStatusReportRequestTest {
    @Test
    public void shouldDeserializeFromJSON() {
        JsonObject jsonObject = new JsonObject();
        JsonObject clusterJSON = new JsonObject();
        clusterJSON.addProperty("go_server_url", "https://go-server/go");
        jsonObject.add("cluster_profile_properties", clusterJSON);

        ClusterStatusReportRequest clusterStatusReportRequest = ClusterStatusReportRequest.fromJSON(jsonObject.toString());

        ClusterStatusReportRequest expected = new ClusterStatusReportRequest(Collections.singletonMap("go_server_url", "https://go-server/go"));
        assertThat(clusterStatusReportRequest).isEqualTo(expected);
    }
}
