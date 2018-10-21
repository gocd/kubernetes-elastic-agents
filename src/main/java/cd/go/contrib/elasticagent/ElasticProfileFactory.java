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
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_KUBERNETES_CLUSTER_CA_CERT;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_KUBERNETES_CLUSTER_URL;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_NAMESPACE;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.PROFILE_SECURITY_TOKEN;
import static java.text.MessageFormat.format;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.go.plugin.api.logging.Logger;

public class ElasticProfileFactory {

	public static final Logger LOG = Logger.getLoggerFor(ElasticProfileFactory.class);

	private static final ElasticProfileFactory ELASTIC_PROFILE_FACTORY = new ElasticProfileFactory();

	public static ElasticProfileFactory instance() {
		return ELASTIC_PROFILE_FACTORY;
	}

	/**
	 * Copies the PluginSettings if they are not present in ElasticProfileSettings
	 *
	 * @param elasticProfileProperties Request Properties for Elastic Profile
	 * @param PluginSettings           plugin level settings
	 * @return ElasticProfileSettings merged settings
	 */

	public synchronized ElasticProfileSettings from(Map<String, String> elasticProfileProperties,
			PluginSettings pluginSettings) {

		ElasticProfileSettings elasticProfileSettings = new ElasticProfileSettings();

		Integer autoRegisterTimeout = StringUtils
				.isBlank(elasticProfileProperties.get(PROFILE_AUTO_REGISTER_TIMEOUT.getKey()))
						? pluginSettings.getAutoRegisterTimeout()
						: Integer.valueOf(elasticProfileProperties.get(PROFILE_AUTO_REGISTER_TIMEOUT.getKey()));
		elasticProfileSettings.setAutoRegisterTimeout(autoRegisterTimeout);

		String nameSpace = StringUtils.isBlank(elasticProfileProperties.get(PROFILE_NAMESPACE.getKey()))
				? pluginSettings.getNamespace()
				: elasticProfileProperties.get(PROFILE_NAMESPACE.getKey());
		elasticProfileSettings.setNamespace(nameSpace);

		String securityToken = StringUtils.isBlank(elasticProfileProperties.get(PROFILE_SECURITY_TOKEN.getKey()))
				? pluginSettings.getSecurityToken()
				: elasticProfileProperties.get(PROFILE_SECURITY_TOKEN.getKey());
		elasticProfileSettings.setSecurityToken(securityToken);

		String clusterUrl = StringUtils.isBlank(elasticProfileProperties.get(PROFILE_KUBERNETES_CLUSTER_URL.getKey()))
				? pluginSettings.getClusterUrl()
				: elasticProfileProperties.get(PROFILE_KUBERNETES_CLUSTER_URL.getKey());
		elasticProfileSettings.setClusterUrl(clusterUrl);

		String clusterCaCert = StringUtils
				.isBlank(elasticProfileProperties.get(PROFILE_KUBERNETES_CLUSTER_CA_CERT.getKey()))
						? pluginSettings.getCaCertData()
						: elasticProfileProperties.get(PROFILE_KUBERNETES_CLUSTER_CA_CERT.getKey());
		elasticProfileSettings.setClusterCACertData(clusterCaCert);

		LOG.debug(format("[Merged Setting Namespace:{0}", nameSpace));
		LOG.debug(format("[Merged Setting ClusterUrl:{0}", clusterUrl));
		LOG.debug(format("[Merged Setting autoRegisterTimeout:{0}", autoRegisterTimeout));

		elasticProfileSettings.setGoServerUrl(pluginSettings.getGoServerUrl());

		return elasticProfileSettings;

	}
}
