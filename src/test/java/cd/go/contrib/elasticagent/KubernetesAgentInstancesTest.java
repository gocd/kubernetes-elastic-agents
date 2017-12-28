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

import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class KubernetesAgentInstancesTest {
    @Mock
    KubernetesClientFactory factory;

    @Mock
    KubernetesInstanceFactory mockKubernetesInstanceFactory;

    @Mock
    CreateAgentRequest mockCreateAgentRequest;

    @Mock
    PluginSettings mockPluginSettings;

    @Mock
    KubernetesClient mockKubernetesClient;

    @Mock
    PluginRequest mockPluginRequest;

    private HashMap<String, String> testProperties;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        testProperties = new HashMap<>();
        when(mockCreateAgentRequest.properties()).thenReturn(testProperties);
        when(factory.kubernetes(mockPluginSettings)).thenReturn(mockKubernetesClient);
        JobIdentifier jobId = new JobIdentifier("test", 1L, "Test pipeline", "test name", "1", "test job", 100L);
        when(mockCreateAgentRequest.jobIdentifier()).thenReturn(jobId);
    }

    @Test
    public void shouldCreateKubernetesPodUsingPodYamlAndCacheCreatedInstance() throws Exception {
        KubernetesInstance kubernetesInstance = new KubernetesInstance(new DateTime(), "test", "test-agent", new HashMap<>(), 100L, PodState.Running);
        when(mockKubernetesInstanceFactory.create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, true)).
                thenReturn(kubernetesInstance);

        testProperties.put("SpecifiedUsingPodConfiguration", "true");

        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory, mockKubernetesInstanceFactory);
        KubernetesInstance instance = agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        assertTrue(agentInstances.instanceExists(instance));
    }

    @Test
    public void shouldCreateKubernetesPodAndCacheCreatedInstance() throws Exception {
        KubernetesInstance kubernetesInstance = new KubernetesInstance(new DateTime(), "test", "test-agent", new HashMap<>(), 100L, PodState.Running);
        when(mockKubernetesInstanceFactory.create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, false)).
                thenReturn(kubernetesInstance);
        testProperties.put("SpecifiedUsingPodConfiguration", "false");
        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory, mockKubernetesInstanceFactory);
        KubernetesInstance instance = agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        assertTrue(agentInstances.instanceExists(instance));
    }

    @Test
    public void shouldNotCreatePodWhenOutstandingRequestsExistForJobs() throws Exception {
        KubernetesInstance kubernetesInstance = new KubernetesInstance(new DateTime(), "test", "test-agent", new HashMap<>(), 100L, PodState.Running);
        when(mockKubernetesInstanceFactory.create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, false)).
                thenReturn(kubernetesInstance);
        testProperties.put("SpecifiedUsingPodConfiguration", "false");

        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory, mockKubernetesInstanceFactory);
        JobIdentifier jobId = new JobIdentifier("test", 1L, "Test pipeline", "test name", "1", "test job", 100L);
        when(mockCreateAgentRequest.jobIdentifier()).thenReturn(jobId);
        agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        verify(mockKubernetesInstanceFactory, times(1)).create(any(), any(), any(), any(), any());
        reset(mockKubernetesInstanceFactory);

        agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        verify(mockKubernetesInstanceFactory, times(0)).create(any(), any(), any(), any(), any());
    }

    @Test
    public void shouldNotCreatePodsWhenOutstandingLimitOfPendingKubernetesPodsHasReached() {

    }
}
