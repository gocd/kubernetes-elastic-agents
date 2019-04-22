/*
 * Copyright 2018 ThoughtWorks, Inc.
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

/**
 * Enumerable that represents one of the messages that the server sends to the plugin
 */
public enum Request {
    REQUEST_CREATE_AGENT(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".create-agent"),
    REQUEST_SERVER_PING(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".server-ping"),
    REQUEST_SHOULD_ASSIGN_WORK(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".should-assign-work"),
    REQUEST_JOB_COMPLETION(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".job-completion"),
    REQUEST_MIGRATE_CONFIGURATION(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".migrate-config"),

    PLUGIN_SETTINGS_GET_ICON(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".get-icon"),

    REQUEST_GET_CLUSTER_PROFILE_METADATA(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".get-cluster-profile-metadata"),
    REQUEST_VALIDATE_CLUSTER_PROFILE_CONFIGURATION(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".validate-cluster-profile"),
    REQUEST_GET_CLUSTER_PROFILE_VIEW(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".get-cluster-profile-view"),
    REQUEST_CLUSTER_PROFILE_CHANGED(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".cluster-profile-changed"),

    REQUEST_GET_ELASTIC_AGENT_PROFILE_METADATA(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".get-elastic-agent-profile-metadata"),
    REQUEST_VALIDATE_ELASTIC_AGENT_PROFILE(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".validate-elastic-agent-profile"),
    REQUEST_GET_ELASTIC_AGENT_PROFILE_VIEW(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".get-elastic-agent-profile-view"),

    REQUEST_GET_CAPABILITIES(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".get-capabilities"),
    REQUEST_ELASTIC_AGENT_STATUS_REPORT(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".agent-status-report"),
    REQUEST_CLUSTER_STATUS_REPORT(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".cluster-status-report");

    private final String requestName;

    Request(String requestName) {
        this.requestName = requestName;
    }

    public static Request fromString(String requestName) {
        if (requestName != null) {
            for (Request request : Request.values()) {
                if (requestName.equalsIgnoreCase(request.requestName)) {
                    return request;
                }
            }
        }

        return null;
    }

    private static class Constants {
        public static final String ELASTIC_AGENT_REQUEST_PREFIX = "cd.go.elastic-agent";
        public static final String GO_PLUGIN_SETTINGS_PREFIX = "go.plugin-settings";
    }
}
