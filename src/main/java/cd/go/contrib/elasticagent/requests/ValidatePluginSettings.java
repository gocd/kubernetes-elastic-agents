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

import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.executors.ValidateConfigurationExecutor;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class ValidatePluginSettings {
    @Expose
    @SerializedName("plugin-settings")
    private PluginSettings pluginSettings = new PluginSettings();

    public ValidatePluginSettings() {
    }

    public static ValidatePluginSettings fromJSON(String json) {
        return GSON.fromJson(json, ValidatePluginSettings.class);
    }

    public RequestExecutor executor(PluginRequest pluginRequest) {
        return new ValidateConfigurationExecutor(this, pluginRequest);
    }

    public String get(String key) {
        if (pluginSettings == null || pluginSettings.get(key) == null) {
            return null;
        }

        return pluginSettings.get(key).value;
    }

    public void put(String key, String value) {
        pluginSettings.put(key, new Value(value));
    }

    private static class PluginSettings extends HashMap<String, Value> {
    }

    private static class Value {
        @Expose
        @SerializedName("value")
        private String value;

        public Value() {
        }

        public Value(String value) {
            this.value = value;
        }
    }
}
