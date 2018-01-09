package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.reports.agent.KubernetesElasticAgent;
import cd.go.contrib.elasticagent.requests.AgentStatusReportRequest;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;

public class AgentStatusReportExecutor {
    private final AgentStatusReportRequest request;
    private final PluginRequest pluginRequest;
    private final KubernetesClientFactory factory;
    private final PluginStatusReportViewBuilder statusReportViewBuilder;

    public AgentStatusReportExecutor(AgentStatusReportRequest request, PluginRequest pluginRequest) {
        this(request, pluginRequest, KubernetesClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    public AgentStatusReportExecutor(AgentStatusReportRequest request, PluginRequest pluginRequest, KubernetesClientFactory kubernetesClientFactory, PluginStatusReportViewBuilder builder) {
        this.request = request;
        this.pluginRequest = pluginRequest;
        this.factory = kubernetesClientFactory;
        this.statusReportViewBuilder = builder;
    }

    public GoPluginApiResponse execute() throws Exception {
        LOG.info(String.format("[status-report] Generating status report for agent: %s with job: %s", request.getElasticAgentId(), request.getJobIdentifier()));
        KubernetesClient client = factory.kubernetes(pluginRequest.getPluginSettings());
        KubernetesElasticAgent elasticAgent = KubernetesElasticAgent.fromLiveInformation(client);

        final Template template = statusReportViewBuilder.getTemplate("agent-status-report.template.ftlh");
        final String statusReportView = statusReportViewBuilder.build(template, elasticAgent);

        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("view", statusReportView);

        return DefaultGoPluginApiResponse.success(responseJSON.toString());
    }
}
