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

import io.fabric8.kubernetes.client.KubernetesClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Map;

public class KubernetesInstance {
    private final DateTime createdAt;
    private final String environment;
    private final String name;
    private final Map<String, String> properties;

    public KubernetesInstance(DateTime createdAt, String environment, String name, Map<String, String> properties) {
        this.createdAt = createdAt.withZone(DateTimeZone.UTC);
        this.environment = environment;
        this.name = name;
        this.properties = properties;
    }

    public void terminate(KubernetesClient client) {
        client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE_KEY).withName(name).delete();
    }

    public String name() {
        return name;
    }

    public DateTime createdAt() {
        return createdAt;
    }

    public String environment() {
        return environment;
    }

    public Map<String, String> getInstanceProperties() {
        return properties;
    }
}
