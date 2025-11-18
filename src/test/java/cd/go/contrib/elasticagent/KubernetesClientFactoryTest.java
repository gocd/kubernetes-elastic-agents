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

        clock = new Clock.TestClock();
        factory = new KubernetesClientFactory(clock);
        populatedSettings = PluginSettings.fromJSON(new Gson().toJson(Map.<String, Object>of(
                "go_server_url", "https://foo.go.cd/go",
                "auto_register_timeout", "13",
                "pending_pods_count", "14",
                "kubernetes_cluster_url", "https://cloud.example.com",
                "security_token", "foo-token",
                "namespace", "gocd",
                "kubernetes_cluster_ca_cert", "ca-cert-data",
                "cluster_request_timeout", "10000"
        )));
        emptySettings = PluginSettings.fromJSON("{}");
    }

    @Test
    public void shouldInitializeClientWithAllValuesSet() {
        try (KubernetesClientFactory.CachedClient client = factory.client(populatedSettings)) {
            assertThat(client.get().getConfiguration())
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
            try (KubernetesClientFactory.CachedClient client = factory.client(emptySettings)) {
                assertThat(client.get().getConfiguration())
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
            try (KubernetesClientFactory.CachedClient client = factory.client(populatedSettings)) {
                assertThat(client.get().getConfiguration())
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
        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        assertThat(client.leases()).isEqualTo(1);
        client.close();
        assertThat(client.leases()).isEqualTo(0);

        clock.set(Instant.now().plus(1, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);
        client2.close();

        clock.set(Instant.now().plus(2, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client3 = factory.client(populatedSettings);
        assertEquals(client, client3);
        client3.close();

        clock.set(Instant.now().plus(5, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client4 = factory.client(populatedSettings);
        assertEquals(client, client4);
        client4.close();

        clock.set(Instant.now().plus(9, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client5 = factory.client(populatedSettings);
        assertEquals(client, client5);
        client5.close();
    }

    @Test
    public void shouldRecycleClientOnTimer() {
        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        assertThat(client.leases()).isEqualTo(1);
        client.close();
        assertThat(client.leases()).isEqualTo(0);

        clock.set(Instant.now().plus(9, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);
        client.close();
        assertThat(client.leases()).isEqualTo(0);

        clock.set(Instant.now().plus(11, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient clientAfterTimeElapse = factory.client(populatedSettings);
        assertNotEquals(client, clientAfterTimeElapse);
        clientAfterTimeElapse.close();
        assertThat(client.leases()).isEqualTo(0);
        assertThat(client.isClosed()).isTrue();
        assertThat(clientAfterTimeElapse.leases()).isEqualTo(0);
        assertThat(clientAfterTimeElapse.isClosed()).isFalse();
    }

    @Test
    public void shouldReadClientRecycleIntervalFromSystemProperty() {
        System.setProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY, "2");

        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        client.close();

        clock.set(Instant.now().plus(1, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);
        client.close();

        clock.set(Instant.now().plus(3, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient clientAfterTimeElapse = factory.client(populatedSettings);
        assertNotEquals(client, clientAfterTimeElapse);
        clientAfterTimeElapse.close();
        assertThat(client.leases()).isEqualTo(0);
        assertThat(client.isClosed()).isTrue();
        assertThat(clientAfterTimeElapse.leases()).isEqualTo(0);
        assertThat(clientAfterTimeElapse.isClosed()).isFalse();
    }

    private void changeCluster() {
        populatedSettings = PluginSettings.fromJSON(new Gson().toJson(Map.<String, Object>of(
                "go_server_url", "https://foo.go.cd/go",
                "auto_register_timeout", "13",
                "pending_pods_count", "14",
                "kubernetes_cluster_url", "https://cloud.example.com/" + Math.random(),
                "security_token", "foo-token",
                "namespace", "gocd",
                "kubernetes_cluster_ca_cert", "ca-cert-data",
                "cluster_request_timeout", "10000"
        )));
    }

    @Test
    public void shouldSetClientRecycleIntervalToDefaultValueWhenInvalidValueForSystemPropertyIsProvided() {
        System.setProperty(CLIENT_RECYCLE_SYSTEM_PROPERTY_KEY, "two");

        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        client.close();

        clock.set(Instant.now().plus(1, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);
        client.close();

        clock.set(Instant.now().plus(9, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient client3 = factory.client(populatedSettings);
        assertEquals(client, client3);
        client.close();

        clock.set(Instant.now().plus(11, ChronoUnit.MINUTES));
        KubernetesClientFactory.CachedClient clientAfterTimeElapse = factory.client(populatedSettings);
        assertNotEquals(client, clientAfterTimeElapse);
        clientAfterTimeElapse.close();
    }

    @Test
    public void shouldAllowExplicitlyClearingClient() {
        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        factory.clearOutExistingClient();
        KubernetesClientFactory.CachedClient client2 = factory.client(populatedSettings);
        assertNotEquals(client, client2);
        assertThat(client.isClosed()).isFalse();
    }

    @Test
    public void shouldNotCloseStaleClientWhenLeasesAreZeroIfCurrentCachedClient() {
        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        assertThat(client.leases()).isEqualTo(1);
        KubernetesClientFactory.CachedClient client2 = factory.client(populatedSettings);
        assertEquals(client, client2);
        assertThat(client.leases()).isEqualTo(2);
        client.close();
        client.close();
        assertThat(client.leases()).isEqualTo(0);
        assertThat(client.isClosed()).isFalse();
    }

    @Test
    public void shouldCloseStaleClientWhenLeasesAreZeroIfNotCurrentCachedClient() {
        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        assertThat(client.leases()).isEqualTo(1);
        assertEquals(client, factory.client(populatedSettings));
        assertThat(client.leases()).isEqualTo(2);
        client.close();
        assertThat(client.leases()).isEqualTo(1);
        client.close();
        assertThat(client.isClosed()).isFalse();
    }

    @Test
    public void shouldCloseStaleClientWhenSwappingClientImmediatelyIfLeasesAreZero() {
        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        client.close();
        assertThat(client.leases()).isEqualTo(0);
        assertThat(client.isClosed()).isFalse();

        changeCluster();
        assertNotEquals(client, factory.client(populatedSettings));
        assertThat(client.isClosed()).isTrue();
    }

    @Test
    public void shouldCloseStaleClientWhenSwappingClientOnlyWhenLeasesLaterBecomeZero() {
        KubernetesClientFactory.CachedClient client = factory.client(populatedSettings);
        assertThat(client.leases()).isEqualTo(1);
        assertThat(client.isClosed()).isFalse();

        changeCluster();
        assertNotEquals(client, factory.client(populatedSettings));
        assertThat(client.isClosed()).isFalse();

        client.close();
        assertThat(client.isClosed()).isTrue();
    }
}
