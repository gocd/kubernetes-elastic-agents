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
    private String autoRegisterTimeout;

    @Expose
    @SerializedName("kubernetes_cluster_url")
    private String kubernetesClusterUrl;

    @Expose
    @SerializedName("kubernetes_cluster_username")
    private String kubernetesClusterUsername;

    @Expose
    @SerializedName("kubernetes_cluster_password")
    private String kubernetesClusterPassword;

    @Expose
    @SerializedName("kubernetes_cluster_ca_cert")
    private String kubernetesClusterCACert;

    private Period autoRegisterPeriod;


    public static PluginSettings fromJSON(String json) {
        return GSON.fromJson(json, PluginSettings.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginSettings)) return false;

        PluginSettings that = (PluginSettings) o;

        if (goServerUrl != null ? !goServerUrl.equals(that.goServerUrl) : that.goServerUrl != null) return false;
        if (autoRegisterTimeout != null ? !autoRegisterTimeout.equals(that.autoRegisterTimeout) : that.autoRegisterTimeout != null)
            return false;
        if (kubernetesClusterUrl != null ? !kubernetesClusterUrl.equals(that.kubernetesClusterUrl) : that.kubernetesClusterUrl != null)
            return false;
        return autoRegisterPeriod != null ? autoRegisterPeriod.equals(that.autoRegisterPeriod) : that.autoRegisterPeriod == null;
    }

    @Override
    public int hashCode() {
        int result = goServerUrl != null ? goServerUrl.hashCode() : 0;
        result = 31 * result + (autoRegisterTimeout != null ? autoRegisterTimeout.hashCode() : 0);
        result = 31 * result + (kubernetesClusterUrl != null ? kubernetesClusterUrl.hashCode() : 0);
        result = 31 * result + (autoRegisterPeriod != null ? autoRegisterPeriod.hashCode() : 0);
        return result;
    }

    public Period getAutoRegisterPeriod() {
        if (this.autoRegisterPeriod == null) {
            this.autoRegisterPeriod = new Period().withMinutes(Integer.parseInt(getAutoRegisterTimeout()));
        }
        return this.autoRegisterPeriod;
    }

    String getAutoRegisterTimeout() {
        if (autoRegisterTimeout == null) {
            autoRegisterTimeout = "10";
        }
        return autoRegisterTimeout;
    }

    public String getGoServerUrl() {
        return goServerUrl;
    }

    public String getKubernetesClusterUrl() {
        return kubernetesClusterUrl;
    }

    public String getKubernetesClusterUsername() {
        return kubernetesClusterUsername;
    }

    public String getKubernetesClusterPassword() {
        return kubernetesClusterPassword;
    }

    public String getKubernetesClusterCACert() {
        return kubernetesClusterCACert;
    }

    public void setGoServerUrl(String goServerUrl) {
        this.goServerUrl = goServerUrl;
    }
}
