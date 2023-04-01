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

package cd.go.contrib.elasticagent.builders;

import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.model.KubernetesCluster;
import cd.go.contrib.elasticagent.model.KubernetesNode;
import cd.go.contrib.elasticagent.model.KubernetesPod;
import freemarker.template.TemplateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginStatusReportViewBuilderTest {
    @Test
    public void shouldBuildStatusReportHtmlWithAgentStatusReportLink() throws IOException, TemplateException {
        KubernetesPod pod = mock(KubernetesPod.class);
        when(pod.getJobIdentifier()).thenReturn(new JobIdentifier(3243546575676657L));
        when(pod.getCreationTimestamp()).thenReturn(new Date());

        KubernetesNode node = mock(KubernetesNode.class);
        when(node.getPods()).thenReturn(singletonList(pod));

        KubernetesCluster cluster = mock(KubernetesCluster.class);
        when(cluster.getNodes()).thenReturn(singletonList(node));
        when(cluster.getPluginId()).thenReturn("cd.go.contrib.elastic.agent.kubernetes");
        PluginStatusReportViewBuilder builder = PluginStatusReportViewBuilder.instance();

        String build = builder.build(builder.getTemplate("status-report.template.ftlh"), cluster);

        Document document = Jsoup.parse(build);

        Element link = document.selectFirst("tbody tr td a");
        System.out.println(link);

        assertThat(link.attr("href")).isEqualTo("/go/admin/status_reports/cd.go.contrib.elastic.agent.kubernetes/agent/?job_id=3243546575676657");
    }

}
