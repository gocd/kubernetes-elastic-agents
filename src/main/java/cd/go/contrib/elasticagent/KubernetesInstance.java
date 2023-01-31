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

import io.fabric8.kubernetes.client.KubernetesClient;

import java.time.Instant;
import java.util.Map;

public class KubernetesInstance {
    private final Instant createdAt;
    private final String environment;
    private final String name;
    private final Map<String, String> properties;
    private final Long jobId;
    private final PodState state;

    public KubernetesInstance(Instant createdAt, String environment, String name, Map<String, String> properties, Long jobId, PodState state) {
        this.createdAt = createdAt;
        this.environment = environment;
        this.name = name;
        this.properties = properties;
        this.jobId = jobId;
        this.state = state;
    }

    public void terminate(KubernetesClient client) {
        client.pods().withName(name).delete();
    }

    public String name() {
        return name;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String environment() {
        return environment;
    }

    public Map<String, String> getInstanceProperties() {
        return properties;
    }

    public Long jobId() {
        return jobId;
    }

    public boolean isPending() {
        return this.state.equals(PodState.Pending);
    }
}
