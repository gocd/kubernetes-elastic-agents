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

package cd.go.contrib.elasticagent;

import com.thoughtworks.go.plugin.api.response.GoApiResponse;

import static java.text.MessageFormat.format;

public class ServerRequestFailedException extends RuntimeException {

    private ServerRequestFailedException(GoApiResponse response, String request) {
        super(format(
                "The server sent an unexpected status code {0} with the response body {1} when it was sent a {2} message",
                response.responseCode(), response.responseBody(), request
        ));
    }

    public static ServerRequestFailedException disableAgents(GoApiResponse response) {
        return new ServerRequestFailedException(response, "disable agents");
    }

    public static ServerRequestFailedException deleteAgents(GoApiResponse response) {
        return new ServerRequestFailedException(response, "delete agents");
    }

    public static ServerRequestFailedException listAgents(GoApiResponse response) {
        return new ServerRequestFailedException(response, "list agents");
    }

    public static ServerRequestFailedException getPluginSettings(GoApiResponse response) {
        return new ServerRequestFailedException(response, "get plugin settings");
    }

    public static ServerRequestFailedException serverInfo(GoApiResponse response) {
        return new ServerRequestFailedException(response, "get server info");
    }
}
