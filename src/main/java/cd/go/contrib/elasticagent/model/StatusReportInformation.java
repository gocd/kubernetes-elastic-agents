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

package cd.go.contrib.elasticagent.model;

import java.util.List;

public class StatusReportInformation {
    private KubernetesCluster cluster;
    private JobIdentifier identifier;

    public StatusReportInformation(KubernetesCluster cluster, JobIdentifier identifier) {
        this.cluster = cluster;
        this.identifier = identifier;
    }

    public List<KubernetesNode> getNodes() {
        return cluster.getNodes();
    }

    public String getJobInformation() {
        return identifier.representation();
    }
}
