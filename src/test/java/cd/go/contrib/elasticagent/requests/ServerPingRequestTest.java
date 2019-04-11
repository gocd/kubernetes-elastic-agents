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

package cd.go.contrib.elasticagent.requests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ServerPingRequestTest {
    @Test
    public void shouldDeserializeFromJSON() {
        JsonObject clusterJSON = new JsonObject();
        clusterJSON.addProperty("go_server_url", "https://go-server/go");

        JsonArray allClustersArray = new JsonArray();
        allClustersArray.add(clusterJSON);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("all_cluster_profile_properties", allClustersArray);

        ServerPingRequest serverPingRequest = ServerPingRequest.fromJSON(jsonObject.toString());

        HashMap<String, String> clusterProfileConfigurations = new HashMap<>();
        clusterProfileConfigurations.put("go_server_url", "https://go-server/go");
        ServerPingRequest expected = new ServerPingRequest(Arrays.asList(clusterProfileConfigurations));

        assertThat(serverPingRequest, is(expected));
    }
}
