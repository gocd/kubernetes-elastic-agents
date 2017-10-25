/*
 * Copyright 2017 ThoughtWorks, Inc.
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

import cd.go.contrib.elasticagent.utils.Util;
import io.fabric8.kubernetes.api.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KubernetesNode {
    private final String name;
    private final List<KubernetesPod> pods = new ArrayList<>();
    private final String osImage;
    private final String operatingSystem;
    private final String architecture;
    private final String containerRuntimeVersion;
    private final String totalMemory;
    private final String totalCPU;
    private final String totalPods;
    private final String allocatablePods;
    private final String allocatableCPU;
    private final String allocatableMemory;
    private final String externalID;
    private final String nodeAddress;
    private final String kubeletVersion;
    private final String kubeProxyVersion;

    public KubernetesNode(Node node) {
        name = node.getMetadata().getName();
        externalID = node.getSpec().getExternalID();
        nodeAddress = node.getStatus().getAddresses().get(0).getAddress();

        totalCPU = node.getStatus().getCapacity().get("cpu").getAmount();
        String memory = node.getStatus().getCapacity().get("memory").getAmount();
        totalMemory = Util.readableSize(Long.valueOf(memory.replace("Ki", "")));
        totalPods = node.getStatus().getCapacity().get("pods").getAmount();

        allocatableCPU = node.getStatus().getAllocatable().get("cpu").getAmount();
        String allocatableMemory = node.getStatus().getAllocatable().get("memory").getAmount();
        this.allocatableMemory = Util.readableSize(Long.valueOf(allocatableMemory.replace("Ki", "")));
        allocatablePods = node.getStatus().getAllocatable().get("pods").getAmount();

        osImage = node.getStatus().getNodeInfo().getOsImage();
        operatingSystem = node.getStatus().getNodeInfo().getOperatingSystem();
        architecture = node.getStatus().getNodeInfo().getArchitecture();

        containerRuntimeVersion = node.getStatus().getNodeInfo().getContainerRuntimeVersion();
        kubeletVersion = node.getStatus().getNodeInfo().getKubeletVersion();
        kubeProxyVersion = node.getStatus().getNodeInfo().getKubeProxyVersion();
    }

    public String getName() {
        return name;
    }

    public String getOsImage() {
        return osImage;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getContainerRuntimeVersion() {
        return containerRuntimeVersion;
    }

    public String getTotalMemory() {
        return totalMemory;
    }

    public String getTotalCPU() {
        return totalCPU;
    }

    public String getTotalPods() {
        return totalPods;
    }

    public String getAllocatablePods() {
        return allocatablePods;
    }

    public String getAllocatableCPU() {
        return allocatableCPU;
    }

    public String getAllocatableMemory() {
        return allocatableMemory;
    }

    public String getExternalID() {
        return externalID;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public String getKubeletVersion() {
        return kubeletVersion;
    }

    public String getKubeProxyVersion() {
        return kubeProxyVersion;
    }

    public void add(KubernetesPod kubernetesPod) {
        this.pods.add(kubernetesPod);
    }

    public List<KubernetesPod> getPods() {
        return Collections.unmodifiableList(pods);
    }
}
