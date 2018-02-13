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

import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.model.Field;
import cd.go.contrib.elasticagent.model.GoServerUrlField;
import cd.go.contrib.elasticagent.model.PositiveNumberField;
import cd.go.contrib.elasticagent.model.SecureURLField;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class GetPluginConfigurationExecutor implements RequestExecutor {
    public static final Field GO_SERVER_URL = new GoServerUrlField("go_server_url", "GoCD server URL", false, "0");
    public static final Field AUTOREGISTER_TIMEOUT = new PositiveNumberField("auto_register_timeout", "Agent auto-register timeout (in minutes)", "10", true, false, "1");
    public static final Field MAXIMUM_PENDING_PODS_COUNT = new PositiveNumberField("pending_pods_count", "Maximum pending pods", "10", true, false, "2");
    public static final Field CLUSTER_URL = new SecureURLField("kubernetes_cluster_url", "Cluster URL", true, "3");
    public static final Field CLUSTER_CA_CERT = new Field("kubernetes_cluster_ca_cert", "Cluster ca-certificate", null, true, true, "4");
    public static final Map<String, Field> FIELDS = new LinkedHashMap<>();

    static {
        FIELDS.put(GO_SERVER_URL.key(), GO_SERVER_URL);
        FIELDS.put(AUTOREGISTER_TIMEOUT.key(), AUTOREGISTER_TIMEOUT);
        FIELDS.put(MAXIMUM_PENDING_PODS_COUNT.key(), MAXIMUM_PENDING_PODS_COUNT);
        FIELDS.put(CLUSTER_URL.key(), CLUSTER_URL);
        FIELDS.put(CLUSTER_CA_CERT.key(), CLUSTER_CA_CERT);
    }

    public GoPluginApiResponse execute() {
        return DefaultGoPluginApiResponse.success(GSON.toJson(FIELDS));
    }
}
