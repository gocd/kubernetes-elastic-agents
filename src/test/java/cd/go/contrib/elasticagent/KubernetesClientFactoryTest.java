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

import com.google.gson.Gson;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.properties.SystemProperties;
import uk.org.webcompere.systemstubs.resource.Executable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagent.KubernetesClientFactory.CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.org.webcompere.systemstubs.resource.Resources.with;

public class KubernetesClientFactoryTest {
    private PluginSettings populatedSettings;
    private PluginSettings emptySettings;
    private KubernetesClientFactory factory;
    private Clock.TestClock clock;

    @TempDir Path tempDir;
    private Path tempTokenFile;

    @BeforeEach
    public void setUp() throws Exception {
        tempTokenFile = tempDir.resolve("token");
        Files.writeString(tempTokenFile, "some-other-token");

        System.clearProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY);
        final Map<String, Object> pluginSettingsMap = new HashMap<>();
        pluginSettingsMap.put("go_server_url", "https://foo.go.cd/go");
        pluginSettingsMap.put("auto_register_timeout", "13");
        pluginSettingsMap.put("pending_pods_count", "14");
        pluginSettingsMap.put("kubernetes_cluster_url", "https://cloud.example.com");
        pluginSettingsMap.put("security_token", "foo-token");
        pluginSettingsMap.put("namespace", "gocd");
        pluginSettingsMap.put("kubernetes_cluster_ca_cert", "ca-cert-data");
        pluginSettingsMap.put("cluster_request_timeout", "10000");

        clock = new Clock.TestClock();
        factory = new KubernetesClientFactory(clock);
        populatedSettings = PluginSettings.fromJSON(new Gson().toJson(pluginSettingsMap));
        emptySettings = PluginSettings.fromJSON("{}");
    }

    @Test
    public void shouldInitializeClientWithAllValuesSet() {
        try (KubernetesClient client = factory.client(populatedSettings)) {
            assertThat(client.getConfiguration())
                    .satisfies(config -> {
                        assertThat(config.getMasterUrl()).isEqualTo("https://cloud.example.com/");
                        assertThat(config.getAutoOAuthToken()).isNull();
                        assertThat(config.getOauthToken()).isEqualTo("foo-token");
                        assertThat(config.getNamespace()).isEqualTo("gocd");
                        assertThat(config.getCaCertData()).isEqualTo("ca-cert-data");
                        assertThat(config.getRequestTimeout()).isEqualTo(10000);
                    });
        }
    }

    @Test
    public void shouldAutoConfigureClientWithDefaults() throws Exception {
        withAutoConfigurationValuesAvailable().execute(() -> {
            try (KubernetesClient client = factory.client(emptySettings)) {
                assertThat(client.getConfiguration())
                        .satisfies(config -> {
                            assertThat(config.getMasterUrl()).isEqualTo("https://default.cluster:8443/");
                            assertThat(config.getAutoOAuthToken()).isEqualTo("some-other-token");
                            assertThat(config.getOauthToken()).isNull();
                            assertThat(config.getNamespace()).isEqualTo("some-namespace");
                            assertThat(config.getCaCertData()).isEqualTo("other-ca-cert-content");
                            assertThat(config.getRequestTimeout()).isEqualTo(10000);
                        });
            }
        });
    }

    @Test
    public void shouldInitializeClientWithOverridesForAutoConfiguredValues() throws Exception {
        withAutoConfigurationValuesAvailable().execute(() -> {
            try (KubernetesClient client = factory.client(populatedSettings)) {
                assertThat(client.getConfiguration())
                        .satisfies(config -> {
                            assertThat(config.getMasterUrl()).isEqualTo("https://cloud.example.com/");

                            // The auto token will be set, but client should always ignore it in preference for the other token
                            assertThat(config.getAutoOAuthToken()).isEqualTo("some-other-token");
                            assertThat(config.getOauthToken()).isEqualTo("foo-token");

                            assertThat(config.getNamespace()).isEqualTo("gocd");
                            assertThat(config.getCaCertData()).isEqualTo("ca-cert-data");
                            assertThat(config.getRequestTimeout()).isEqualTo(10000);
                        });
            }
        });
    }

    private Executable withAutoConfigurationValuesAvailable() {
        return with(new EnvironmentVariables()
                        .set(Config.KUBERNETES_SERVICE_HOST_PROPERTY, "default.cluster")
                        .set(Config.KUBERNETES_SERVICE_PORT_PROPERTY, "8443"),
                new SystemProperties()
                        .set(Config.KUBERNETES_CA_CERTIFICATE_DATA_SYSTEM_PROPERTY, "other-ca-cert-content")
                        .set(Config.KUBERNETES_AUTH_SERVICEACCOUNT_TOKEN_FILE_SYSTEM_PROPERTY, tempTokenFile.toAbsolutePath().toString())
                        .set(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, "some-namespace")
        );
    }

    @Test
    public void shouldReuseTheExistingClientIfNotTimeElapsed() {
        KubernetesClient client = factory.client(populatedSettings);

        clock.set(Instant.now().plus(1, ChronoUnit.MINUTES));
        KubernetesClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);

        clock.set(Instant.now().plus(2, ChronoUnit.MINUTES));
        KubernetesClient client3 = factory.client(populatedSettings);
        assertEquals(client, client3);

        clock.set(Instant.now().plus(5, ChronoUnit.MINUTES));
        KubernetesClient client4 = factory.client(populatedSettings);
        assertEquals(client, client4);

        clock.set(Instant.now().plus(9, ChronoUnit.MINUTES));
        KubernetesClient client5 = factory.client(populatedSettings);
        assertEquals(client, client5);
    }

    @Test
    public void shouldRecycleClientOnTimer() {
        KubernetesClient client = factory.client(populatedSettings);

        clock.set(Instant.now().plus(9, ChronoUnit.MINUTES));

        KubernetesClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);

        clock.set(Instant.now().plus(11, ChronoUnit.MINUTES));

        KubernetesClient clientAfterTimeElapse = factory.client(populatedSettings);
        assertNotEquals(client, clientAfterTimeElapse);
    }

    @Test
    public void shouldReadClientRecycleIntervalFromSystemProperty() {
        System.setProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY, "2");

        KubernetesClient client = factory.client(populatedSettings);

        clock.set(Instant.now().plus(1, ChronoUnit.MINUTES));
        KubernetesClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);

        clock.set(Instant.now().plus(3, ChronoUnit.MINUTES));
        KubernetesClient client3 = factory.client(populatedSettings);
        assertNotEquals(client, client3);
    }

    @Test
    public void shouldSetClientRecycleIntervalToDefaultValueWhenInvalidValueForSystemPropertyIsProvided() {
        System.setProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY, "two");

        KubernetesClient client = factory.client(populatedSettings);

        clock.set(Instant.now().plus(1, ChronoUnit.MINUTES));
        KubernetesClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);

        clock.set(Instant.now().plus(9, ChronoUnit.MINUTES));
        KubernetesClient client3 = factory.client(populatedSettings);
        assertEquals(client, client3);

        clock.set(Instant.now().plus(11, ChronoUnit.MINUTES));
        KubernetesClient client4 = factory.client(populatedSettings);
        assertNotEquals(client, client4);
    }

    @Test
    public void shouldAllowExplicitlyClearingClient() {
        KubernetesClient client = factory.client(populatedSettings);
        factory.clearOutExistingClient();
        KubernetesClient client2 = factory.client(populatedSettings);
        assertNotEquals(client, client2);
    }
}
