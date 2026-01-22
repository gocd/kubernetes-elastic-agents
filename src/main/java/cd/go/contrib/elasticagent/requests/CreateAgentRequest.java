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

package cd.go.contrib.elasticagent.requests;

import cd.go.contrib.elasticagent.*;
import cd.go.contrib.elasticagent.executors.CreateAgentRequestExecutor;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.fabric8.kubernetes.api.model.EnvVar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static cd.go.contrib.elasticagent.utils.Util.isBlank;

public class CreateAgentRequest {
    @Expose
    @SerializedName("auto_register_key")
    private String autoRegisterKey;
    @Expose
    @SerializedName("elastic_agent_profile_properties")
    private Map<String, String> elasticProfileProperties;
    @Expose
    @SerializedName("environment")
    private String environment;
    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;
    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public CreateAgentRequest() {
    }

    private CreateAgentRequest(String autoRegisterKey, Map<String, String> elasticProfileProperties, String environment) {
        this.autoRegisterKey = autoRegisterKey;
        this.elasticProfileProperties = elasticProfileProperties;
        this.environment = environment;
    }

    public CreateAgentRequest(String autoRegisterKey, Map<String, String> elasticProfileProperties, String environment, JobIdentifier identifier) {
        this(autoRegisterKey, elasticProfileProperties, environment);
        this.jobIdentifier = identifier;
    }

    public CreateAgentRequest(String autoRegisterKey, Map<String, String> elasticProfileProperties, String environment, JobIdentifier identifier, ClusterProfileProperties clusterProfileProperties) {
        this(autoRegisterKey, elasticProfileProperties, environment, identifier);
        this.clusterProfileProperties = clusterProfileProperties;
    }

    public static CreateAgentRequest fromJSON(String json) {
        return GSON.fromJson(json, CreateAgentRequest.class);
    }

    public String autoRegisterKey() {
        return autoRegisterKey;
    }

    public Map<String, String> elasticProfileProperties() {
        return elasticProfileProperties;
    }

    public String environment() {
        return environment;
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public ClusterProfileProperties clusterProfileProperties() {
        return clusterProfileProperties;
    }

    public RequestExecutor executor(AgentInstances<KubernetesInstance> agentInstances, PluginRequest pluginRequest) {
        return new CreateAgentRequestExecutor(this, agentInstances, pluginRequest);
    }

    public Collection<EnvVar> autoregisterPropertiesAsEnvironmentVars(String elasticAgentId) {
        ArrayList<EnvVar> vars = new ArrayList<>();
        if (!isBlank(autoRegisterKey)) {
            vars.add(new EnvVar("GO_EA_AUTO_REGISTER_KEY", autoRegisterKey, null));
        }
        if (!isBlank(environment)) {
            vars.add(new EnvVar("GO_EA_AUTO_REGISTER_ENVIRONMENT", environment, null));
        }
        vars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID", elasticAgentId, null));
        vars.add(new EnvVar("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID", Constants.PLUGIN_ID, null));
        return vars;
    }

    @Override
    public String toString() {
        return "CreateAgentRequest{" +
                "autoRegisterKey='" + autoRegisterKey + '\'' +
                ", properties=" + elasticProfileProperties +
                ", environment='" + environment + '\'' +
                ", jobIdentifier=" + jobIdentifier +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }
}
