/*
 * Copyright 2018 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagent.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;


public class GetCapabilitiesExecutorTest {

    @Test
    public void shouldSupportStatusReport() throws Exception {
        final GoPluginApiResponse response = new GetCapabilitiesExecutor().execute();

        assertThat(response.responseCode()).isEqualTo(200);
        String expected = "{" +
                "  \"supports_plugin_status_report\":false," +
                "  \"supports_cluster_status_report\":true," +
                "  \"supports_agent_status_report\":true" +
                "}";

        JSONAssert.assertEquals(expected, response.responseBody(), true);
    }
}
