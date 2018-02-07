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

package cd.go.contrib.elasticagent;

import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;

import java.util.HashMap;
import java.util.UUID;

public class CreateAgentRequestMother {
    public static CreateAgentRequest defaultCreateAgentRequest() {
        String autoRegisterKey = UUID.randomUUID().toString();
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "gocd/custom-gocd-agent-alpine");
        properties.put("MaxMemory", "1024M");
        properties.put("MaxCPU", "2");
        properties.put("Environment", "ENV1=VALUE1\n" +
                "ENV2=VALUE2");
        properties.put("PodConfiguration", "");
        properties.put("SpecifiedUsingPodConfiguration", "false");
        properties.put("Privileged", "false");

        String environment = "QA";
        JobIdentifier identifier = new JobIdentifier("up_42", 1L, "up_42_label", "up42_stage", "20", "up42_job", 10L);
        return new CreateAgentRequest(autoRegisterKey, properties, environment, identifier);
    }

    public static CreateAgentRequest createAgentRequestUsingPodYaml() {
        String podYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: test-pod-yaml\n" +
                "  labels:\n" +
                "    app: gocd-agent\n" +
                "  annotations:\n" +
                "    annotation-key: my-fancy-annotation-value\n" +
                "spec:\n" +
                "  containers:\n" +
                "  - name: gocd-agent-container\n" +
                "    image: gocd/gocd-agent-alpine-3.5:v17.12.0\n" +
                "    imagePullPolicy: Always\n" +
                "    env:\n" +
                "    - name: DEMO_ENV\n" +
                "      value: DEMO_FANCY_VALUE\n" +
                "    ports:\n" +
                "    - containerPort: 80";

        String autoRegisterKey = UUID.randomUUID().toString();
        HashMap<String, String> properties = new HashMap<>();

        properties.put("PodConfiguration", podYaml);
        properties.put("SpecifiedUsingPodConfiguration", "true");

        String environment = "QA";
        JobIdentifier identifier = new JobIdentifier("up_42", 1L, "up_42_label", "up42_stage", "20", "up42_job", 10L);
        return new CreateAgentRequest(autoRegisterKey, properties, environment, identifier);
    }
}
