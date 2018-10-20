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

package cd.go.contrib.elasticagent;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class KubernetesClientFactory {
    private static final KubernetesClientFactory KUBERNETES_CLIENT_FACTORY = new KubernetesClientFactory();
    private KubernetesClient client;
    private PluginSettings pluginSettings;

    public static KubernetesClientFactory instance() {
        return KUBERNETES_CLIENT_FACTORY;
    }

    public synchronized KubernetesClient createClientForPluginSetting(PluginSettings pluginSettings) {
        if (pluginSettings.equals(this.pluginSettings) && this.client != null) {
            LOG.debug("Using previously created client.");
            return this.client;
        }

        LOG.debug(format("Creating a new client because {0}.", (client == null) ? "client is null" : "plugin setting is changed"));
        this.pluginSettings = pluginSettings;
        this.client = createClient(pluginSettings);
        LOG.debug("New client is created.");
        return this.client;
    }
    
    public synchronized KubernetesClient createClientForElasticProfile(ElasticProfileSettings pluginSettings) {
    	
        final ConfigBuilder configBuilder = new ConfigBuilder()
                .withOauthToken(pluginSettings.getSecurityToken())
                .withMasterUrl(pluginSettings.getClusterUrl())
                .withCaCertData(pluginSettings.getClusterCACertData())
                .withNamespace(pluginSettings.getNamespace());

        return new DefaultKubernetesClient(configBuilder.build());
    }

    private KubernetesClient createClient(PluginSettings pluginSettings) {
        final ConfigBuilder configBuilder = new ConfigBuilder()
                .withOauthToken(pluginSettings.getSecurityToken())
                .withMasterUrl(pluginSettings.getClusterUrl())
                .withCaCertData(pluginSettings.getCaCertData())
                .withNamespace(pluginSettings.getNamespace());

        return new DefaultKubernetesClient(configBuilder.build());
    }
}