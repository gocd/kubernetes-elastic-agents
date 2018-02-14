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
import cd.go.contrib.elasticagent.model.*;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class GetPluginConfigurationExecutor implements RequestExecutor {
    public static final Map<String, Field> FIELDS = new LinkedHashMap<>();
    public static final Field GO_SERVER_URL = new GoServerUrlField("go_server_url", "GoCD server URL", false, "0");
    public static final Field AUTO_REGISTER_TIMEOUT = new PositiveNumberField("auto_register_timeout", "Agent auto-register timeout (in minutes)", "10", false, false, "1");
    public static final Field MAX_PENDING_PODS = new PositiveNumberField("pending_pods_count", "Maximum pending pods", "10", false, false, "2");
    public static final Field AUTHENTICATION_STRATEGY = new NonBlankField("authentication_strategy", "Authentication strategy", false, "3");
    public static final Field OAUTH_TOKEN = new Field("oauth_token", "Oauth token", null, false, true, "4");
    public static final Field CLUSTER_URL = new SecureURLField("kubernetes_cluster_url", "Cluster URL", false, "5");
    public static final Field CLUSTER_CA_CERT = new Field("kubernetes_cluster_ca_cert", "Cluster ca certificate", null, false, true, "6");
    public static final Field CLIENT_KEY_DATA = new Field("client_key_data", "Client key data", null, false, true, "7");
    public static final Field CLIENT_CERT_DATA = new Field("client_cert_data", "client cert data", null, false, true, "8");

    static {
        FIELDS.put(GO_SERVER_URL.key(), GO_SERVER_URL);
        FIELDS.put(AUTO_REGISTER_TIMEOUT.key(), AUTO_REGISTER_TIMEOUT);
        FIELDS.put(MAX_PENDING_PODS.key(), MAX_PENDING_PODS);
        FIELDS.put(CLUSTER_URL.key(), CLUSTER_URL);
        FIELDS.put(CLUSTER_CA_CERT.key(), CLUSTER_CA_CERT);
        FIELDS.put(AUTHENTICATION_STRATEGY.key(), AUTHENTICATION_STRATEGY);
        FIELDS.put(OAUTH_TOKEN.key(), OAUTH_TOKEN);
        FIELDS.put(CLIENT_KEY_DATA.key(), CLIENT_KEY_DATA);
        FIELDS.put(CLIENT_CERT_DATA.key(), CLIENT_CERT_DATA);
    }

    public GoPluginApiResponse execute() {
        return DefaultGoPluginApiResponse.success(GSON.toJson(FIELDS));
    }
}
