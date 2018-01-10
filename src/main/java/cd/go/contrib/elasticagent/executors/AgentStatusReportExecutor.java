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
import freemarker.template.Template;
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
        KubernetesClient client = factory.kubernetes(pluginRequest.getPluginSettings());

        Pod pod;
        if (StringUtils.isNotBlank(elasticAgentId)) {
            pod = findPodUsingElasticAgentId(elasticAgentId, client);
        } else {
            pod = findPodUsingJobIdentifier(jobIdentifier, client);
        }

        KubernetesElasticAgent elasticAgent = KubernetesElasticAgent.fromPod(pod, elasticAgentId, jobIdentifier);

        final Template template = statusReportViewBuilder.getTemplate("agent-status-report.template.ftlh");
        final String statusReportView = statusReportViewBuilder.build(template, elasticAgent);

        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("view", statusReportView);

        return DefaultGoPluginApiResponse.success(responseJSON.toString());
    }

    private Pod findPodUsingJobIdentifier(JobIdentifier jobIdentifier, KubernetesClient client) {
        try {
            return client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE)
                    .withLabel(Constants.JOB_ID_LABEL_KEY, String.valueOf(jobIdentifier.getJobId()))
                    .list().getItems().get(0);
        } catch (Exception e) {
            //todo: returning null is evil.. how about showing a fancy not found page?
            return null;
        }
    }

    private Pod findPodUsingElasticAgentId(String elasticAgentId, KubernetesClient client) {
        List<Pod> pods = client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE).list().getItems();
        for (Pod pod : pods) {
            if (pod.getMetadata().getName().equals(elasticAgentId)) {
                return pod;
            }
        }

        //todo: returning null is evil.. how about showing a fancy not found page?
        return null;
    }
}
