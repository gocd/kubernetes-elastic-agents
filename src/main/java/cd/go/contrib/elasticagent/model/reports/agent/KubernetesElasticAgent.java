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

package cd.go.contrib.elasticagent.model.reports.agent;

import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.ArrayList;
import java.util.List;

public class KubernetesElasticAgent {
    private JobIdentifier jobIdentifier;
    private KubernetesPodDetails podDetails;
    private ArrayList<GoCDContainerDetails> containerDetails;
    private String elasticAgentId;
    private ArrayList<KubernetesPodEvent> events;
    private ArrayList<GoCDContainerLog> containerLogs;
    private String configuration;

    public static KubernetesElasticAgent fromPod(KubernetesClient client, Pod pod, JobIdentifier jobIdentifier) {
        KubernetesElasticAgent agent = new KubernetesElasticAgent();
        agent.jobIdentifier = getJobIdentifier(pod, jobIdentifier);
        agent.elasticAgentId = pod.getMetadata().getName();
        agent.podDetails = KubernetesPodDetails.fromPod(pod);
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        agent.containerDetails = getContainerDetails(pod.getSpec().getContainers(), containerStatuses);
        agent.events = getAllEventsForPod(pod, client);
        agent.containerLogs = getContainerLogs(client, pod, containerStatuses);
        agent.configuration = getPodConfiguration(pod);
        return agent;
    }

    private static ArrayList<GoCDContainerDetails> getContainerDetails(List<Container> containers, List<ContainerStatus> containerStatuses) {
        ArrayList<GoCDContainerDetails> details = new ArrayList<>();
        for(ContainerStatus status: containerStatuses) {
            details.add(GoCDContainerDetails.fromContainer(getContainerSpecByName(containers, status.getName()), status));
        }
        return details;
    }

    private static Container getContainerSpecByName(List<Container> containers, String name) {
        return containers.stream().filter(container -> container.getName().equals(name)).findFirst().get();
    }

    private static ArrayList<GoCDContainerLog> getContainerLogs(KubernetesClient client, Pod pod, List<ContainerStatus> containerStatuses) {
        ArrayList<GoCDContainerLog> logs = new ArrayList<>();
        for(ContainerStatus containerStatus: containerStatuses) {
            logs.add(new GoCDContainerLog(containerStatus.getName(), getPodLogs(pod, client, containerStatus.getName())));
        }
        return logs;
    }

    private static JobIdentifier getJobIdentifier(Pod pod, JobIdentifier jobIdentifier) {
        if (jobIdentifier != null) {
            return jobIdentifier;
        }

        return JobIdentifier.fromJson(pod.getMetadata().getAnnotations().get(Constants.JOB_IDENTIFIER_LABEL_KEY));
    }

    public ArrayList<KubernetesPodEvent> getEvents() {
        return events;
    }

    public KubernetesPodDetails getPodDetails() {
        return podDetails;
    }

    public ArrayList<GoCDContainerDetails> getContainerDetails() {
        return containerDetails;
    }

    public ArrayList<GoCDContainerLog> getContainerLogs() {
        return containerLogs;
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

        for (Event event : client.v1().events().inAnyNamespace().list().getItems()) {
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

    private static String getPodLogs(Pod pod, KubernetesClient client, String containerName) {
        return client.pods()
                .withName(pod.getMetadata().getName()).inContainer(containerName).getLog(true);
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
