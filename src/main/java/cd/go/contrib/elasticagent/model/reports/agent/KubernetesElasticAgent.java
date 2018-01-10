package cd.go.contrib.elasticagent.model.reports.agent;

import cd.go.contrib.elasticagent.model.JobIdentifier;
import io.fabric8.kubernetes.api.model.Pod;

public class KubernetesElasticAgent {
    private KubernetesPodDetails podDetails;
    private GoCDContainerDetails agentDetails;
    private String elasticAgentId;

    public static KubernetesElasticAgent fromPod(Pod pod, String elasticAgentId, JobIdentifier jobIdentifier) {
        KubernetesElasticAgent agent = new KubernetesElasticAgent();
        agent.elasticAgentId = elasticAgentId;
        agent.podDetails = KubernetesPodDetails.fromPod(pod);
        agent.agentDetails = GoCDContainerDetails.fromContainer(pod.getSpec().getContainers().get(0), pod.getStatus().getContainerStatuses().get(0));
        return agent;
    }

    public KubernetesPodDetails getPodDetails() {
        return podDetails;
    }

    public GoCDContainerDetails getAgentDetails() {
        return agentDetails;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }
}
