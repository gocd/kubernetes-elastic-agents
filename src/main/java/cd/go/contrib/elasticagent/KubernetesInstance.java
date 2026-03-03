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

import java.util.Collections;
import java.util.Map;
import java.time.Instant;

import io.fabric8.kubernetes.client.KubernetesClient;

public record KubernetesInstance(
        Instant createdAt,
        String environment,
        String podName,
        Map<String, String> podAnnotations,
        Long jobId,
        PodState podState,
        AgentState agentState) {

    /**
     * AgentState represents the possible agent states from the
     * GoCD server perspective - whether it is currently running a job,
     * ready to accept a new job, etc.
     */
    public enum AgentState {
        /**
         * Unknown means the agent hasn't yet been registered with the plugin.
         * For example, if the GoCD server restarted while a pod was building,
         * the state will be Unknown until the pod finishes its job.
         */
        Unknown,
        /**
         * Idle means the agent has just finished a job.
         */
        Idle,
        /**
         * Building means the agent has been assigned a job.
         */
        Building,
    }

    /**
     * ELASTIC_CONFIG_HASH is a pod annotation that contains a hash of the cluster profile
     * configuration and elastic profile configuration that were used to create the pod.
     */
    public static final String ELASTIC_CONFIG_HASH = "go.cd/elastic-config-hash";

    // TODO: static method
    public void terminate(KubernetesClient client) {
        client.pods().withName(this.podName).delete();
    }

    public boolean isPending() {
        return this.podState.equals(PodState.Pending);
    }

    public KubernetesInstance withAgentState(AgentState newAgentState) {
        return new KubernetesInstance(
                this.createdAt,
                this.environment,
                this.podName,
                this.podAnnotations,
                this.jobId,
                this.podState,
                newAgentState
        );
    }
}
