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

import static cd.go.contrib.elasticagent.utils.Util.GSON;

import java.util.Map;

import com.google.gson.reflect.TypeToken;

import cd.go.contrib.elasticagent.ElasticProfileFactory;
import cd.go.contrib.elasticagent.KubernetesClientFactory;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.executors.ProfileValidateRequestExecutor;

public class ProfileValidateRequest {
    private Map<String, String> properties;
    
    public ProfileValidateRequest(Map<String, String> properties) {
        this.properties = properties;
    }

    public static ProfileValidateRequest fromJSON(String json) {
        return new ProfileValidateRequest(GSON.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType()));
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public RequestExecutor executor(PluginRequest pluginRequest) {
        return new ProfileValidateRequestExecutor(this,pluginRequest, KubernetesClientFactory.instance(),ElasticProfileFactory.instance());
    }
    
    
}
