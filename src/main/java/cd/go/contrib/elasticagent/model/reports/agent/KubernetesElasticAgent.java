package cd.go.contrib.elasticagent.model.reports.agent;

import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesElasticAgent {
    public static KubernetesElasticAgent fromLiveInformation(KubernetesClient client) {
        KubernetesElasticAgent elasticAgent = new KubernetesElasticAgent();
        return elasticAgent;
    }
}
