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

package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.model.Metadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetClusterProfileMetadataExecutorTest {
    @Test
    public void shouldSerializeAllFields() {
        GoPluginApiResponse response = new GetClusterProfileMetadataExecutor().execute();
        List<Metadata> list = new Gson().fromJson(response.responseBody(), new TypeToken<List<Metadata>>() {
        }.getType());
        assertEquals(list.size(), GetClusterProfileMetadataExecutor.FIELDS.size());
    }

    @Test
    public void assertJsonStructure() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileMetadataExecutor().execute();

        assertThat(response.responseCode()).isEqualTo(200);
        String expectedJSON = """
                [
                  {
                    "key": "go_server_url",
                    "metadata": {
                      "required": true,
                      "secure": false
                    }
                  },
                  {
                    "key": "auto_register_timeout",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "pending_pods_count",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "kubernetes_cluster_url",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "namespace",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "security_token",
                    "metadata": {
                      "required": false,
                      "secure": true
                    }
                  },
                  {
                    "key": "kubernetes_cluster_ca_cert",
                    "metadata": {
                      "required": false,
                      "secure": true
                    }
                  },
                  {
                    "key": "cluster_request_timeout",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "enable_agent_reuse",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  }  
                ]""";

        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
