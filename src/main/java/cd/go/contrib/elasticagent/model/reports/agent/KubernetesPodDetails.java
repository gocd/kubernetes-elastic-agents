package cd.go.contrib.elasticagent.model.reports.agent;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class KubernetesPodDetails {

    private String name;
    private String nodeName;
    private String namespace;
    private String createdAt;
    private String startedAt;
    private String phase;
    private String podIP;
    private String hostIP;
    private ArrayList<Condition> conditions;

    public static KubernetesPodDetails fromPod(Pod pod) {
        KubernetesPodDetails podDetails = new KubernetesPodDetails();

        podDetails.name = pod.getMetadata().getName();
        podDetails.nodeName = pod.getSpec().getNodeName();
        podDetails.namespace = pod.getMetadata().getNamespace();

        podDetails.createdAt = pod.getMetadata().getCreationTimestamp();
        podDetails.startedAt = pod.getStatus().getStartTime();

        podDetails.phase = pod.getStatus().getPhase();

        podDetails.podIP = pod.getStatus().getPodIP();
        podDetails.hostIP = pod.getStatus().getHostIP();

        podDetails.conditions = new ArrayList<>();
        for (PodCondition podCondition : pod.getStatus().getConditions()) {
            Condition condition = new Condition(podCondition.getType(),
                    podCondition.getStatus());
            podDetails.conditions.add(condition);
        }

        return podDetails;
    }

    public String getName() {
        return name;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getPhase() {
        return phase;
    }

    public String getPodIP() {
        return podIP;
    }

    public String getHostIP() {
        return hostIP;
    }

    public String getConditions() {
        return conditions.stream()
                .map(Condition::toString)
                .collect(Collectors.joining(", "));
    }

    private static class Condition {
        private final String type;
        private final String status;

        public Condition(String type, String status) {
            this.type = type;
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return format("{0}: {1}", type, status);
        }
    }
}
