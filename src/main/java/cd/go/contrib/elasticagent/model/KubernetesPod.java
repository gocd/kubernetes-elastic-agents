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

package cd.go.contrib.elasticagent.model;

import cd.go.contrib.elasticagent.Constants;
import io.fabric8.kubernetes.api.model.Pod;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

public class KubernetesPod {
    private final String podName;
    private final String nodeName;
    private final String image;
    private final Date creationTimestamp;
    private final String podIP;
    private final String status;
    private final JobIdentifier jobIdentifier;

    public KubernetesPod(Pod pod) {
        jobIdentifier = JobIdentifier.fromJson(pod.getMetadata().getAnnotations().get(Constants.JOB_IDENTIFIER_LABEL_KEY));
        podName = pod.getMetadata().getName();
        image = pod.getSpec().getContainers().get(0).getImage();
        podIP = pod.getStatus().getPodIP();
        final CharSequence text = pod.getMetadata().getCreationTimestamp();
        creationTimestamp = Date.from(Constants.KUBERNETES_POD_CREATION_TIME_FORMAT.parse(text, Instant::from));
        status = pod.getStatus().getPhase();

        nodeName = pod.getSpec().getNodeName();
    }

    public String getPodName() {
        return podName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getImage() {
        return image;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getPodIP() {
        return podIP;
    }

    public String getStatus() {
        return status;
    }

    public String getJobInformation() {
        if (jobIdentifier != null) {
            return jobIdentifier.getRepresentation();
        }
        return "No Job Information Available!";
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KubernetesPod that)) return false;

        if (!Objects.equals(podName, that.podName)) return false;
        if (!Objects.equals(nodeName, that.nodeName)) return false;
        if (!Objects.equals(image, that.image)) return false;
        if (!Objects.equals(creationTimestamp, that.creationTimestamp))
            return false;
        if (!Objects.equals(podIP, that.podIP)) return false;
        if (!Objects.equals(status, that.status)) return false;
        return Objects.equals(jobIdentifier, that.jobIdentifier);
    }

    @Override
    public int hashCode() {
        int result = podName != null ? podName.hashCode() : 0;
        result = 31 * result + (nodeName != null ? nodeName.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (podIP != null ? podIP.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (jobIdentifier != null ? jobIdentifier.hashCode() : 0);
        return result;
    }
}
