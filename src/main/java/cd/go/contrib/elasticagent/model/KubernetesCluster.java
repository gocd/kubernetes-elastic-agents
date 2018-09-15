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

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.go.contrib.elasticagent.Constants;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesCluster {
    private List<KubernetesNode> nodes;
    private final String pluginId;

    public KubernetesCluster(List<KubernetesClient> clients) throws ParseException {
       pluginId = Constants.PLUGIN_ID;
       
       Map<String,KubernetesNode> nodeMap = new HashMap<>();
       for(KubernetesClient client:clients) {
    	   client.nodes().list().getItems().forEach(node ->  {
    		   LOG.info("node " + node.getMetadata().getName());
    		   nodeMap.put(node.getMetadata().getName(),new KubernetesNode(node)); 
    		  }
    	   );
        }
       
       LOG.info("Running kubernetes nodes " + nodeMap.values().size());
       for(KubernetesClient client:clients) {
    	    fetchPods(client,nodeMap);
        }
       
       nodes = new ArrayList<KubernetesNode>(nodeMap.values());
       LOG.info("Running kubernetes nodes " + nodes.size());
    }

    private void fetchPods(KubernetesClient dockerClient, Map<String,KubernetesNode> dockerNodeMap) throws ParseException {
        
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
