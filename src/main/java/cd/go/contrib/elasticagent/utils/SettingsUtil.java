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

package cd.go.contrib.elasticagent.utils;

import org.apache.commons.lang3.StringUtils;

import cd.go.contrib.elasticagent.KubernetesSettings;
import cd.go.contrib.elasticagent.PluginSettings;

public class SettingsUtil {

	 /**
     * Copies the PluginSettings if they are not present in KubernetesSettings
     *
     * @param KubernetesSettings the elastic profile level settings
     * @param PluginSettings plugin level settings
     * @return KubernetesSettings merged settings
     */
	
    public static KubernetesSettings mergeSettings(KubernetesSettings kubernetesSettings,PluginSettings pluginSettings) {
    	
    	if (StringUtils.isBlank(kubernetesSettings.getNamespace())) {
    		kubernetesSettings.setNamespace(pluginSettings.getNamespace());
    	}
    	
    	if (StringUtils.isBlank(kubernetesSettings.getSecurityToken())) {
    		kubernetesSettings.setSecurityToken(pluginSettings.getSecurityToken());
    	}
    	
    	if (kubernetesSettings.getAutoRegisterTimeout()==null) {
    		kubernetesSettings.setAutoRegisterTimeout(pluginSettings.getAutoRegisterTimeout());
    	}
    	
    	kubernetesSettings.setClusterCACertData(pluginSettings.getCaCertData());
    	kubernetesSettings.setClusterUrl(pluginSettings.getClusterUrl());
    	kubernetesSettings.setGoServerUrl(pluginSettings.getGoServerUrl());
    	
       return kubernetesSettings;
    }
  
}
