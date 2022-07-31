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

public class PluginSettingsMother {
    public static PluginSettings defaultPluginSettings() {
        return PluginSettings.fromJSON("{" +
                "\"go_server_url\": \"https://foo.go.cd/go\", " +
                "\"auto_register_timeout\": \"10\", " +
                "\"kubernetes_cluster_url\": \"https://cloud.example.com\", " +
                "\"kubernetes_cluster_username\": \"admin\", " +
                "\"kubernetes_cluster_password\": \"password\", " +
                "\"kubernetes_cluster_ca_cert\": \"my awesome ca cert\" " +
                "}");
    }
}
