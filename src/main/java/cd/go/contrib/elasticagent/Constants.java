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

import cd.go.contrib.elasticagent.utils.Util;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;

import java.util.Collections;

public interface Constants {
    String PLUGIN_ID = Util.pluginId();

    // The type of this extension
    String EXTENSION_TYPE = "elastic-agent";

    // The extension point API version that this plugin understands
    String API_VERSION = "2.0";
    String SERVER_INFO_API_VERSION = "1.0";

    // the identifier of this plugin
    GoPluginIdentifier PLUGIN_IDENTIFIER = new GoPluginIdentifier(EXTENSION_TYPE, Collections.singletonList(API_VERSION));

    // requests that the plugin makes to the server
    String REQUEST_SERVER_PREFIX = "go.processor";
    String REQUEST_SERVER_DISABLE_AGENT = REQUEST_SERVER_PREFIX + ".elastic-agents.disable-agents";
    String REQUEST_SERVER_DELETE_AGENT = REQUEST_SERVER_PREFIX + ".elastic-agents.delete-agents";
    String REQUEST_SERVER_GET_PLUGIN_SETTINGS = REQUEST_SERVER_PREFIX + ".plugin-settings.get";
    String REQUEST_SERVER_LIST_AGENTS = REQUEST_SERVER_PREFIX + ".elastic-agents.list-agents";
    String REQUEST_SERVER_INFO = REQUEST_SERVER_PREFIX + ".server-info.get";

    // internal use only
    String CREATED_BY_LABEL_KEY = "Elastic-Agent-Created-By";
    String ENVIRONMENT_LABEL_KEY = "Elastic-Agent-Environment-Name";
    String JOB_ID_LABEL_KEY = "Elastic-Agent-Job-Id";
    String JOB_IDENTIFIER_LABEL_KEY = "Elastic-Agent-Job-Identifier";

    String KUBERNETES_NAMESPACE_KEY = "default";
    String KUBERNETES_POD_KIND_LABEL_KEY = "kind";
    String KUBERNETES_POD_KIND_LABEL_VALUE = "kubernetes-elastic-agent";
    String KUBERNETES_POD_NAME = "kubernetes-elastic-agent";
    String KUBERNETES_POD_CREATION_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    String POD_POSTFIX = "POD_POSTFIX";
    String CONTAINER_POSTFIX = "CONTAINER_POSTFIX";
    String GOCD_AGENT_IMAGE = "GOCD_AGENT_IMAGE";
    String LATEST_VERSION = "LATEST_VERSION";
}
