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

import cd.go.contrib.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagent.KubernetesInstance.AgentState;
import cd.go.contrib.elasticagent.utils.Util;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class KubernetesAgentInstances implements AgentInstances<KubernetesInstance> {
    private final ConcurrentHashMap<String, KubernetesInstance> instances;
    public Clock clock = Clock.DEFAULT;

    private KubernetesClientFactory factory;
    private KubernetesInstanceFactory kubernetesInstanceFactory;

    public KubernetesAgentInstances() {
        this(KubernetesClientFactory.instance(), new KubernetesInstanceFactory());
    }

    public KubernetesAgentInstances(KubernetesClientFactory factory) {
        this(factory, new KubernetesInstanceFactory());
    }

    public KubernetesAgentInstances(KubernetesClientFactory factory, KubernetesInstanceFactory kubernetesInstanceFactory) {
        this(factory, kubernetesInstanceFactory, Collections.emptyMap());
    }

    public KubernetesAgentInstances(KubernetesClientFactory factory, KubernetesInstanceFactory kubernetesInstanceFactory, Map<String, KubernetesInstance> initialInstances) {
        this.factory = factory;
        this.kubernetesInstanceFactory = kubernetesInstanceFactory;
        this.instances = new ConcurrentHashMap<>(initialInstances);
    }

    @Override
    public Optional<KubernetesInstance> requestCreateAgent(CreateAgentRequest request, PluginSettings settings, PluginRequest pluginRequest, ConsoleLogAppender consoleLogAppender) {
        final Integer maxAllowedPods = settings.getMaxPendingPods();
        synchronized (instances) {
            if (instances.size() < maxAllowedPods) {
                return requestCreateAgentHelper(request, settings, pluginRequest, consoleLogAppender);
            } else {
                String message = String.format("[Create Agent Request] The number of pending kubernetes pods is currently at the maximum permissible limit (%s). Total kubernetes pods (%s). Not creating any more pods.",
                        maxAllowedPods,
                        instances.size());
                LOG.warn(message);
                consoleLogAppender.accept(message);
                return Optional.empty();
            }
        }
    }

    private List<KubernetesInstance> findPodsEligibleForReuse(CreateAgentRequest request) {
        Long jobId = request.jobIdentifier().getJobId();
        String jobElasticConfigHash = KubernetesInstanceFactory.agentConfigHash(
                request.clusterProfileProperties(), request.elasticProfileProperties());

        List<KubernetesInstance> eligiblePods = new ArrayList<>();

        for (KubernetesInstance instance : instances.values()) {
            if (instance.getJobId().equals(jobId)) {
                eligiblePods.add(instance);
                continue;
            }

            String podElasticConfigHash = instance.getPodAnnotations().get(KubernetesInstance.ELASTIC_CONFIG_HASH);
            boolean sameElasticConfig = Objects.equals(podElasticConfigHash, jobElasticConfigHash);
            boolean instanceIsIdle = instance.getAgentState().equals(KubernetesInstance.AgentState.Idle);
            boolean podIsRunning = instance.getPodState().equals(PodState.Running);
            boolean isReusable = sameElasticConfig && instanceIsIdle && podIsRunning;

            LOG.info(
                    "[reuse] Is pod {} reusable for job {}? {}. Job has {}={}; pod has {}={}, agentState={}, podState={}",
                    instance.getPodName(),
                    jobId,
                    isReusable,
                    KubernetesInstance.ELASTIC_CONFIG_HASH,
                    jobElasticConfigHash,
                    KubernetesInstance.ELASTIC_CONFIG_HASH,
                    podElasticConfigHash,
                    instance.getAgentState(),
                    instance.getPodState()
            );

            if (isReusable) {
                eligiblePods.add(instance);
            }
        }

        return eligiblePods;
    }


    private Optional<KubernetesInstance> requestCreateAgentHelper(
            CreateAgentRequest request,
            PluginSettings settings,
            PluginRequest pluginRequest,
            ConsoleLogAppender consoleLogAppender) {
        JobIdentifier jobIdentifier = request.jobIdentifier();
        Long jobId = jobIdentifier.getJobId();

        // Agent reuse disabled - create a new pod only if one hasn't already been created for this job ID.
        if (!settings.getEnableAgentReuse()) {
            // Already created a pod for this job ID.
            if (isAgentCreatedForJob(jobId)) {
                String message = format("[Create Agent Request] Request for creating an agent for Job Identifier [{0}] has already been scheduled. Skipping current request.", jobIdentifier);
                LOG.warn(message);
                consoleLogAppender.accept(message);
                return Optional.empty();
            }
            // No pod created yet for this job ID. Create one.
            KubernetesClient client = factory.client(settings);
            KubernetesInstance instance = kubernetesInstanceFactory.create(request, settings, client, pluginRequest);
            consoleLogAppender.accept(String.format("Created pod: %s", instance.getPodName()));
            instance = instance.toBuilder().agentState(AgentState.Building).build();
            register(instance);
            consoleLogAppender.accept(String.format("Agent pod %s created. Waiting for it to register to the GoCD server.", instance.getPodName()));
            return Optional.of(instance);
        }

        // Agent reuse enabled - look for any extant pods that match this job,
        // and create a new one only if there are none.
        List<KubernetesInstance> reusablePods = findPodsEligibleForReuse(request);
        LOG.info("[reuse] Found {} pods eligible for reuse for CreateAgentRequest for job {}: {}",
              reusablePods.size(),
              jobId,
              reusablePods.stream().map(pod -> pod.getPodName()).collect(Collectors.toList()));

        if (reusablePods.isEmpty()) {
            KubernetesClient client = factory.client(settings);
            KubernetesInstance instance = kubernetesInstanceFactory.create(request, settings, client, pluginRequest);
            consoleLogAppender.accept(String.format("Created pod: %s", instance.getPodName()));
            instance = instance.toBuilder().agentState(AgentState.Building).build();
            register(instance);
            consoleLogAppender.accept(String.format("Agent pod %s created. Waiting for it to register to the GoCD server.", instance.getPodName()));
            return Optional.of(instance);
        } else {
            String message = String.format("[reuse] Not creating a new pod - found %s eligible for reuse.", reusablePods.size());
            consoleLogAppender.accept(message);
            LOG.info(message);
            return Optional.empty();
        }
    }

    private boolean isAgentCreatedForJob(Long jobId) {
        for (KubernetesInstance instance : instances.values()) {
            if (instance.getJobId().equals(jobId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void terminate(String agentId, PluginSettings settings) {
        KubernetesInstance instance = instances.get(agentId);
        if (instance != null) {
            KubernetesClient client = factory.client(settings);
            client.pods().withName(instance.getPodName()).delete();
        } else {
            LOG.warn(format("Requested to terminate an instance that does not exist {0}.", agentId));
        }
        instances.remove(agentId);
    }

    @Override
    public void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception {
        KubernetesAgentInstances toTerminate = unregisteredAfterTimeout(settings, agents);
        if (toTerminate.instances.isEmpty()) {
            return;
        }

        LOG.warn(format("Terminating instances that did not register {0}.", toTerminate.instances.keySet()));
        for (String podName : toTerminate.instances.keySet()) {
            terminate(podName, settings);
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

            if (clock.now().isAfter(instance.getCreatedAt().plus(settings.getAutoRegisterPeriod()))) {
                oldAgents.add(agent);
            }
        }
        return new Agents(oldAgents);
    }

    public List<Pod> listAgentPods(KubernetesClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client is null");
        }
        return client.pods()
                .withLabel(Constants.KUBERNETES_POD_KIND_LABEL_KEY, Constants.KUBERNETES_POD_KIND_LABEL_VALUE)
                .list()
                .getItems();
    }

    @Override
    public void refreshAll(PluginSettings properties) {
        LOG.debug("[Refresh Instances] Syncing k8s elastic agent pod information for cluster {}.", properties);
        List<Pod> pods = null;
        try {
            KubernetesClient client = factory.client(properties);
            pods = listAgentPods(client);
        } catch (Exception e) {
            LOG.error("Error occurred while trying to list kubernetes pods:", e);

            if (e.getCause() instanceof SocketTimeoutException) {
                LOG.error("Error caused due to SocketTimeoutException. This generally happens due to stale kubernetes client. Clearing out existing kubernetes client and creating a new one!");
                factory.clearOutExistingClient();
                KubernetesClient client = factory.client(properties);
                pods = listAgentPods(client);
            }
        }

        if (pods == null) {
            LOG.info("Did not find any running kubernetes pods.");
            return;
        }

        Map<String, KubernetesInstance> oldInstances = Map.copyOf(instances);
        instances.clear();

        for (Pod pod : pods) {
            String podName = pod.getMetadata().getName();
            // preserve pod's agent state
            KubernetesInstance newInstance = kubernetesInstanceFactory.fromKubernetesPod(pod);
            KubernetesInstance oldInstance = oldInstances.get(podName);
            if (oldInstance != null) {
                AgentState oldAgentState = oldInstances.get(podName).getAgentState();
                newInstance = newInstance.toBuilder().agentState(oldAgentState).build();
                LOG.debug("[reuse] Preserved AgentState {} upon refresh of pod {}", oldAgentState, podName);
            }
            register(newInstance);
        }

        LOG.info(String.format("[refresh-pod-state] Pod information successfully synced. All(Running/Pending) pod count is %d.", instances.size()));
    }

    @Override
    public KubernetesInstance updateAgent(String agentId, Function<KubernetesInstance, KubernetesInstance> updateFn) {
        return instances.compute(agentId, (_agentId, instance) -> updateFn.apply(instance));
    }

    @Override
    public KubernetesInstance find(String agentId) {
        return instances.get(agentId);
    }

    public void register(KubernetesInstance instance) {
        instances.put(instance.getPodName(), instance);
    }

    private KubernetesAgentInstances unregisteredAfterTimeout(PluginSettings settings, Agents knownAgents) throws Exception {
        Duration period = settings.getAutoRegisterPeriod();
        KubernetesAgentInstances unregisteredInstances = new KubernetesAgentInstances();
        KubernetesClient client = factory.client(settings);

        for (String instanceName : instances.keySet()) {
            if (knownAgents.containsAgentWithId(instanceName)) {
                continue;
            }

            Pod pod = getPod(client, instanceName);
            if (pod == null) {
                LOG.debug(String.format("[server-ping] Pod with name %s is already deleted.", instanceName));
                continue;
            }

            Instant createdAt = Constants.KUBERNETES_POD_CREATION_TIME_FORMAT.parse(pod.getMetadata().getCreationTimestamp(), Instant::from);

            if (clock.now().isAfter(createdAt.plus(period))) {
                unregisteredInstances.register(kubernetesInstanceFactory.fromKubernetesPod(pod));
            }
        }

        return unregisteredInstances;
    }

    private Pod getPod(KubernetesClient client, String instanceName) {
        try {
            return client.pods().withName(instanceName).get();
        } catch (Exception e) {
            LOG.warn(String.format("[server-ping] Failed to fetch pod[%s] information:", instanceName), e);
            return null;
        }
    }

    public boolean instanceExists(KubernetesInstance instance) {
        return instances.contains(instance);
    }

    public boolean hasInstance(String elasticAgentId) {
        return find(elasticAgentId) != null;
    }
}
