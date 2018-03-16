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
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagent.Constants.JOB_ID_LABEL_KEY;
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

    @Mock
    private MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> mockedOperation;

    @Mock
    private PodList podList;
    private HashMap<String, String> testProperties;

    @Before
    public void setUp() {
        initMocks(this);
        testProperties = new HashMap<>();
        when(mockCreateAgentRequest.properties()).thenReturn(testProperties);
        when(mockPluginSettings.getMaxPendingPods()).thenReturn(10);
        when(factory.client(mockPluginSettings)).thenReturn(mockKubernetesClient);
        JobIdentifier jobId = new JobIdentifier("test", 1L, "Test pipeline", "test name", "1", "test job", 100L);
        when(mockCreateAgentRequest.jobIdentifier()).thenReturn(jobId);

        when(mockKubernetesClient.pods()).thenReturn(mockedOperation);
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);
        when(mockedOperation.list()).thenReturn(podList);
        when(podList.getItems()).thenReturn(Collections.emptyList());
    }

    @Test
    public void shouldCreateKubernetesPodUsingPodYamlAndCacheCreatedInstance() {
        KubernetesInstance kubernetesInstance = new KubernetesInstance(new DateTime(), "test", "test-agent", new HashMap<>(), 100L, PodState.Running);
        when(mockKubernetesInstanceFactory.create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, true)).
                thenReturn(kubernetesInstance);

        testProperties.put("SpecifiedUsingPodConfiguration", "true");

        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory, mockKubernetesInstanceFactory);
        KubernetesInstance instance = agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        assertTrue(agentInstances.instanceExists(instance));
    }

    @Test
    public void shouldCreateKubernetesPodAndCacheCreatedInstance() {
        KubernetesInstance kubernetesInstance = new KubernetesInstance(new DateTime(), "test", "test-agent", new HashMap<>(), 100L, PodState.Running);
        when(mockKubernetesInstanceFactory.create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, false)).
                thenReturn(kubernetesInstance);
        testProperties.put("SpecifiedUsingPodConfiguration", "false");
        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory, mockKubernetesInstanceFactory);
        KubernetesInstance instance = agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        assertTrue(agentInstances.instanceExists(instance));
    }

    @Test
    public void shouldNotCreatePodWhenOutstandingRequestsExistForJobs() {
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

        final Map<String, String> labels = new HashMap<>();
        labels.put(JOB_ID_LABEL_KEY, jobId.getJobId().toString());
        labels.put(Constants.KUBERNETES_POD_KIND_LABEL_KEY, Constants.KUBERNETES_POD_KIND_LABEL_VALUE);

        final Pod pod = mock(Pod.class);
        final ObjectMeta objectMeta = mock(ObjectMeta.class);
        when(pod.getMetadata()).thenReturn(objectMeta);
        when(objectMeta.getLabels()).thenReturn(labels);
        when(objectMeta.getName()).thenReturn("test-agent");
        when(podList.getItems()).thenReturn(Arrays.asList(pod));
        when(mockKubernetesInstanceFactory.fromKubernetesPod(pod)).thenReturn(kubernetesInstance);

        agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        verify(mockKubernetesInstanceFactory, times(0)).create(any(), any(), any(), any(), any());
    }

    @Test
    public void shouldNotCreatePodsWhenOutstandingLimitOfPendingKubernetesPodsHasReached() {
        //set maximum pending pod count to 1
        when(mockPluginSettings.getMaxPendingPods()).thenReturn(1);

        //pending kubernetes pod
        KubernetesInstance kubernetesInstance = new KubernetesInstance(new DateTime(), "test", "test-agent", new HashMap<>(), 100L, PodState.Pending);
        when(mockKubernetesInstanceFactory.create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, false)).
                thenReturn(kubernetesInstance);
        testProperties.put("SpecifiedUsingPodConfiguration", "false");

        //first create agent request
        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory, mockKubernetesInstanceFactory);
        JobIdentifier jobId = new JobIdentifier("test", 1L, "Test pipeline", "test name", "1", "test job", 100L);
        when(mockCreateAgentRequest.jobIdentifier()).thenReturn(jobId);
        agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        verify(mockKubernetesInstanceFactory, times(1)).create(any(), any(), any(), any(), any());
        reset(mockKubernetesInstanceFactory);

        final Map<String, String> labels = new HashMap<>();
        labels.put(JOB_ID_LABEL_KEY, jobId.getJobId().toString());
        labels.put(Constants.KUBERNETES_POD_KIND_LABEL_KEY, Constants.KUBERNETES_POD_KIND_LABEL_VALUE);

        final Pod pod = mock(Pod.class);
        final ObjectMeta objectMeta = mock(ObjectMeta.class);
        when(pod.getMetadata()).thenReturn(objectMeta);
        when(objectMeta.getLabels()).thenReturn(labels);
        when(objectMeta.getName()).thenReturn("test-agent");
        when(podList.getItems()).thenReturn(Arrays.asList(pod));
        when(mockKubernetesInstanceFactory.fromKubernetesPod(pod)).thenReturn(kubernetesInstance);

        //second create agent request
        agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);
        verify(mockKubernetesInstanceFactory, times(0)).create(any(), any(), any(), any(), any());
    }

    @Test
    public void shouldSyncPodsStateFromClusterBeforeCreatingPod() {
        when(mockKubernetesInstanceFactory.create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, false)).
                thenReturn(new KubernetesInstance(new DateTime(), "test", "test-agent", new HashMap<>(), 100L, PodState.Running));

        final KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory, mockKubernetesInstanceFactory);

        agentInstances.create(mockCreateAgentRequest, mockPluginSettings, mockPluginRequest);

        InOrder inOrder = inOrder(mockKubernetesInstanceFactory, mockedOperation);
        inOrder.verify(mockedOperation).list();
        inOrder.verify(mockKubernetesInstanceFactory).create(mockCreateAgentRequest, mockPluginSettings, mockKubernetesClient, mockPluginRequest, false);
    }
}
