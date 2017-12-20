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

import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.KubernetesCluster;
import cd.go.contrib.elasticagent.model.StatusReportInformation;
import cd.go.contrib.elasticagent.requests.StatusReportRequest;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;

public class StatusReportExecutor implements RequestExecutor {
    private StatusReportRequest statusReportRequest;
    private final PluginRequest pluginRequest;
    private final KubernetesClientFactory factory;
    private final PluginStatusReportViewBuilder statusReportViewBuilder;


    public StatusReportExecutor(StatusReportRequest statusReportRequest, PluginRequest pluginRequest) throws IOException {
        this(statusReportRequest, pluginRequest, KubernetesClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    public StatusReportExecutor(StatusReportRequest statusReportRequest, PluginRequest pluginRequest, KubernetesClientFactory factory, PluginStatusReportViewBuilder statusReportViewBuilder) throws IOException {
        this.statusReportRequest = statusReportRequest;
        this.pluginRequest = pluginRequest;
        this.factory = factory;
        this.statusReportViewBuilder = statusReportViewBuilder;
    }

    public GoPluginApiResponse execute() throws Exception {
        LOG.info(String.format("[status-report] Generating status report %s", statusReportRequest.toString()));
        KubernetesClient client = factory.kubernetes(pluginRequest.getPluginSettings());
        StatusReportInformation reportInformation = new StatusReportInformation(new KubernetesCluster(client), statusReportRequest.jobIdentifier());
        final Template template = statusReportViewBuilder.getTemplate("status-report.template.ftlh");
        final String statusReportView = statusReportViewBuilder.build(template, reportInformation);

        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("view", statusReportView);

        return DefaultGoPluginApiResponse.success(responseJSON.toString());
    }
}