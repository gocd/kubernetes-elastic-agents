/*
 * Copyright 2019 ThoughtWorks, Inc.
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

import com.google.gson.Gson;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagent.KubernetesClientFactory.CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class KubernetesClientFactoryTest {
    private PluginSettings pluginSettings;
    private KubernetesClientFactory factory;
    private Clock.TestClock clock;

    @Before
    public void setUp() throws Exception {
        System.clearProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY);
        final Map<String, Object> pluginSettingsMap = new HashMap<>();
        pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
        pluginSettingsMap.put("auto_register_timeout", "13");
        pluginSettingsMap.put("pending_pods_count", "14");
        pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
        pluginSettingsMap.put("security_token", "foo-token");
        pluginSettingsMap.put("namespace", "gocd");

        clock = new Clock.TestClock();
        factory = new KubernetesClientFactory(clock);
        pluginSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));
    }

    @Test
    public void shouldInitializeClient() {
        KubernetesClient client = factory.client(pluginSettings);
    }

    @Test
    public void shouldReuseTheExistingClientIfNotTimeElapsed() {
        KubernetesClient client = factory.client(pluginSettings);

        clock.set(new DateTime().plusMinutes(1));
        KubernetesClient client2 = factory.client(pluginSettings);
        assertEquals(client, client2);

        clock.set(new DateTime().plusMinutes(2));
        KubernetesClient client3 = factory.client(pluginSettings);
        assertEquals(client, client3);

        clock.set(new DateTime().plusMinutes(5));
        KubernetesClient client4 = factory.client(pluginSettings);
        assertEquals(client, client4);

        clock.set(new DateTime().plusMinutes(9));
        KubernetesClient client5 = factory.client(pluginSettings);
        assertEquals(client, client5);
    }

    @Test
    public void shouldRecycleClientOnTimer() {
        KubernetesClient client = factory.client(pluginSettings);

        clock.set(new DateTime().plusMinutes(9));

        KubernetesClient client2 = factory.client(pluginSettings);
        assertEquals(client, client2);

        clock.set(new DateTime().plusMinutes(11));

        KubernetesClient clientAfterTimeElapse = factory.client(pluginSettings);
        assertNotEquals(client, clientAfterTimeElapse);
    }

    @Test
    public void shouldReadClientRecycleIntervalFromSystemProperty() {
        System.setProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY, "2");

        KubernetesClient client = factory.client(pluginSettings);

        clock.set(new DateTime().plusMinutes(1));
        KubernetesClient client2 = factory.client(pluginSettings);
        assertEquals(client, client2);

        clock.set(new DateTime().plusMinutes(3));
        KubernetesClient client3 = factory.client(pluginSettings);
        assertNotEquals(client, client3);
    }

    @Test
    public void shouldSetClientRecycleIntervalToDefaultValueWhenInvalidValueForSystemPropertyIsProvided() {
        System.setProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY, "two");

        KubernetesClient client = factory.client(pluginSettings);

        clock.set(new DateTime().plusMinutes(1));
        KubernetesClient client2 = factory.client(pluginSettings);
        assertEquals(client, client2);

        clock.set(new DateTime().plusMinutes(9));
        KubernetesClient client3 = factory.client(pluginSettings);
        assertEquals(client, client3);

        clock.set(new DateTime().plusMinutes(11));
        KubernetesClient client4 = factory.client(pluginSettings);
        assertNotEquals(client, client4);
    }

    @Test
    public void shouldAllowExplicitlyClearingClient() {
        KubernetesClient client = factory.client(pluginSettings);
        factory.clearOutExistingClient();
        KubernetesClient client2 = factory.client(pluginSettings);
        assertNotEquals(client, client2);
    }
}
