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

import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.model.reports.agent.KubernetesElasticAgent;
import cd.go.contrib.elasticagent.reports.StatusReportGenerationErrorHandler;
import cd.go.contrib.elasticagent.reports.StatusReportGenerationException;
import cd.go.contrib.elasticagent.requests.AgentStatusReportRequest;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.utils.Util.isBlank;
import static java.text.MessageFormat.format;

public class AgentStatusReportExecutor {
    private final AgentStatusReportRequest request;
    private final KubernetesClientFactory factory;
    private final PluginStatusReportViewBuilder statusReportViewBuilder;

    public AgentStatusReportExecutor(AgentStatusReportRequest request) {
        this(request, KubernetesClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    AgentStatusReportExecutor(AgentStatusReportRequest request, KubernetesClientFactory kubernetesClientFactory, PluginStatusReportViewBuilder builder) {
        this.request = request;
        this.factory = kubernetesClientFactory;
        this.statusReportViewBuilder = builder;
    }

    public GoPluginApiResponse execute() {
        String elasticAgentId = request.getElasticAgentId();
        JobIdentifier jobIdentifier = request.getJobIdentifier();
        LOG.info(format("[status-report] Generating status report for agent: {0} with job: {1}", elasticAgentId, jobIdentifier));
        try (KubernetesClientFactory.CachedClient client = factory.client(request.clusterProfileProperties())) {
            try {
                Pod pod;
                if (!isBlank(elasticAgentId)) {
                    pod = findPodUsingElasticAgentId(elasticAgentId, client.get());
                } else {
                    pod = findPodUsingJobIdentifier(jobIdentifier, client.get());
                }

                KubernetesElasticAgent elasticAgent = KubernetesElasticAgent.fromPod(client.get(), pod, jobIdentifier);

                final String statusReportView = statusReportViewBuilder.build(statusReportViewBuilder.getTemplate("agent-status-report.template.ftlh"), elasticAgent);

                final JsonObject responseJSON = new JsonObject();
                responseJSON.addProperty("view", statusReportView);

                return DefaultGoPluginApiResponse.success(responseJSON.toString());
            } catch (Exception e) {
                return StatusReportGenerationErrorHandler.handle(statusReportViewBuilder, e);
            }
        }
    }

    private Pod findPodUsingJobIdentifier(JobIdentifier jobIdentifier, KubernetesClient client) {
        try {
            return client.pods()
                    .withLabel(Constants.JOB_ID_LABEL_KEY, String.valueOf(jobIdentifier.getJobId()))
                    .list().getItems().get(0);
        } catch (Exception e) {
            throw StatusReportGenerationException.noRunningPod(jobIdentifier);
        }
    }

    private Pod findPodUsingElasticAgentId(String elasticAgentId, KubernetesClient client) {
        List<Pod> pods = client.pods().list().getItems();
        for (Pod pod : pods) {
            if (pod.getMetadata().getName().equals(elasticAgentId)) {
                return pod;
            }
        }

        throw StatusReportGenerationException.noRunningPod(elasticAgentId);
    }
}
