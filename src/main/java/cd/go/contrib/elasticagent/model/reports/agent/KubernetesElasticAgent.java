/*
 * Copyright 2018 ThoughtWorks, Inc.
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
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.ArrayList;

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

        return JobIdentifier.fromJson(pod.getMetadata().getAnnotations().get(Constants.JOB_IDENTIFIER_LABEL_KEY));
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
        return client.pods()
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
