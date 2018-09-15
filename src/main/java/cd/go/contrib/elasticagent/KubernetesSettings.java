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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;

public class KubernetesSettings {
    private String goServerUrl;

    private Integer autoRegisterTimeout;

    private Integer maxPendingPods;

    private String clusterUrl;

    private String securityToken;

    private String clusterCACertData;

    private String namespace;

    private Period autoRegisterPeriod;

    public KubernetesSettings() {
    }

    public KubernetesSettings(String goServerUrl, String clusterUrl, String clusterCACertData) {
        this.goServerUrl = goServerUrl;
        this.clusterUrl = clusterUrl;
        this.clusterCACertData = clusterCACertData;
    }

    public String getGoServerUrl() {
		return goServerUrl;
	}

	public void setGoServerUrl(String goServerUrl) {
		this.goServerUrl = goServerUrl;
	}

	public Integer getAutoRegisterTimeout() {
		return autoRegisterTimeout;
	}

	public void setAutoRegisterTimeout(Integer autoRegisterTimeout) {
		this.autoRegisterTimeout = autoRegisterTimeout;
	}

	public Integer getMaxPendingPods() {
		return maxPendingPods;
	}

	public void setMaxPendingPods(Integer maxPendingPods) {
		this.maxPendingPods = maxPendingPods;
	}

	public String getClusterUrl() {
		return clusterUrl;
	}

	public void setClusterUrl(String clusterUrl) {
		this.clusterUrl = clusterUrl;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public String getClusterCACertData() {
		return clusterCACertData;
	}

	public void setClusterCACertData(String clusterCACertData) {
		this.clusterCACertData = clusterCACertData;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

    public Period getAutoRegisterPeriod() {
        if (this.autoRegisterPeriod == null) {
            this.autoRegisterPeriod = new Period().withMinutes(getAutoRegisterTimeout());
        }
        return this.autoRegisterPeriod;
    }

	public void setAutoRegisterPeriod(Period autoRegisterPeriod) {
		this.autoRegisterPeriod = autoRegisterPeriod;
	}

	private <T> T getOrDefault(T t, T defaultValue) {
        if (t instanceof String && StringUtils.isBlank(String.valueOf(t))) {
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
        if (!(o instanceof KubernetesSettings)) return false;

        KubernetesSettings that = (KubernetesSettings) o;

        if (goServerUrl != null ? !goServerUrl.equals(that.goServerUrl) : that.goServerUrl != null) return false;
        if (autoRegisterTimeout != null ? !autoRegisterTimeout.equals(that.autoRegisterTimeout) : that.autoRegisterTimeout != null)
            return false;
        if (maxPendingPods != null ? !maxPendingPods.equals(that.maxPendingPods) : that.maxPendingPods != null)
            return false;
        if (clusterUrl != null ? !clusterUrl.equals(that.clusterUrl) : that.clusterUrl != null) return false;
        if (securityToken != null ? !securityToken.equals(that.securityToken) : that.securityToken != null) return false;
        if (clusterCACertData != null ? !clusterCACertData.equals(that.clusterCACertData) : that.clusterCACertData != null)
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
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }
}
