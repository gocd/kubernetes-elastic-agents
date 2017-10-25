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

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;

public class KubernetesClientFactory {
    private static final KubernetesClientFactory KUBERNETES_CLIENT_FACTORY = new KubernetesClientFactory();
    private KubernetesClient client;
    private PluginSettings pluginSettings;

    public static KubernetesClientFactory instance() {
        return KUBERNETES_CLIENT_FACTORY;
    }

    private static KubernetesClient createClient(PluginSettings pluginSettings) throws Exception {
        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(pluginSettings.getKubernetesClusterUrl());
        if (StringUtils.isNotBlank(pluginSettings.getKubernetesClusterUsername())) {
            configBuilder.withUsername(pluginSettings.getKubernetesClusterUsername());
        }

        if (StringUtils.isNotBlank(pluginSettings.getKubernetesClusterPassword())) {
            configBuilder.withPassword(pluginSettings.getKubernetesClusterPassword());
        }

        if (StringUtils.isNotBlank(pluginSettings.getKubernetesClusterCACert())) {
            configBuilder.withCaCertData(pluginSettings.getKubernetesClusterCACert());
        }

        Config build = configBuilder.build();
        return new DefaultKubernetesClient(build);
    }

    public synchronized KubernetesClient kubernetes(PluginSettings pluginSettings) throws Exception {
        if (pluginSettings.equals(this.pluginSettings) && this.client != null) {
            return this.client;
        }

        this.pluginSettings = pluginSettings;
        this.client = createClient(pluginSettings);
        return this.client;
    }
}