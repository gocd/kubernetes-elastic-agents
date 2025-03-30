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

import cd.go.contrib.elasticagent.GoServerURLMetadata;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.model.Metadata;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.List;

public class GetClusterProfileMetadataExecutor implements RequestExecutor {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Metadata GO_SERVER_URL = new GoServerURLMetadata();
    public static final Metadata AUTO_REGISTER_TIMEOUT = new Metadata("auto_register_timeout", false, false);
    public static final Metadata MAX_PENDING_PODS = new Metadata("pending_pods_count", false, false);
    public static final Metadata CLUSTER_URL = new Metadata("kubernetes_cluster_url", false, false);
    public static final Metadata NAMESPACE = new Metadata("namespace", false, false);
    public static final Metadata SECURITY_TOKEN = new Metadata("security_token", false, true);
    public static final Metadata CLUSTER_CA_CERT = new Metadata("kubernetes_cluster_ca_cert", false, true);
    public static final Metadata CLUSTER_REQUEST_TIMEOUT = new Metadata("cluster_request_timeout", false, false);
    public static final Metadata ENABLE_AGENT_REUSE = new Metadata("enable_agent_reuse", false, false);

    public static final List<Metadata> FIELDS = List.of(
        GO_SERVER_URL,
        AUTO_REGISTER_TIMEOUT,
        MAX_PENDING_PODS,
        CLUSTER_URL,
        NAMESPACE,
        SECURITY_TOKEN,
        CLUSTER_CA_CERT,
        CLUSTER_REQUEST_TIMEOUT,
        ENABLE_AGENT_REUSE
    );

    @Override
    public GoPluginApiResponse execute() throws Exception {
        return DefaultGoPluginApiResponse.success(GSON.toJson(FIELDS));
    }
}
