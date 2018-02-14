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

import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.SPECIFIED_USING_POD_CONFIGURATION;
import static cd.go.contrib.elasticagent.utils.Util.getSimpleDateFormat;

public class KubernetesAgentInstances implements AgentInstances<KubernetesInstance> {
    private final ConcurrentHashMap<String, KubernetesInstance> instances = new ConcurrentHashMap<>();
    public Clock clock = Clock.DEFAULT;
    final Semaphore semaphore = new Semaphore(0, true);

    private KubernetesClientFactory factory;
    private KubernetesInstanceFactory kubernetesInstanceFactory;

    public KubernetesAgentInstances() {
        this(KubernetesClientFactory.instance(), new KubernetesInstanceFactory());
    }

    public KubernetesAgentInstances(KubernetesClientFactory factory) {
        this(factory, new KubernetesInstanceFactory());
    }

    public KubernetesAgentInstances(KubernetesClientFactory factory, KubernetesInstanceFactory kubernetesInstanceFactory) {
        this.factory = factory;
        this.kubernetesInstanceFactory = kubernetesInstanceFactory;
    }

    @Override
    public KubernetesInstance create(CreateAgentRequest request, PluginSettings settings, PluginRequest pluginRequest) throws Exception {
        final Integer maxAllowedContainers = settings.getMaxPendingPods();
        synchronized (instances) {
            doWithLockOnSemaphore(new SetupSemaphore(maxAllowedContainers, instances, semaphore));

            if (semaphore.tryAcquire()) {
                return createKubernetesInstance(request, settings, pluginRequest);
            } else {
                LOG.warn(String.format("The number of pending kubernetes pods is currently at the maximum permissible limit (%d). Total kubernetes pods (%d). Not creating any more containers.", maxAllowedContainers, instances.size()));
                return null;
            }
        }
    }

    private void doWithLockOnSemaphore(Runnable runnable) {
        synchronized (semaphore) {
            runnable.run();
        }
    }

    private KubernetesInstance createKubernetesInstance(CreateAgentRequest request, PluginSettings settings, PluginRequest pluginRequest) throws Exception {
        JobIdentifier jobIdentifier = request.jobIdentifier();
        if (isAgentCreatedForJob(jobIdentifier.getJobId())) {
            LOG.warn("[Create Agent Request] Request for creating an agent for Job Identifier [" + jobIdentifier + "] has already been scheduled. Skipping current request.");
            return null;
        }

        KubernetesClient client = factory.client(settings);
        KubernetesInstance instance = kubernetesInstanceFactory.create(request, settings, client, pluginRequest, isUsingPodYaml(request));
        register(instance);

        return instance;
    }

    private boolean isAgentCreatedForJob(Long jobId) {
        for (KubernetesInstance instance : instances.values()) {
            if (instance.jobId().equals(jobId)) {
                return true;
            }
        }

        return false;
    }

    private boolean isUsingPodYaml(CreateAgentRequest request) {
        return Boolean.valueOf(request.properties().get(SPECIFIED_USING_POD_CONFIGURATION.getKey()));
    }

    @Override
    public void terminate(String agentId, PluginSettings settings) throws Exception {
        KubernetesInstance instance = instances.get(agentId);
        if (instance != null) {
            KubernetesClient client = factory.client(settings);
            instance.terminate(client);
        } else {
            LOG.warn("Requested to terminate an instance that does not exist " + agentId);
        }
        instances.remove(agentId);
    }

    @Override
    public void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception {
        KubernetesAgentInstances toTerminate = unregisteredAfterTimeout(settings, agents);
        if (toTerminate.instances.isEmpty()) {
            return;
        }

        LOG.warn("Terminating instances that did not register " + toTerminate.instances.keySet());
        for (KubernetesInstance container : toTerminate.instances.values()) {
            terminate(container.name(), settings);
        }
    }

    @Override
    public Agents instancesCreatedAfterTimeout(PluginSettings settings, Agents agents) {
        ArrayList<Agent> oldAgents = new ArrayList<>();
        for (Agent agent : agents.agents()) {
            KubernetesInstance instance = instances.get(agent.elasticAgentId());
            if (instance == null) {
                continue;
            }

            if (clock.now().isAfter(instance.createdAt().plus(settings.getAutoRegisterPeriod()))) {
                oldAgents.add(agent);
            }
        }
        return new Agents(oldAgents);
    }

    @Override
    public void refreshAll(PluginRequest pluginRequest) throws Exception {
        LOG.debug("[Refresh Instances]. Syncing k8s elastic agent pod information");
        KubernetesClient client = factory.client(pluginRequest.getPluginSettings());
        PodList list = client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE).list();

        for (Pod pod : list.getItems()) {
            Map<String, String> podLabels = pod.getMetadata().getLabels();
            if (podLabels != null) {
                if (StringUtils.equals(Constants.KUBERNETES_POD_KIND_LABEL_VALUE, podLabels.get(Constants.KUBERNETES_POD_KIND_LABEL_KEY))) {
                    register(kubernetesInstanceFactory.fromKubernetesPod(pod));
                }
            }
        }
    }

    @Override
    public KubernetesInstance find(String agentId) {
        return instances.get(agentId);
    }

    private void register(KubernetesInstance instance) {
        instances.put(instance.name(), instance);
    }

    private KubernetesAgentInstances unregisteredAfterTimeout(PluginSettings settings, Agents knownAgents) throws Exception {
        Period period = settings.getAutoRegisterPeriod();
        KubernetesAgentInstances unregisteredInstances = new KubernetesAgentInstances();
        KubernetesClient client = factory.client(settings);

        for (String instanceName : instances.keySet()) {
            if (knownAgents.containsAgentWithId(instanceName)) {
                continue;
            }
            Pod pod = client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE).withName(instanceName).get();
            Date createdAt = getSimpleDateFormat().parse(pod.getMetadata().getCreationTimestamp());
            DateTime dateTimeCreated = new DateTime(createdAt);

            if (clock.now().isAfter(dateTimeCreated.plus(period))) {
                unregisteredInstances.register(kubernetesInstanceFactory.fromKubernetesPod(pod));
            }
        }
        return unregisteredInstances;
    }

    public boolean instanceExists(KubernetesInstance instance) {
        return instances.contains(instance);
    }
}
