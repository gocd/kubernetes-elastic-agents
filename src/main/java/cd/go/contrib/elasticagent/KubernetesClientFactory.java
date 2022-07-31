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

package cd.go.contrib.elasticagent;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class KubernetesClientFactory {
    private static final KubernetesClientFactory KUBERNETES_CLIENT_FACTORY = new KubernetesClientFactory();
    private final Clock clock;
    private KubernetesClient client;
    private PluginSettings clusterProfileConfigurations;
    private long clientCreatedTime;
    private long kubernetesClientRecycleIntervalInMinutes = -1;
    public static final String CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY = "go.kubernetes.elastic-agent.plugin.client.recycle.interval.in.minutes";

    public KubernetesClientFactory() {
        this.clock = Clock.DEFAULT;
    }

    //used for testing..
    public KubernetesClientFactory(Clock clock) {
        this.clock = clock;
    }

    public static KubernetesClientFactory instance() {
        return KUBERNETES_CLIENT_FACTORY;
    }

    public synchronized KubernetesClient client(PluginSettings clusterProfileConfigurations) {
        clearOutClientOnTimer();
        if (clusterProfileConfigurations.equals(this.clusterProfileConfigurations) && this.client != null) {
            LOG.debug("Using previously created client.");
            return this.client;
        }

        LOG.debug(format("Creating a new client because {0}.", (client == null) ? "client is null" : "cluster profile configurations has changed"));
        this.clusterProfileConfigurations = clusterProfileConfigurations;
        this.client = createClientFor(clusterProfileConfigurations);
        this.clientCreatedTime = this.clock.now().getMillis();
        LOG.debug("New client is created.");

        return this.client;
    }

    private void clearOutClientOnTimer() {
        long currentTime = this.clock.now().getMillis();
        long differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - this.clientCreatedTime);
        if (differenceInMinutes > getKubernetesClientRecycleInterval()) {
            LOG.info("Recycling kubernetes client on timer...");
            clearOutExistingClient();
        }
    }

    private KubernetesClient createClientFor(PluginSettings pluginSettings) {
        final ConfigBuilder configBuilder = new ConfigBuilder()
                .withOauthToken(pluginSettings.getSecurityToken())
                .withMasterUrl(pluginSettings.getClusterUrl())
                .withCaCertData(pluginSettings.getCaCertData())
                .withNamespace(pluginSettings.getNamespace());

        return new DefaultKubernetesClient(configBuilder.build());
    }

    public void clearOutExistingClient() {
        if (this.client != null) {
            LOG.debug("Terminating existing kubernetes client...");
            this.client.close();
            this.client = null;
        }
    }

    private long getKubernetesClientRecycleInterval() {
        //if the value is already read, send it..
        if (this.kubernetesClientRecycleIntervalInMinutes != -1) {
            return this.kubernetesClientRecycleIntervalInMinutes;
        }

        String property = System.getProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY);
        if (StringUtils.isBlank(property)) {
            //set default to 10 minutes when system property is not specified
            this.kubernetesClientRecycleIntervalInMinutes = 10;
            return this.kubernetesClientRecycleIntervalInMinutes;
        }

        try {
            this.kubernetesClientRecycleIntervalInMinutes = Integer.valueOf(property);
        } catch (Exception e) {
            //set default value to 10 minutes when parsing user input fails
            this.kubernetesClientRecycleIntervalInMinutes = 10;
        }

        return this.kubernetesClientRecycleIntervalInMinutes;
    }
}
