package cd.go.contrib.elasticagent.model.reports.agent;

import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.ArrayList;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class KubernetesElasticAgent {
    private JobIdentifier jobIdentifier;
    private KubernetesPodDetails podDetails;
    private GoCDContainerDetails agentDetails;
    private String elasticAgentId;
    private ArrayList<KubernetesPodEvent> events;
    private String logs;
    private String configuration;

    public static KubernetesElasticAgent fromPod(KubernetesClient client, Pod pod, JobIdentifier jobIdentifier) {
        KubernetesElasticAgent agent = new KubernetesElasticAgent();
        agent.jobIdentifier = getJobIdentifier(pod, jobIdentifier);
        agent.elasticAgentId = pod.getMetadata().getName();
        agent.podDetails = KubernetesPodDetails.fromPod(pod);
        agent.agentDetails = GoCDContainerDetails.fromContainer(pod.getSpec().getContainers().get(0), pod.getStatus().getContainerStatuses().get(0));
        agent.events = getAllEventsForPod(pod, client);
        agent.logs = getPodLogs(pod, client);
        agent.configuration = getPodConfiguration(pod);
        return agent;
    }

    private static JobIdentifier getJobIdentifier(Pod pod, JobIdentifier jobIdentifier) {
        if (jobIdentifier != null) {
            return jobIdentifier;
        }

        final String json = pod.getMetadata().getAnnotations().get(Constants.JOB_IDENTIFIER_LABEL_KEY);
        return GSON.fromJson(json, JobIdentifier.class);
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

    public String getLogs() {
        return logs;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
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

    private static String getPodLogs(Pod pod, KubernetesClient client) {
        return client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE)
                .withName(pod.getMetadata().getName()).getLog(true);
    }

    private static String getPodConfiguration(Pod pod) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            return mapper.writeValueAsString(pod);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Failed to get Pod Configuration!";
        }
    }
}
