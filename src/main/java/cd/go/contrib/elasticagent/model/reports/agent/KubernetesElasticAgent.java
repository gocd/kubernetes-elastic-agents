package cd.go.contrib.elasticagent.model.reports.agent;

import cd.go.contrib.elasticagent.model.JobIdentifier;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.ArrayList;

public class KubernetesElasticAgent {
    private KubernetesPodDetails podDetails;
    private GoCDContainerDetails agentDetails;
    private String elasticAgentId;
    private ArrayList<KubernetesPodEvent> events;

    public static KubernetesElasticAgent fromPod(KubernetesClient client, Pod pod, String elasticAgentId, JobIdentifier jobIdentifier) {
        KubernetesElasticAgent agent = new KubernetesElasticAgent();
        agent.elasticAgentId = elasticAgentId;
        agent.podDetails = KubernetesPodDetails.fromPod(pod);
        agent.agentDetails = GoCDContainerDetails.fromContainer(pod.getSpec().getContainers().get(0), pod.getStatus().getContainerStatuses().get(0));
        agent.events = getAllEventsForPod(pod, client);
        return agent;
    }

    private static ArrayList<KubernetesPodEvent> getAllEventsForPod(Pod pod, KubernetesClient client) {
        ArrayList<KubernetesPodEvent> events = new ArrayList<>();

        for (Event event : client.events().inAnyNamespace().list().getItems()) {
            if (event.getInvolvedObject().getKind().equals("Pod") && event.getInvolvedObject().getName().equals(pod.getMetadata().getName())) {
                KubernetesPodEvent podEvent = new KubernetesPodEvent(event.getFirstTimestamp(),
                        event.getLastTimestamp(),
                        event.getCount(),
                        event.getInvolvedObject().getFieldPath(),
                        event.getType(),
                        event.getReason(),
                        event.getMessage());

                events.add(podEvent);
            }
        }

        return events;
    }

    public ArrayList<KubernetesPodEvent> getEvents() {
        return events;
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
