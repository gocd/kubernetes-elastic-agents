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

package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.*;
import cd.go.contrib.elasticagent.requests.ServerPingRequest;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import java.time.Instant;
import java.util.*;

import static cd.go.contrib.elasticagent.Constants.ENVIRONMENT_LABEL_KEY;
import static cd.go.contrib.elasticagent.Constants.JOB_ID_LABEL_KEY;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class ServerPingRequestExecutorTest extends BaseTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    KubernetesClientFactory factory;
    @Mock
    private KubernetesClient mockedClient;
    @Mock
    private MixedOperation<Pod, PodList, PodResource> mockedOperation;
    @Mock
    private Pod mockedPod;
    @Mock
    private PodResource podResource;
    private ObjectMeta objectMetadata;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        when(factory.client(any()).get()).thenReturn(mockedClient);
        when(mockedClient.pods()).thenReturn(mockedOperation);
        when(mockedOperation.resource(any(Pod.class))).thenAnswer((Answer<PodResource>) invocation -> {
            Object[] args = invocation.getArguments();
            Pod pod = (Pod) args[0];

            when(podResource.create()).thenReturn(pod);

            return podResource;
        });

        when(mockedOperation.withName(anyString())).thenReturn(podResource);
        when(podResource.get()).thenReturn(mockedPod);

        objectMetadata = new ObjectMeta();
        objectMetadata.setCreationTimestamp(Constants.KUBERNETES_POD_CREATION_TIME_FORMAT.format(Instant.now()));

        when(mockedPod.getMetadata()).thenReturn(objectMetadata);

        final PodList podList = mock(PodList.class);
        when(mockedOperation.list()).thenReturn(podList);
        when(podList.getItems()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testShouldTerminateKubernetesPodsRunningAfterTimeout_forSingleCluster() throws Exception {
        String agentId1 = UUID.randomUUID().toString();
        String agentId2 = UUID.randomUUID().toString();
        String agentId3 = UUID.randomUUID().toString();

        ClusterProfileProperties clusterProfileProperties = createClusterProfileProperties();

        Agent agent1 = new Agent(agentId1, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent agent1AfterDisabling = new Agent(agentId1, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Disabled); //idle time elapsed
        Agent agent2 = new Agent(agentId2, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle just created
        Agent agent3 = new Agent(agentId3, Agent.AgentState.Building, Agent.BuildState.Building, Agent.ConfigState.Enabled); //running time elapsed

        KubernetesInstance k8sPodForAgent1 = KubernetesInstance.builder().createdAt(Instant.now().minus(100, MINUTES)).environment("test").podName(agentId1).jobId(1L).podState(PodState.Running).build();
        KubernetesInstance k8sPodForAgent2 = KubernetesInstance.builder().createdAt(Instant.now()).environment("test").podName(agentId2).jobId(2L).podState(PodState.Running).build();
        KubernetesInstance k8sPodForAgent3 = KubernetesInstance.builder().createdAt(Instant.now().minus(100, MINUTES)).environment("test").podName(agentId3).jobId(3L).podState(PodState.Running).build();

        final Agents allAgentsInitially = new Agents(Arrays.asList(agent1, agent2, agent3));
        final Agents allAgentsAfterDisablingIdleAgents = new Agents(Arrays.asList(agent1AfterDisabling, agent2, agent3));

        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory);
        agentInstances.register(k8sPodForAgent1);
        agentInstances.register(k8sPodForAgent2);
        agentInstances.register(k8sPodForAgent3);

        HashMap<String, KubernetesAgentInstances> clusterSpecificInstances = new HashMap<>();
        clusterSpecificInstances.put(clusterProfileProperties.uuid(), agentInstances);

        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Collections.singletonList(clusterProfileProperties));

        PluginRequest pluginRequest = mock(PluginRequest.class);

        when(pluginRequest.listAgents()).thenReturn(allAgentsInitially, allAgentsAfterDisablingIdleAgents, new Agents());

        assertTrue(clusterSpecificInstances.get(clusterProfileProperties.uuid()).hasInstance(k8sPodForAgent1.getPodName()));

        new ServerPingRequestExecutor(serverPingRequest.allClusterProfileProperties(), clusterSpecificInstances, pluginRequest).execute();

        verify(pluginRequest, atLeastOnce()).disableAgents(Collections.singletonList(agent1));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Collections.singletonList(agent1AfterDisabling));
        assertFalse(clusterSpecificInstances.get(clusterProfileProperties.uuid()).hasInstance(k8sPodForAgent1.getPodName()));
    }

    @Test
    public void testShouldTerminateKubernetesPodsRunningAfterTimeout_forMultipleClusters() throws Exception {
        String agentId1 = "agentId1-" + UUID.randomUUID();
        String agentId2 = "agentId2-" + UUID.randomUUID();
        String agentId3 = "agentId3-" + UUID.randomUUID();

        String agentId4 = "agentId4-" + UUID.randomUUID();
        String agentId5 = "agentId5-" + UUID.randomUUID();
        String agentId6 = "agentId6-" + UUID.randomUUID();

        ClusterProfileProperties clusterProfilePropertiesForCluster1 = new ClusterProfileProperties("https://localhost:8154/go", null, null);
        ClusterProfileProperties clusterProfilePropertiesForCluster2 = new ClusterProfileProperties("https://localhost:8254/go", null, null);

        Agent agent1 = new Agent(agentId1, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent agent1AfterDisabling = new Agent(agentId1, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Disabled); //idle time elapsed
        Agent agent2 = new Agent(agentId2, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle just created
        Agent agent3 = new Agent(agentId3, Agent.AgentState.Building, Agent.BuildState.Building, Agent.ConfigState.Enabled); //running time elapsed

        Agent agent4 = new Agent(agentId4, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent agent4AfterDisabling = new Agent(agentId4, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Disabled); //idle time elapsed
        Agent agent5 = new Agent(agentId5, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle just created
        Agent agent6 = new Agent(agentId6, Agent.AgentState.Building, Agent.BuildState.Building, Agent.ConfigState.Enabled); //running time elapsed

        KubernetesInstance k8sPodForAgent1 = KubernetesInstance.builder()
                .createdAt(Instant.now().minus(100, MINUTES)).environment("test").podName(agentId1).jobId(1L).podState(PodState.Running).build();
        KubernetesInstance k8sPodForAgent2 = KubernetesInstance.builder()
                .createdAt(Instant.now()).environment("test").podName(agentId2).jobId(2L).podState(PodState.Running).build();
        KubernetesInstance k8sPodForAgent3 = KubernetesInstance.builder()
                .createdAt(Instant.now().minus(100, MINUTES)).environment("test").podName(agentId3).jobId(3L).podState(PodState.Running).build();

        KubernetesInstance k8sPodForAgent4 = KubernetesInstance.builder()
                .createdAt(Instant.now().minus(100, MINUTES)).environment("test").podName(agentId4).jobId(1L).podState(PodState.Running).build();
        KubernetesInstance k8sPodForAgent5 = KubernetesInstance.builder()
                .createdAt(Instant.now()).environment("test").podName(agentId5).jobId(2L).podState(PodState.Running).build();
        KubernetesInstance k8sPodForAgent6 = KubernetesInstance.builder()
                .createdAt(Instant.now().minus(100, MINUTES)).environment("test").podName(agentId6).jobId(3L).podState(PodState.Running).build();

        final Agents allAgentsInitially = new Agents(Arrays.asList(agent1, agent2, agent3, agent4, agent5, agent6));
        final Agents allAgentsAfterDisablingIdleAgentsFromCluster1 = new Agents(Arrays.asList(agent1AfterDisabling, agent2, agent3, agent4, agent5, agent6));
        final Agents allAgentsAfterTerminatingIdleAgentsFromCluster1 = new Agents(Arrays.asList(agent2, agent3, agent4, agent5, agent6));
        final Agents allAgentsAfterDisablingIdleAgentsFromCluster2 = new Agents(Arrays.asList(agent2, agent3, agent4AfterDisabling, agent5, agent6));
        final Agents allAgentsAfterTerminatingIdleAgentsFromCluster2 = new Agents(Arrays.asList(agent2, agent3, agent5, agent6));

        KubernetesAgentInstances agentInstancesForCluster1 = new KubernetesAgentInstances(factory);
        agentInstancesForCluster1.register(k8sPodForAgent1);
        agentInstancesForCluster1.register(k8sPodForAgent2);
        agentInstancesForCluster1.register(k8sPodForAgent3);

        KubernetesAgentInstances agentInstancesForCluster2 = new KubernetesAgentInstances(factory);
        agentInstancesForCluster2.register(k8sPodForAgent4);
        agentInstancesForCluster2.register(k8sPodForAgent5);
        agentInstancesForCluster2.register(k8sPodForAgent6);

        HashMap<String, KubernetesAgentInstances> clusterSpecificInstances = new HashMap<>();
        clusterSpecificInstances.put(clusterProfilePropertiesForCluster1.uuid(), agentInstancesForCluster1);
        clusterSpecificInstances.put(clusterProfilePropertiesForCluster2.uuid(), agentInstancesForCluster2);

        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Arrays.asList(clusterProfilePropertiesForCluster1, clusterProfilePropertiesForCluster2));

        PluginRequest pluginRequest = mock(PluginRequest.class);

        when(pluginRequest.listAgents()).thenReturn(allAgentsInitially, allAgentsAfterDisablingIdleAgentsFromCluster1, allAgentsAfterTerminatingIdleAgentsFromCluster1, allAgentsAfterDisablingIdleAgentsFromCluster2, allAgentsAfterTerminatingIdleAgentsFromCluster2, new Agents());

        assertTrue(clusterSpecificInstances.get(clusterProfilePropertiesForCluster1.uuid()).hasInstance(k8sPodForAgent1.getPodName()));
        assertTrue(clusterSpecificInstances.get(clusterProfilePropertiesForCluster2.uuid()).hasInstance(k8sPodForAgent4.getPodName()));

        new ServerPingRequestExecutor(serverPingRequest.allClusterProfileProperties(), clusterSpecificInstances, pluginRequest).execute();

        verify(pluginRequest, atLeastOnce()).disableAgents(Collections.singletonList(agent1));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Collections.singletonList(agent1AfterDisabling));

        verify(pluginRequest, atLeastOnce()).disableAgents(Collections.singletonList(agent4));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Collections.singletonList(agent4AfterDisabling));

        assertFalse(clusterSpecificInstances.get(clusterProfilePropertiesForCluster1.uuid()).hasInstance(k8sPodForAgent1.getPodName()));
        assertFalse(clusterSpecificInstances.get(clusterProfilePropertiesForCluster2.uuid()).hasInstance(k8sPodForAgent4.getPodName()));
    }

    @Test
    public void testShouldTerminateUnregisteredInstances_forSingleCluster() throws Exception {
        String unregisteredAgentId1 = "unregisteredAgentId1-" + UUID.randomUUID();
        String unregisteredAgentId2 = "unregisteredAgentId2-" + UUID.randomUUID();

        long time = Calendar.getInstance().getTimeInMillis();
        Pod mockedPod = mock(Pod.class);
        when(mockedOperation.withName(anyString())).thenReturn(podResource);
        when(podResource.get()).thenReturn(mockedPod);
        objectMetadata = new ObjectMeta();
        objectMetadata.setLabels(Map.of(JOB_ID_LABEL_KEY, "20", ENVIRONMENT_LABEL_KEY, "test"));
        objectMetadata.setName(unregisteredAgentId1);
        objectMetadata.setCreationTimestamp(Constants.KUBERNETES_POD_CREATION_TIME_FORMAT.format(Instant.now().minus(20, MINUTES)));

        when(mockedPod.getMetadata()).thenReturn(objectMetadata);

        ClusterProfileProperties clusterProfilePropertiesForCluster1 = new ClusterProfileProperties("https://localhost:8154/go", null, null);

        KubernetesInstance k8sUnregisteredCluster1Pod1 = KubernetesInstance.builder()
                .createdAt(Instant.now().minus(100, MINUTES)).environment("test").podName(unregisteredAgentId1).jobId(3L).podState(PodState.Running).build();
        KubernetesInstance k8sUnregisteredCluster1Pod2 = KubernetesInstance.builder()
                .createdAt(Instant.now()).environment("test").podName(unregisteredAgentId2).jobId(3L).podState(PodState.Running).build();

        final Agents allAgentsInitially = new Agents();

        KubernetesAgentInstances agentInstancesForCluster1 = new KubernetesAgentInstances(factory);
        agentInstancesForCluster1.register(k8sUnregisteredCluster1Pod1);
        agentInstancesForCluster1.register(k8sUnregisteredCluster1Pod2);

        HashMap<String, KubernetesAgentInstances> clusterSpecificInstances = new HashMap<>();
        clusterSpecificInstances.put(clusterProfilePropertiesForCluster1.uuid(), agentInstancesForCluster1);

        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(List.of(clusterProfilePropertiesForCluster1));

        PluginRequest pluginRequest = mock(PluginRequest.class);

        when(pluginRequest.listAgents()).thenReturn(allAgentsInitially);

        assertTrue(clusterSpecificInstances.get(clusterProfilePropertiesForCluster1.uuid()).hasInstance(k8sUnregisteredCluster1Pod1.getPodName()));
        assertTrue(clusterSpecificInstances.get(clusterProfilePropertiesForCluster1.uuid()).hasInstance(k8sUnregisteredCluster1Pod2.getPodName()));

        new ServerPingRequestExecutor(serverPingRequest.allClusterProfileProperties(), clusterSpecificInstances, pluginRequest).execute();

        assertFalse(clusterSpecificInstances.get(clusterProfilePropertiesForCluster1.uuid()).hasInstance(k8sUnregisteredCluster1Pod1.getPodName()));
        assertTrue(clusterSpecificInstances.get(clusterProfilePropertiesForCluster1.uuid()).hasInstance(k8sUnregisteredCluster1Pod2.getPodName()));
    }

    @Test
    public void testShouldDisableAndDeleteMissingAgents() throws Exception {
        ClusterProfileProperties clusterProfilePropertiesForCluster1 = new ClusterProfileProperties("https://localhost:8154/go", null, null);

        Agent agent1 = new Agent("agent1", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent agent2 = new Agent("agent2", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle just created

        final Agents allAgents = new Agents(Arrays.asList(agent1, agent2));

        KubernetesAgentInstances agentInstancesForCluster1 = new KubernetesAgentInstances(factory);

        HashMap<String, KubernetesAgentInstances> clusterSpecificInstances = new HashMap<>();
        clusterSpecificInstances.put(clusterProfilePropertiesForCluster1.uuid(), agentInstancesForCluster1);

        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(List.of(clusterProfilePropertiesForCluster1));

        PluginRequest pluginRequest = mock(PluginRequest.class);

        when(pluginRequest.listAgents()).thenReturn(allAgents);

        new ServerPingRequestExecutor(serverPingRequest.allClusterProfileProperties(), clusterSpecificInstances, pluginRequest).execute();

        verify(pluginRequest, atLeastOnce()).disableAgents(Arrays.asList(agent2, agent1));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Arrays.asList(agent2, agent1));
    }

    @Test
    public void shouldRefreshPodsForAllClusters() throws Exception {
        KubernetesAgentInstances agentInstancesForCluster1 = mock(KubernetesAgentInstances.class);
        when(agentInstancesForCluster1.listAgentPods(any())).thenReturn(Collections.emptyList());

        KubernetesAgentInstances agentInstancesForCluster2 = mock(KubernetesAgentInstances.class);
        when(agentInstancesForCluster2.listAgentPods(any())).thenReturn(Collections.emptyList());

        ClusterProfileProperties clusterProfilePropertiesForCluster1 = new ClusterProfileProperties("https://localhost:8154/go", "https://cluster1", null);
        ClusterProfileProperties clusterProfilePropertiesForCluster2 = new ClusterProfileProperties("https://localhost:8154/go", "https://cluster2", null);

        Map<String, KubernetesAgentInstances> clusterSpecificInstances = Map.of(
                clusterProfilePropertiesForCluster1.uuid(), agentInstancesForCluster1,
                clusterProfilePropertiesForCluster2.uuid(), agentInstancesForCluster2
        );

        List<ClusterProfileProperties> allClusterProps = List.of(clusterProfilePropertiesForCluster1, clusterProfilePropertiesForCluster2);

        PluginRequest pluginRequest = mock(PluginRequest.class);

        // Use spy to disable methods we're not testing
        ServerPingRequestExecutor spy = spy(new ServerPingRequestExecutor(allClusterProps, clusterSpecificInstances, pluginRequest));
        doNothing().when(spy).performCleanupForACluster(any(), any());
        doNothing().when(spy).checkForPossiblyMissingAgents();
        spy.execute();
        verify(agentInstancesForCluster1, times(1)).refreshAll(clusterProfilePropertiesForCluster1);
        verify(agentInstancesForCluster2, times(1)).refreshAll(clusterProfilePropertiesForCluster2);
    }

    @Test
    public void shouldInitializeInstancesAndRefreshPodsForNewClusters() throws Exception {
        ClusterProfileProperties clusterProfilePropertiesForCluster1 = new ClusterProfileProperties("https://localhost:8154/go", "https://cluster1", null);
        List<ClusterProfileProperties> allClusterProps = List.of(clusterProfilePropertiesForCluster1);
        Map<String, KubernetesAgentInstances> clusterSpecificInstances = new HashMap<>();
        KubernetesAgentInstances agentInstancesForCluster1 = mock(KubernetesAgentInstances.class);
        PluginRequest pluginRequest = mock(PluginRequest.class);

        // Use spy to disable methods we're not testing
        ServerPingRequestExecutor spy = spy(new ServerPingRequestExecutor(allClusterProps, clusterSpecificInstances, pluginRequest));
        doNothing().when(spy).performCleanupForACluster(any(), any());
        doNothing().when(spy).checkForPossiblyMissingAgents();
        when(spy.newKubernetesInstances()).thenReturn(agentInstancesForCluster1);
        spy.execute();

        verify(spy, times(1)).newKubernetesInstances();
        assertThat(clusterSpecificInstances).isEqualTo(Map.of(clusterProfilePropertiesForCluster1.uuid(), agentInstancesForCluster1));
        verify(agentInstancesForCluster1, times(1)).refreshAll(clusterProfilePropertiesForCluster1);
    }
}
