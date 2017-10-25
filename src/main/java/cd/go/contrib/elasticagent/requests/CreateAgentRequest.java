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

package cd.go.contrib.elasticagent.requests;

import cd.go.contrib.elasticagent.AgentInstances;
import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.executors.CreateAgentRequestExecutor;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.fabric8.kubernetes.api.model.EnvVar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CreateAgentRequest {
    @Expose
    @SerializedName("auto_register_key")
    private String autoRegisterKey;
    @Expose
    @SerializedName("properties")
    private Map<String, String> properties;
    @Expose
    @SerializedName("environment")
    private String environment;


    public CreateAgentRequest() {
    }

    public CreateAgentRequest(String autoRegisterKey, Map<String, String> properties, String environment) {
        this.autoRegisterKey = autoRegisterKey;
        this.properties = properties;
        this.environment = environment;
    }

    public static CreateAgentRequest fromJSON(String json) {
        return GSON.fromJson(json, CreateAgentRequest.class);
    }

    public String autoRegisterKey() {
        return autoRegisterKey;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public String environment() {
        return environment;
    }

    public RequestExecutor executor(AgentInstances agentInstances, PluginRequest pluginRequest) {
        return new CreateAgentRequestExecutor(this, agentInstances, pluginRequest);
    }

    public Collection<EnvVar> autoregisterPropertiesAsEnvironmentVars(String elasticAgentId) {
        ArrayList<EnvVar> vars = new ArrayList<>();
        if (isNotBlank(autoRegisterKey)) {
            vars.add(new EnvVar("GO_EA_AUTO_REGISTER_KEY", autoRegisterKey, null));
        }
        if (isNotBlank(environment)) {
            vars.add(new EnvVar("GO_EA_AUTO_REGISTER_ENVIRONMENT", environment, null));
        }
        vars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID", elasticAgentId, null));
        vars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID", Constants.PLUGIN_ID, null));
        return vars;
    }
}
