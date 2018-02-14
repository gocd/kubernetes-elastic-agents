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

import cd.go.contrib.elasticagent.model.AuthenticationStrategy;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.joda.time.Period;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class PluginSettings {
    @Expose
    @SerializedName("go_server_url")
    private String goServerUrl;

    @Expose
    @SerializedName("auto_register_timeout")
    private Integer autoRegisterTimeout = 10;

    @Expose
    @SerializedName("pending_pods_count")
    private Integer maxPendingPods = 10;

    @Expose
    @SerializedName("authentication_strategy")
    private String authenticationStrategy = AuthenticationStrategy.OAUTH_TOKEN.name();

    @Expose
    @SerializedName("oauth_token")
    private String oauthToken;

    @Expose
    @SerializedName("kubernetes_cluster_url")
    private String clusterUrl;

    @Expose
    @SerializedName("kubernetes_cluster_ca_cert")
    private String clusterCACertData;

    @Expose
    @SerializedName("client_key_data")
    private String clientKeyData;

    @Expose
    @SerializedName("client_cert_data")
    private String clientCertData;

    private Period autoRegisterPeriod;

    public PluginSettings() {
    }

    public PluginSettings(String goServerUrl, String clusterUrl, String clusterCACertData) {
        this.goServerUrl = goServerUrl;
        this.clusterUrl = clusterUrl;
        this.clusterCACertData = clusterCACertData;
    }

    public static PluginSettings fromJSON(String json) {
        return GSON.fromJson(json, PluginSettings.class);
    }

    public Period getAutoRegisterPeriod() {
        if (this.autoRegisterPeriod == null) {
            this.autoRegisterPeriod = new Period().withMinutes(getAutoRegisterTimeout());
        }
        return this.autoRegisterPeriod;
    }

    Integer getAutoRegisterTimeout() {
        return autoRegisterTimeout;
    }

    public Integer getMaxPendingPods() {
        return Integer.valueOf(maxPendingPods);
    }

    public String getGoServerUrl() {
        return goServerUrl;
    }

    public AuthenticationStrategy getAuthenticationStrategy() {
        return AuthenticationStrategy.from(authenticationStrategy);
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public String getClusterUrl() {
        return clusterUrl;
    }

    public String getCaCertData() {
        return clusterCACertData;
    }

    public String getClientKeyData() {
        return clientKeyData;
    }

    public String getClientCertData() {
        return clientCertData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginSettings)) return false;

        PluginSettings that = (PluginSettings) o;

        if (goServerUrl != null ? !goServerUrl.equals(that.goServerUrl) : that.goServerUrl != null) return false;
        if (autoRegisterTimeout != null ? !autoRegisterTimeout.equals(that.autoRegisterTimeout) : that.autoRegisterTimeout != null)
            return false;
        if (maxPendingPods != null ? !maxPendingPods.equals(that.maxPendingPods) : that.maxPendingPods != null)
            return false;
        if (authenticationStrategy != null ? !authenticationStrategy.equals(that.authenticationStrategy) : that.authenticationStrategy != null)
            return false;
        if (clusterUrl != null ? !clusterUrl.equals(that.clusterUrl) : that.clusterUrl != null) return false;
        if (clusterCACertData != null ? !clusterCACertData.equals(that.clusterCACertData) : that.clusterCACertData != null)
            return false;
        if (oauthToken != null ? !oauthToken.equals(that.oauthToken) : that.oauthToken != null) return false;
        if (clientKeyData != null ? !clientKeyData.equals(that.clientKeyData) : that.clientKeyData != null)
            return false;
        return clientCertData != null ? clientCertData.equals(that.clientCertData) : that.clientCertData == null;
    }

    @Override
    public int hashCode() {
        int result = goServerUrl != null ? goServerUrl.hashCode() : 0;
        result = 31 * result + (autoRegisterTimeout != null ? autoRegisterTimeout.hashCode() : 0);
        result = 31 * result + (maxPendingPods != null ? maxPendingPods.hashCode() : 0);
        result = 31 * result + (authenticationStrategy != null ? authenticationStrategy.hashCode() : 0);
        result = 31 * result + (clusterUrl != null ? clusterUrl.hashCode() : 0);
        result = 31 * result + (clusterCACertData != null ? clusterCACertData.hashCode() : 0);
        result = 31 * result + (oauthToken != null ? oauthToken.hashCode() : 0);
        result = 31 * result + (clientKeyData != null ? clientKeyData.hashCode() : 0);
        result = 31 * result + (clientCertData != null ? clientCertData.hashCode() : 0);
        return result;
    }
}
