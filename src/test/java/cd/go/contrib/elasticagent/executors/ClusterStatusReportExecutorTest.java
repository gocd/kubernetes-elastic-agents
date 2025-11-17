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

import cd.go.contrib.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.KubernetesCluster;
import cd.go.contrib.elasticagent.requests.ClusterStatusReportRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
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
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


public class ClusterStatusReportExecutorTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KubernetesClientFactory kubernetesClientFactory;

    @Mock
    private ClusterStatusReportRequest request;

    @Mock
    private ClusterProfileProperties clusterProfileProperties;

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private NonNamespaceOperation<Node, NodeList, Resource<Node>> nodes;

    @Mock
    private MixedOperation<Pod, PodList, PodResource> pods;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        when(request.clusterProfileProperties()).thenReturn(clusterProfileProperties);
        when(kubernetesClientFactory.client(clusterProfileProperties).get()).thenReturn(kubernetesClient);
    }

    @Test
    public void shouldBuildStatusReportView() throws Exception {
        when(nodes.list()).thenReturn(new NodeList());
        when(kubernetesClient.nodes()).thenReturn(nodes);

        when(pods.withLabel(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID)).thenReturn(pods);
        when(pods.list()).thenReturn(new PodList());
        when(kubernetesClient.pods()).thenReturn(pods);

        final PluginStatusReportViewBuilder builder = mock(PluginStatusReportViewBuilder.class);
        final Template template = mock(Template.class);

        when(builder.getTemplate("status-report.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(KubernetesCluster.class))).thenReturn("status-report");

        final GoPluginApiResponse response = new ClusterStatusReportExecutor(request, builder, kubernetesClientFactory).execute();

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"view\":\"status-report\"}");
    }
}
