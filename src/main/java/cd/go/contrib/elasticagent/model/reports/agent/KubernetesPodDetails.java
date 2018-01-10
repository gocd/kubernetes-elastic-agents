package cd.go.contrib.elasticagent.model.reports.agent;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;

import java.util.ArrayList;

public class KubernetesPodDetails {
    private String name;
    private String clusterName;
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
        podDetails.clusterName = pod.getMetadata().getClusterName();
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
                    podCondition.getStatus(),
                    podCondition.getLastTransitionTime(),
                    podCondition.getLastProbeTime());
            podDetails.conditions.add(condition);
        }

        return podDetails;
    }

    public String getName() {
        return name;
    }

    public String getClusterName() {
        return clusterName;
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

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    private static class Condition {
        private final String type;
        private final String status;
        private final String lastTransitionTime;
        private final String lastProbeTime;

        public Condition(String type, String status, String lastTransitionTime, String lastProbeTime) {
            this.type = type;
            this.status = status;
            this.lastTransitionTime = lastTransitionTime;
            this.lastProbeTime = lastProbeTime;
        }

        public String getType() {
            return type;
        }

        public String getStatus() {
            return status;
        }

        public String getLastTransitionTime() {
            return lastTransitionTime;
        }

        public String getLastProbeTime() {
            return lastProbeTime;
        }
    }
}
