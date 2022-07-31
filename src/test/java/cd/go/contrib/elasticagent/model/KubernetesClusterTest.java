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

package cd.go.contrib.elasticagent.model;

import cd.go.contrib.elasticagent.Constants;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class KubernetesClusterTest {

    @Mock
    private NonNamespaceOperation<Node, NodeList, Resource<Node>> nodes;

    @Mock
    private MixedOperation<Pod, PodList, PodResource<Pod>> pods;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCreateKubernetesClusterObject() throws Exception {
        final KubernetesClient kubernetesClient = mock(KubernetesClient.class);

        when(nodes.list()).thenReturn(new NodeList());
        when(kubernetesClient.nodes()).thenReturn(nodes);

        when(pods.withLabel(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID)).thenReturn(pods);
        when(pods.list()).thenReturn(new PodList());
        when(kubernetesClient.pods()).thenReturn(pods);

        final KubernetesCluster cluster = new KubernetesCluster(kubernetesClient);

        verify(kubernetesClient, times(1)).nodes();
        verify(kubernetesClient, times(1)).pods();
    }
}
