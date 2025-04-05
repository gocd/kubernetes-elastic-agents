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

import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.KubernetesCluster;
import cd.go.contrib.elasticagent.reports.StatusReportGenerationErrorHandler;
import cd.go.contrib.elasticagent.requests.ClusterStatusReportRequest;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;

public class ClusterStatusReportExecutor {
    private final PluginStatusReportViewBuilder statusReportViewBuilder;
    private final ClusterStatusReportRequest request;
    private final KubernetesClientFactory factory;

    public ClusterStatusReportExecutor(ClusterStatusReportRequest request, PluginStatusReportViewBuilder statusReportViewBuilder) {
        this.request = request;
        this.factory = KubernetesClientFactory.instance();
        this.statusReportViewBuilder = statusReportViewBuilder;
    }

    public ClusterStatusReportExecutor(ClusterStatusReportRequest request, PluginStatusReportViewBuilder statusReportViewBuilder, KubernetesClientFactory factory) {
        this.request = request;
        this.factory = factory;
        this.statusReportViewBuilder = statusReportViewBuilder;
    }

    public GoPluginApiResponse execute() {
        try {
            LOG.info("[status-report] Generating status report.");
            final KubernetesCluster kubernetesCluster;
            try (KubernetesClientFactory.CachedClient client = factory.client(request.clusterProfileProperties())) {
                kubernetesCluster = new KubernetesCluster(client.get());
            }
            final Template template = statusReportViewBuilder.getTemplate("status-report.template.ftlh");
            final String statusReportView = statusReportViewBuilder.build(template, kubernetesCluster);

            final JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            return StatusReportGenerationErrorHandler.handle(statusReportViewBuilder, e);
        }
    }
}
