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

package cd.go.contrib.elasticagent.executors;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import cd.go.contrib.elasticagent.Agent;
import cd.go.contrib.elasticagent.AgentInstances;
import cd.go.contrib.elasticagent.Agents;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.KubernetesInstance;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.KubernetesCluster;
import cd.go.contrib.elasticagent.reports.StatusReportGenerationErrorHandler;
import freemarker.template.Template;
import io.fabric8.kubernetes.client.KubernetesClient;

public class StatusReportExecutor {
    private final PluginRequest pluginRequest;
    private final KubernetesClientFactory factory;
    private final PluginStatusReportViewBuilder statusReportViewBuilder;
    private final AgentInstances<KubernetesInstance> agentInstances;


    public StatusReportExecutor(AgentInstances<KubernetesInstance> agentInstances,PluginRequest pluginRequest) {
        this(agentInstances,pluginRequest, KubernetesClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    public StatusReportExecutor(AgentInstances<KubernetesInstance> agentInstances,PluginRequest pluginRequest, KubernetesClientFactory factory, PluginStatusReportViewBuilder statusReportViewBuilder) {
    	this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
        this.factory = factory;
        this.statusReportViewBuilder = statusReportViewBuilder;
    }

    public GoPluginApiResponse execute() {
        try {
            LOG.info("[status-report] Generating status report.");
            
            Agents allAgents = pluginRequest.listAgents();
            
            Map<String,KubernetesClient> clientMap = new HashMap<>();
            KubernetesClient client;
            KubernetesInstance instance;
            for (Agent agent : allAgents.agents()) {
                if (agentInstances.find(agent.elasticAgentId()) != null) {
                	instance = agentInstances.find(agent.elasticAgentId()) ;
                	client = factory.createClientForElasticProfile(instance.getSettings());
                	LOG.info("[status-report] client."+client.getNamespace());
                	clientMap.put(client.getNamespace(), client);
                }
            }
            
            // Use the default Plugin Level Client as well to get all Nodes
            client = factory.createClientForPluginSetting(pluginRequest.getPluginSettings());
            List<KubernetesClient> clientList = new ArrayList<>(Arrays.asList(client));
            
            if( clientMap.size()>0) {
	            clientList.addAll(clientMap.values());
            }
            LOG.info("[status-report] clientList."+clientList.size());
            final KubernetesCluster kubernetesCluster = new KubernetesCluster(clientList);
            final Template template = statusReportViewBuilder.getTemplate("status-report.template.ftlh");
            final String statusReportView = statusReportViewBuilder.build(template, kubernetesCluster);

            final JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            return StatusReportGenerationErrorHandler.handle(statusReportViewBuilder, e);
        }
    }
}