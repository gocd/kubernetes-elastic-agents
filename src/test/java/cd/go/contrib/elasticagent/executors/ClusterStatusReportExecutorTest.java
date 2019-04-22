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

import cd.go.contrib.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagent.model.KubernetesCluster;
import cd.go.contrib.elasticagent.requests.ClusterStatusReportRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.internal.NodeOperationsImpl;
import io.fabric8.kubernetes.client.dsl.internal.PodOperationsImpl;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterStatusReportExecutorTest {
    private KubernetesClientFactory kubernetesClientFactory;
    private ClusterStatusReportRequest request;
    private ClusterProfileProperties clusterProfileProperties;
    private KubernetesClient kubernetesClient;

    @Before
    public void setUp() throws Exception {
        kubernetesClientFactory = mock(KubernetesClientFactory.class);
        request = mock(ClusterStatusReportRequest.class);
        clusterProfileProperties = mock(ClusterProfileProperties.class);
        kubernetesClient = mock(KubernetesClient.class);

        when(request.clusterProfileProperties()).thenReturn(clusterProfileProperties);
        when(kubernetesClientFactory.client(clusterProfileProperties)).thenReturn(kubernetesClient);
    }

    @Test
    public void shouldBuildStatusReportView() throws Exception {
        NodeOperationsImpl nodes = mock(NodeOperationsImpl.class);
        PodOperationsImpl pods = mock(PodOperationsImpl.class);

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

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("{\"view\":\"status-report\"}"));
    }
}
