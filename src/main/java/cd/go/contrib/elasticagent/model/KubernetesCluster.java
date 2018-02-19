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

import cd.go.contrib.elasticagent.Constants;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class KubernetesCluster {
    private final List<KubernetesNode> nodes;
    private final String pluginId;

    public KubernetesCluster(KubernetesClient client) throws ParseException {
        pluginId = Constants.PLUGIN_ID;
        nodes = client.nodes().list().getItems().stream().map(node -> new KubernetesNode(node)).collect(toList());
        LOG.info("Running kubernetes nodes " + nodes.size());
        fetchPods(client);
    }

    private void fetchPods(KubernetesClient dockerClient) throws ParseException {
        final Map<String, KubernetesNode> dockerNodeMap = nodes.stream().distinct().collect(toMap(KubernetesNode::getName, node -> node));

        final List<Pod> pods = dockerClient.pods()
                .withLabel(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID)
                .list().getItems();

        LOG.info("Running pods " + pods.size());

        for (Pod pod : pods) {
            final KubernetesPod kubernetesPod = new KubernetesPod(pod);
            final KubernetesNode kubernetesNode = dockerNodeMap.get(kubernetesPod.getNodeName());
            if (kubernetesNode != null) {
                kubernetesNode.add(kubernetesPod);
            }
        }
    }

    public List<KubernetesNode> getNodes() {
        return nodes;
    }

    public String getPluginId() {
        return pluginId;
    }
}
