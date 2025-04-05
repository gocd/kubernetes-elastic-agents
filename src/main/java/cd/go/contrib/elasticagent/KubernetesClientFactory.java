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

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.utils.Util.isBlank;
import static cd.go.contrib.elasticagent.utils.Util.setIfNotBlank;
import static java.text.MessageFormat.format;

public class KubernetesClientFactory {
    private static final KubernetesClientFactory KUBERNETES_CLIENT_FACTORY = new KubernetesClientFactory();
    public static final String CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY = "go.kubernetes.elastic-agent.plugin.client.recycle.interval.in.minutes";

    private final Clock clock;

    private volatile CachedClient client;
    private volatile long kubernetesClientRecycleIntervalInMinutes = -1;

    KubernetesClientFactory() {
        this(Clock.DEFAULT);
    }

    //used for testing..
    KubernetesClientFactory(Clock clock) {
        this.clock = clock;
        System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
    }

    public static KubernetesClientFactory instance() {
        return KUBERNETES_CLIENT_FACTORY;
    }

    public synchronized CachedClient client(PluginSettings clusterProfileConfigurations) {
        clearOutClientOnTimer();
        if (this.client != null && clusterProfileConfigurations.equals(this.client.clusterProfileConfigurations)) {
            LOG.debug("Using previously created client.");
            this.client.leases.incrementAndGet();
            return this.client;
        }

        LOG.debug(format("Creating a new client because {0}.", (client == null) ? "client is null" : "cluster profile configurations has changed"));
        clearOutExistingClient();
        this.client = createClientFor(clusterProfileConfigurations);
        LOG.debug("New client is created.");

        return this.client;
    }

    private void clearOutClientOnTimer() {
        if (client != null && TimeUnit.MILLISECONDS.toMinutes(this.clock.now().toEpochMilli() - this.client.clientCreatedTime) > getKubernetesClientRecycleInterval()) {
            LOG.info("Recycling kubernetes client on timer...");
            clearOutExistingClient();
        }
    }

    private CachedClient createClientFor(PluginSettings pluginSettings) {
        Config config = Config.autoConfigure(null);

        setIfNotBlank(config::setMasterUrl, pluginSettings.getClusterUrl());
        setIfNotBlank(config::setNamespace, pluginSettings.getNamespace());
        setIfNotBlank(config::setOauthToken, pluginSettings.getSecurityToken());
        setIfNotBlank(config::setCaCertData, pluginSettings.getCaCertData());
        config.setRequestTimeout(pluginSettings.getClusterRequestTimeout());

        return new CachedClient(new KubernetesClientBuilder().withConfig(config).build(), pluginSettings);
    }

    public synchronized void clearOutExistingClient() {
        if (this.client != null) {
            this.client.closeIfUnused();
            this.client = null;
        }
    }

    private long getKubernetesClientRecycleInterval() {
        //if the value is already read, send it..
        if (this.kubernetesClientRecycleIntervalInMinutes != -1) {
            return this.kubernetesClientRecycleIntervalInMinutes;
        }

        String property = System.getProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY);
        if (isBlank(property)) {
            //set default to 10 minutes when system property is not specified
            this.kubernetesClientRecycleIntervalInMinutes = 10;
            return this.kubernetesClientRecycleIntervalInMinutes;
        }

        try {
            this.kubernetesClientRecycleIntervalInMinutes = Integer.parseInt(property);
        } catch (Exception e) {
            //set default value to 10 minutes when parsing user input fails
            this.kubernetesClientRecycleIntervalInMinutes = 10;
        }

        return this.kubernetesClientRecycleIntervalInMinutes;
    }

    public class CachedClient implements AutoCloseable {

        private final KubernetesClient client;
        private final PluginSettings clusterProfileConfigurations;
        private final AtomicInteger leases = new AtomicInteger(1);
        private final long clientCreatedTime;
        private volatile boolean closed;

        CachedClient(KubernetesClient client, PluginSettings clusterProfileConfigurations) {
            this.client = client;
            this.clusterProfileConfigurations = clusterProfileConfigurations;
            this.clientCreatedTime = KubernetesClientFactory.this.clock.now().toEpochMilli();
        }

        public KubernetesClient get() {
            return client;
        }

        public int leases() {
            return leases.get();
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            releaseLease();
        }

        private void releaseLease() {
            // Close the client only if it is not the same as the one we have cached
            if (leases.decrementAndGet() == 0 && this != KubernetesClientFactory.this.client && !closed) {
                closeUnderlyingClient();
            }
        }

        public void closeIfUnused() {
            if (leases() == 0 && !closed) {
                closeUnderlyingClient();
            }
        }

        private void closeUnderlyingClient() {
            LOG.debug("Terminating existing kubernetes client...");
            client.close();
            closed = true;
        }
    }
}
