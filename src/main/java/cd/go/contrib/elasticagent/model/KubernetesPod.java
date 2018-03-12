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

package cd.go.contrib.elasticagent.model;

import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.utils.Util;
import io.fabric8.kubernetes.api.model.Pod;

import java.text.ParseException;
import java.util.Date;

public class KubernetesPod {
    private final String podName;
    private String nodeName;
    private final String image;
    private final Date creationTimestamp;
    private final String podIP;
    private final String status;
    private JobIdentifier jobIdentifier;

    public KubernetesPod(Pod pod) throws ParseException {
        jobIdentifier = JobIdentifier.fromJson(pod.getMetadata().getAnnotations().get(Constants.JOB_IDENTIFIER_LABEL_KEY));
        podName = pod.getMetadata().getName();
        image = pod.getSpec().getContainers().get(0).getImage();
        podIP = pod.getStatus().getPodIP();
        creationTimestamp = Util.getSimpleDateFormat().parse(pod.getMetadata().getCreationTimestamp());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KubernetesPod)) return false;

        KubernetesPod that = (KubernetesPod) o;

        if (podName != null ? !podName.equals(that.podName) : that.podName != null) return false;
        if (nodeName != null ? !nodeName.equals(that.nodeName) : that.nodeName != null) return false;
        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(that.creationTimestamp) : that.creationTimestamp != null)
            return false;
        if (podIP != null ? !podIP.equals(that.podIP) : that.podIP != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return jobIdentifier != null ? jobIdentifier.equals(that.jobIdentifier) : that.jobIdentifier == null;
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
