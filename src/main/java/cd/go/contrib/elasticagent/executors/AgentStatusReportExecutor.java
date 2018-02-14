package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.model.reports.agent.KubernetesElasticAgent;
import cd.go.contrib.elasticagent.requests.AgentStatusReportRequest;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
        String elasticAgentId = request.getElasticAgentId();
        JobIdentifier jobIdentifier = request.getJobIdentifier();
        LOG.info(String.format("[status-report] Generating status report for agent: %s with job: %s", elasticAgentId, jobIdentifier));
        KubernetesClient client = factory.client(pluginRequest.getPluginSettings());

        try {
            Pod pod;
            if (StringUtils.isNotBlank(elasticAgentId)) {
                pod = findPodUsingElasticAgentId(elasticAgentId, client);
            } else {
                pod = findPodUsingJobIdentifier(jobIdentifier, client);
            }

            KubernetesElasticAgent elasticAgent = KubernetesElasticAgent.fromPod(client, pod, jobIdentifier);

            final String statusReportView = statusReportViewBuilder.build(statusReportViewBuilder.getTemplate("agent-status-report.template.ftlh"), elasticAgent);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            final String statusReportView = statusReportViewBuilder.build(statusReportViewBuilder.getTemplate("error.template.ftlh"), e);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        }
    }

    private Pod findPodUsingJobIdentifier(JobIdentifier jobIdentifier, KubernetesClient client) {
        try {
            return client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE)
                    .withLabel(Constants.JOB_ID_LABEL_KEY, String.valueOf(jobIdentifier.getJobId()))
                    .list().getItems().get(0);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Can not find a running Pod for the provided job identifier '%s'", jobIdentifier));
        }
    }

    private Pod findPodUsingElasticAgentId(String elasticAgentId, KubernetesClient client) {
        List<Pod> pods = client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE).list().getItems();
        for (Pod pod : pods) {
            if (pod.getMetadata().getName().equals(elasticAgentId)) {
                return pod;
            }
        }

        throw new RuntimeException(String.format("Can not find a running Pod for the provided elastic agent id '%s'", elasticAgentId));
    }
}
