/*
 * Copyright 2017 ThoughtWorks, Inc.
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

import cd.go.contrib.elasticagent.requests.ProfileValidateRequest;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.HashMap;
import java.util.Map;

public class ProfileValidateRequestExecutorTest {
    @Test
    public void shouldBarfWhenUnknownKeysArePassed() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("foo", "bar");
        properties.put("PodSpecType", "properties");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"},{\"key\":\"foo\",\"message\":\"Is an unknown property\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeysForConfigProperties() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("PodSpecType", "properties");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeysForPodConfiguration() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("PodSpecType", "yaml");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Pod Configuration must not be blank.\",\"key\":\"PodConfiguration\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidatePodConfigurationWhenSpecifiedAsYaml() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("PodSpecType", "yaml");
        properties.put("PodConfiguration", "this is my invalid fancy pod yaml!!");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Invalid Pod Yaml.\",\"key\":\"PodConfiguration\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldAllowPodYamlConfiguration() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("PodSpecType", "yaml");
        String podYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: pod-name\n" +
                "  labels:\n" +
                "    app: web\n" +
                "spec:\n" +
                "  containers:\n" +
                "    - name: gocd-agent-container\n" +
                "      image: gocd/fancy-agent-image:latest";

        properties.put("PodConfiguration", podYaml);
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldNotAllowPodYamlConfigurationWhenGenerateNameIsSpecified() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("PodSpecType", "yaml");
        String podYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  generateName: pod-name\n" +
                "  labels:\n" +
                "    app: web\n" +
                "spec:\n" +
                "  containers:\n" +
                "    - name: gocd-agent-container\n" +
                "      image: gocd/fancy-agent-image:latest";

        properties.put("PodConfiguration", podYaml);
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Invalid Pod Yaml. generateName field is not supported by GoCD. Please use {{ POD_POSTFIX }} instead.\",\"key\":\"PodConfiguration\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldAllowJinjaTemplatedPodYaml() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("PodSpecType", "yaml");
        String podYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: pod-name-prefix-{{ POD_POSTFIX }}\n" +
                "  labels:\n" +
                "    app: web\n" +
                "spec:\n" +
                "  containers:\n" +
                "    - name: gocd-agent-container-{{ CONTAINER_POSTFIX }}\n" +
                "      image: {{ GOCD_AGENT_IMAGE }}:{{ LATEST_VERSION }}";

        properties.put("PodConfiguration", podYaml);
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidatePodConfiguration() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("PodSpecType", "yaml");
        properties.put("PodConfiguration", "foobar");
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties));
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Invalid Pod Yaml.\",\"key\":\"PodConfiguration\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }
}
