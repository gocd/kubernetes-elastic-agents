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

package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.*;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static cd.go.contrib.elasticagent.utils.Util.getSimpleDateFormat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServerPingRequestExecutorTest extends BaseTest {
    @Mock
    KubernetesClientFactory factory;
    @Mock
    private KubernetesClient mockedClient;
    @Mock
    private MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> mockedOperation;
    @Mock
    private Pod mockedPod;
    @Mock
    private PodResource<Pod, DoneablePod> podResource;
    private ObjectMeta objectMetadata;

    @Before
    public void setUp() {
        initMocks(this);
        when(factory.client(any(PluginSettings.class))).thenReturn(mockedClient);
        when(mockedClient.pods()).thenReturn(mockedOperation);
        when(mockedOperation.create(any(Pod.class))).thenAnswer(new Answer<Pod>() {
            @Override
            public Pod answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Pod) args[0];
            }
        });

        when(mockedOperation.withName(anyString())).thenReturn(podResource);
        when(podResource.get()).thenReturn(mockedPod);

        objectMetadata = new ObjectMeta();
        objectMetadata.setCreationTimestamp(getSimpleDateFormat().format(new Date()));

        when(mockedPod.getMetadata()).thenReturn(objectMetadata);
    }

    @Test
    public void testShouldDisableIdleAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(Arrays.asList(new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled)));
        AgentInstances<KubernetesInstance> agentInstances = new KubernetesAgentInstances(factory);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);

        final Collection<Agent> values = agents.agents();
        new ServerPingRequestExecutor(agentInstances, pluginRequest).execute();
        verify(pluginRequest).disableAgents(argThat(collectionMatches(values)));
    }

    private ArgumentMatcher<Collection<Agent>> collectionMatches(final Collection<Agent> values) {
        return new ArgumentMatcher<Collection<Agent>>() {
            @Override
            public boolean matches(Collection<Agent> argument) {
                return new ArrayList<>(argument).equals(new ArrayList<>(values));
            }
        };
    }

    @Test
    public void testShouldTerminateDisabledAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(Arrays.asList(new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Disabled)));
        AgentInstances<KubernetesInstance> agentInstances = new KubernetesAgentInstances(factory);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);

        new ServerPingRequestExecutor(agentInstances, pluginRequest).execute();
        final Collection<Agent> values = agents.agents();
        verify(pluginRequest).deleteAgents(argThat(collectionMatches(values)));
    }

    @Test
    public void testShouldTerminateInstancesThatNeverAutoRegistered() throws Exception {
        KubernetesAgentInstances agentInstances = new KubernetesAgentInstances(factory);
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "foo");
        KubernetesInstance container = agentInstances.create(new CreateAgentRequest(null, properties, null, new JobIdentifier(1L)), createSettings(), null);

        agentInstances.clock = new Clock.TestClock().forward(Period.minutes(11));
        PluginRequest pluginRequest = mock(PluginRequest.class);

        objectMetadata.setName(container.name());
        HashMap<String, String> labels = new HashMap<>();
        labels.put(Constants.JOB_ID_LABEL_KEY, "1");
        objectMetadata.setLabels(labels);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        when(pluginRequest.listAgents()).thenReturn(new Agents());
        verifyNoMoreInteractions(pluginRequest);

        new ServerPingRequestExecutor(agentInstances, pluginRequest).execute();
        assertFalse(agentInstances.instanceExists(container));
    }

    @Test
    public void shouldDeleteAgentFromConfigWhenCorrespondingContainerIsNotPresent() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        when(pluginRequest.listAgents()).thenReturn(new Agents(Arrays.asList(new Agent("foo", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled))));
        verifyNoMoreInteractions(pluginRequest);

        AgentInstances<KubernetesInstance> agentInstances = new KubernetesAgentInstances(factory);
        ServerPingRequestExecutor serverPingRequestExecutor = new ServerPingRequestExecutor(agentInstances, pluginRequest);
        serverPingRequestExecutor.execute();
    }
}
