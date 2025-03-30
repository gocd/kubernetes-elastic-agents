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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.Duration;

import static cd.go.contrib.elasticagent.utils.Util.IntTypeAdapter;
import static cd.go.contrib.elasticagent.utils.Util.isBlank;

public class PluginSettings {
    @Expose
    @SerializedName("go_server_url")
    private String goServerUrl;

    @Expose
    @SerializedName("auto_register_timeout")
    private String autoRegisterTimeout;

    @Expose
    @SerializedName("pending_pods_count")
    private String maxPendingPods;

    @Expose
    @SerializedName("kubernetes_cluster_url")
    private String clusterUrl;

    @Expose
    @SerializedName("security_token")
    private String securityToken;

    @Expose
    @SerializedName("kubernetes_cluster_ca_cert")
    private String clusterCACertData;

    @Expose
    @SerializedName("namespace")
    private String namespace;

    @Expose
    @SerializedName("cluster_request_timeout")
    private String clusterRequestTimeout;

    @Expose
    @SerializedName("enable_agent_reuse")
    private boolean enableAgentReuse;

    private Duration autoRegisterPeriod;

    public PluginSettings() {
    }

    public PluginSettings(String goServerUrl, String clusterUrl, String clusterCACertData) {
        this.goServerUrl = goServerUrl;
        this.clusterUrl = clusterUrl;
        this.clusterCACertData = clusterCACertData;
    }

    public static PluginSettings fromJSON(String json) {
        GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.registerTypeAdapter(Integer.class, IntTypeAdapter);
        Gson gson = gsonBuilder.create();
        return gson.fromJson(json, PluginSettings.class);
    }

    public Duration getAutoRegisterPeriod() {
        if (this.autoRegisterPeriod == null) {
            this.autoRegisterPeriod = Duration.ofMinutes(getAutoRegisterTimeout());
        }
        return this.autoRegisterPeriod;
    }

    Integer getAutoRegisterTimeout() {
        Integer value = !isBlank(autoRegisterTimeout) ? Integer.valueOf(autoRegisterTimeout) : null;
        return getOrDefault(value, 10);
    }

    public Integer getMaxPendingPods() {
        Integer value = !isBlank(this.maxPendingPods) ? Integer.valueOf(this.maxPendingPods) : null;
        return getOrDefault(value, 10);
    }

    public String getGoServerUrl() {
        return goServerUrl;
    }

    public String getSecurityToken() {
        return getOrDefault(securityToken, null);
    }

    public String getClusterUrl() {
        return getOrDefault(clusterUrl, null);
    }

    public String getCaCertData() {
        return getOrDefault(clusterCACertData, null);
    }

    public Integer getClusterRequestTimeout() {
        Integer value = !isBlank(this.clusterRequestTimeout) ? Integer.valueOf(this.clusterRequestTimeout) : null;
        return getOrDefault(value, 10000);
    }

    public String getNamespace() {
        return getOrDefault(this.namespace, null);
    }

    public boolean getEnableAgentReuse() {
        return enableAgentReuse;
    }

    public void setEnableAgentReuse(boolean enableAgentReuse) {
        this.enableAgentReuse = enableAgentReuse;
    }

    private <T> T getOrDefault(T t, T defaultValue) {
        if (t instanceof String && isBlank(String.valueOf(t))) {
            return defaultValue;
        }

        if (t == null) {
            return defaultValue;
        }

        return t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginSettings)) return false;

        PluginSettings that = (PluginSettings) o;

        if (goServerUrl != null ? !goServerUrl.equals(that.goServerUrl) : that.goServerUrl != null)
            return false;
        if (autoRegisterTimeout != null ? !autoRegisterTimeout.equals(that.autoRegisterTimeout) : that.autoRegisterTimeout != null)
            return false;
        if (maxPendingPods != null ? !maxPendingPods.equals(that.maxPendingPods) : that.maxPendingPods != null)
            return false;
        if (clusterUrl != null ? !clusterUrl.equals(that.clusterUrl) : that.clusterUrl != null)
            return false;
        if (securityToken != null ? !securityToken.equals(that.securityToken) : that.securityToken != null)
            return false;
        if (clusterCACertData != null ? !clusterCACertData.equals(that.clusterCACertData) : that.clusterCACertData != null)
            return false;
        if (clusterRequestTimeout != null ? !clusterRequestTimeout.equals(that.clusterRequestTimeout) : that.clusterRequestTimeout != null)
            return false;
        return namespace != null ? namespace.equals(that.namespace) : that.namespace == null;
    }

    @Override
    public int hashCode() {
        int result = goServerUrl != null ? goServerUrl.hashCode() : 0;
        result = 31 * result + (autoRegisterTimeout != null ? autoRegisterTimeout.hashCode() : 0);
        result = 31 * result + (maxPendingPods != null ? maxPendingPods.hashCode() : 0);
        result = 31 * result + (clusterUrl != null ? clusterUrl.hashCode() : 0);
        result = 31 * result + (securityToken != null ? securityToken.hashCode() : 0);
        result = 31 * result + (clusterCACertData != null ? clusterCACertData.hashCode() : 0);
        result = 31 * result + (clusterRequestTimeout != null ? clusterRequestTimeout.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PluginSettings{" +
                "goServerUrl='" + goServerUrl + '\'' +
                ", autoRegisterTimeout=" + autoRegisterTimeout +
                ", maxPendingPods=" + maxPendingPods +
                ", clusterUrl='" + clusterUrl + '\'' +
                ", securityToken='" + securityToken + '\'' +
                ", clusterCACertData='" + clusterCACertData + '\'' +
                ", clusterRequestTimeout=" + clusterRequestTimeout +
                ", namespace='" + namespace + '\'' +
                ", autoRegisterPeriod=" + autoRegisterPeriod +
                '}';
    }
}
