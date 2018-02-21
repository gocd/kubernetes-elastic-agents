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
import cd.go.contrib.elasticagent.PluginSettings;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.executors.ValidateConfigurationExecutor;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class ValidatePluginSettingsRequest {
    @Expose
    @SerializedName("plugin-settings")
    private PluginSettingsMap pluginSettingsMap = new PluginSettingsMap();

    public ValidatePluginSettingsRequest() {
    }

    public static ValidatePluginSettingsRequest fromJSON(String json) {
        return GSON.fromJson(json, ValidatePluginSettingsRequest.class);
    }

    public RequestExecutor executor(PluginRequest pluginRequest) {
        return new ValidateConfigurationExecutor(this, pluginRequest);
    }

    public String get(String key) {
        if (pluginSettingsMap == null || pluginSettingsMap.get(key) == null) {
            return null;
        }

        return pluginSettingsMap.get(key).value;
    }

    public PluginSettings getPluginSettingsMap() {
        return pluginSettingsMap.toPluginSettings();
    }

    public void put(String key, String value) {
        pluginSettingsMap.put(key, new Value(value));
    }

    private static class PluginSettingsMap extends HashMap<String, Value> {
        private PluginSettings pluginSettings;

        public PluginSettings toPluginSettings() {
            if (pluginSettings != null) {
                return pluginSettings;
            }

            final JsonObject jsonObject = new JsonObject();
            for (String key : keySet()) {
                jsonObject.addProperty(key, get(key).value);
            }

            pluginSettings = PluginSettings.fromJSON(jsonObject.toString());
            return pluginSettings;
        }
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
