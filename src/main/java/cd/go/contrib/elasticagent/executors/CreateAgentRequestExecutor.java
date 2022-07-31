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
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class CreateAgentRequestExecutor implements RequestExecutor {
    private static final DateTimeFormatter MESSAGE_PREFIX_FORMATTER = DateTimeFormat.forPattern("'##|'HH:mm:ss.SSS '[go]'");
    private final AgentInstances<KubernetesInstance> agentInstances;
    private final PluginRequest pluginRequest;
    private final CreateAgentRequest request;

    public CreateAgentRequestExecutor(CreateAgentRequest request, AgentInstances<KubernetesInstance> agentInstances, PluginRequest pluginRequest) {
        this.request = request;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        LOG.debug(format("[Create Agent] creating elastic agent for profile {0} in cluster {1}", request.properties(), request.clusterProfileProperties()));
        ConsoleLogAppender consoleLogAppender = text -> {
            final String message = String.format("%s %s\n", LocalTime.now().toString(MESSAGE_PREFIX_FORMATTER), text);
            pluginRequest.appendToConsoleLog(request.jobIdentifier(), message);
        };
        consoleLogAppender.accept(format("Received request to create a pod for job {0} in cluster {1} at {2}", request.jobIdentifier(), request.clusterProfileProperties().getClusterUrl(), new DateTime().toString("yyyy-MM-dd HH:mm:ss ZZ")));
        try {
            agentInstances.create(request, request.clusterProfileProperties(), pluginRequest, consoleLogAppender);
        } catch (Exception e) {
            consoleLogAppender.accept(String.format("Failed to create agent pod: %s", e.getMessage()));
            throw e;
        }

        return new DefaultGoPluginApiResponse(200);
    }

}
