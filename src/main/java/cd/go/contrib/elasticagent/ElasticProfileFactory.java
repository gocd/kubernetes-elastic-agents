/*
 * Copyright 2018 ThoughtWorks, Inc.
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

import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_AUTO_REGISTER_TIMEOUT;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_NAMESPACE;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_SECURITY_TOKEN;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class ElasticProfileFactory {

	private static final ElasticProfileFactory ELASTIC_PROFILE_FACTORY = new ElasticProfileFactory();

	public static ElasticProfileFactory instance() {
		return ELASTIC_PROFILE_FACTORY;
	}

	 /**
     * Copies the PluginSettings if they are not present in ElasticProfileSettings
     *
     * @param elasticProfileProperties Request Properties for Elastic Profile
     * @param PluginSettings plugin level settings
     * @return ElasticProfileSettings merged settings
     */
	
	public synchronized ElasticProfileSettings from(Map<String, String> elasticProfileProperties, PluginSettings pluginSettings) {

		ElasticProfileSettings elasticProfileSettings = new ElasticProfileSettings();
		
		elasticProfileSettings.setNamespace(elasticProfileProperties.get(PROFILE_NAMESPACE.getKey()));
    	elasticProfileSettings.setSecurityToken(elasticProfileProperties.get(PROFILE_SECURITY_TOKEN.getKey()));
    	final String autoRegisterTimeout = elasticProfileProperties.get(PROFILE_AUTO_REGISTER_TIMEOUT.getKey());
    	
    	if(StringUtils.isNotBlank(autoRegisterTimeout)) {
    		elasticProfileSettings.setAutoRegisterTimeout(Integer.valueOf(autoRegisterTimeout));
    	}
    	
    	if (StringUtils.isBlank(elasticProfileSettings.getNamespace())) {
    		elasticProfileSettings.setNamespace(pluginSettings.getNamespace());
    	}
    	
    	if (StringUtils.isBlank(elasticProfileSettings.getSecurityToken())) {
    		elasticProfileSettings.setSecurityToken(pluginSettings.getSecurityToken());
    	}
    	
    	if (elasticProfileSettings.getAutoRegisterTimeout()==null) {
    		elasticProfileSettings.setAutoRegisterTimeout(pluginSettings.getAutoRegisterTimeout());
    	}
    	
    	elasticProfileSettings.setClusterCACertData(pluginSettings.getCaCertData());
    	elasticProfileSettings.setClusterUrl(pluginSettings.getClusterUrl());
    	elasticProfileSettings.setGoServerUrl(pluginSettings.getGoServerUrl());

		return elasticProfileSettings;

	}
}
