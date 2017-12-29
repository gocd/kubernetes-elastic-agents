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

import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagent.utils.Size;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.*;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.POD_CONFIGURATION;
import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static cd.go.contrib.elasticagent.utils.Util.getSimpleDateFormat;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class KubernetesInstanceFactory {
    public KubernetesInstance create(CreateAgentRequest request, PluginSettings settings, KubernetesClient client, PluginRequest pluginRequest, Boolean usesPodYaml) {
        if (usesPodYaml) {
            return createUsingPodYaml(request, settings, client, pluginRequest);
        } else {
            return create(request, settings, client, pluginRequest);
        }
    }

    private KubernetesInstance create(CreateAgentRequest request, PluginSettings settings, KubernetesClient client, PluginRequest pluginRequest) {
        String containerName = Constants.KUBERNETES_POD_NAME + UUID.randomUUID().toString();

        Container container = new Container();
        container.setName(containerName);
        container.setImage(image(request.properties()));
        container.setImagePullPolicy("IfNotPresent");

        container.setResources(getPodResources(request));

        ObjectMeta podMetadata = new ObjectMeta();
        podMetadata.setName(containerName);

        PodSpec podSpec = new PodSpec();
        podSpec.setContainers(Arrays.asList(container));

        Pod elasticAgentPod = new Pod("v1", "Pod", podMetadata, podSpec, new PodStatus());

        setGoCDMetadata(request, settings, pluginRequest, elasticAgentPod);

        return createKubernetesPod(client, elasticAgentPod);
    }

    private void setGoCDMetadata(CreateAgentRequest request, PluginSettings settings, PluginRequest pluginRequest, Pod elasticAgentPod) {
        elasticAgentPod.getMetadata().setCreationTimestamp(getSimpleDateFormat().format(new Date()));

        setContainerEnvVariables(elasticAgentPod, request, settings, pluginRequest);
        setAnnotations(elasticAgentPod, request);
        setLabels(elasticAgentPod, request);
    }

    private ResourceRequirements getPodResources(CreateAgentRequest request) {
        ResourceRequirements resources = new ResourceRequirements();
        HashMap<String, Quantity> limits = new HashMap<>();

        String maxMemory = request.properties().get("MaxMemory");
        if (StringUtils.isNotBlank(maxMemory)) {
            Size mem = Size.parse(maxMemory);
            LOG.debug(String.format("[Create Agent] Setting memory resource limit on k8s pod:%s", new Quantity(String.valueOf(mem.toMegabytes()), "M")));
            limits.put("memory", new Quantity(String.valueOf(mem.toBytes())));
        }

        String maxCPU = request.properties().get("MaxCPU");
        if (StringUtils.isNotBlank(maxCPU)) {
            LOG.debug(String.format("[Create Agent] Setting cpu resource limit on k8s pod:%s", new Quantity(maxCPU)));
            limits.put("cpu", new Quantity(maxCPU));
        }

        resources.setLimits(limits);

        return resources;
    }

    private static void setLabels(Pod pod, CreateAgentRequest request) {
        Map<String, String> existingLabels = (pod.getMetadata().getLabels() != null) ? pod.getMetadata().getLabels() : new HashMap<>();
        existingLabels.putAll(labelsFrom(request));
        pod.getMetadata().setLabels(existingLabels);
    }

    private static void setAnnotations(Pod pod, CreateAgentRequest request) {
        Map<String, String> existingAnnotations = (pod.getMetadata().getAnnotations() != null) ? pod.getMetadata().getAnnotations() : new HashMap<>();
        existingAnnotations.putAll(request.properties());
        existingAnnotations.put(Constants.JOB_IDENTIFIER_LABEL_KEY, GSON.toJson(request.jobIdentifier()));
        pod.getMetadata().setAnnotations(existingAnnotations);
    }

    private KubernetesInstance createKubernetesPod(KubernetesClient client, Pod elasticAgentPod) {
        LOG.info(String.format("[Create Agent] Creating K8s pod with spec:%s", elasticAgentPod.toString()));
        Pod pod = client.pods().inNamespace(Constants.KUBERNETES_NAMESPACE_KEY).create(elasticAgentPod);
        return fromKubernetesPod(pod);
    }

    public KubernetesInstance fromKubernetesPod(Pod elasticAgentPod) {
        KubernetesInstance kubernetesInstance;
        try {
            ObjectMeta metadata = elasticAgentPod.getMetadata();
            DateTime createdAt = DateTime.now().withZone(DateTimeZone.UTC);
            if (StringUtils.isNotBlank(metadata.getCreationTimestamp())) {
                createdAt = new DateTime(getSimpleDateFormat().parse(metadata.getCreationTimestamp())).withZone(DateTimeZone.UTC);
            }
            String environment = metadata.getLabels().get(Constants.ENVIRONMENT_LABEL_KEY);
            Long jobId = Long.valueOf(metadata.getLabels().get(Constants.JOB_ID_LABEL_KEY));
            kubernetesInstance = new KubernetesInstance(createdAt, environment, metadata.getName(), metadata.getAnnotations(), jobId, PodState.fromPod(elasticAgentPod));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return kubernetesInstance;
    }

    private static List<EnvVar> environmentFrom(CreateAgentRequest request, PluginSettings settings, String podName, PluginRequest pluginRequest) {
        ArrayList<EnvVar> env = new ArrayList<>();
        String goServerUrl = StringUtils.isBlank(settings.getGoServerUrl()) ? pluginRequest.getSeverInfo().getSecureSiteUrl() : settings.getGoServerUrl();
        env.add(new EnvVar("GO_EA_SERVER_URL", goServerUrl, null));
        String environment = request.properties().get("Environment");
        if (StringUtils.isNotBlank(environment)) {
            env.addAll(parseEnvironments(environment));
        }
        env.addAll(request.autoregisterPropertiesAsEnvironmentVars(podName));

        return new ArrayList<>(env);
    }

    private static void setContainerEnvVariables(Pod pod, CreateAgentRequest request, PluginSettings settings, PluginRequest pluginRequest) {
        for (Container container : pod.getSpec().getContainers()) {
            List<EnvVar> existingEnv = (container.getEnv() != null) ? container.getEnv() : new ArrayList<>();
            existingEnv.addAll(environmentFrom(request, settings, pod.getMetadata().getName(), pluginRequest));
            container.setEnv(existingEnv);
        }
    }

    private static Collection<? extends EnvVar> parseEnvironments(String environment) {
        ArrayList<EnvVar> envVars = new ArrayList<>();
        for (String env : environment.split("\n")) {
            String[] parts = env.split("=");
            envVars.add(new EnvVar(parts[0], parts[1], null));
        }

        return envVars;
    }

    private static HashMap<String, String> labelsFrom(CreateAgentRequest request) {
        HashMap<String, String> labels = new HashMap<>();

        labels.put(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        labels.put(Constants.JOB_ID_LABEL_KEY, String.valueOf(request.jobIdentifier().getJobId()));

        if (StringUtils.isNotBlank(request.environment())) {
            labels.put(Constants.ENVIRONMENT_LABEL_KEY, request.environment());
        }

        labels.put(Constants.KUBERNETES_POD_KIND_LABEL_KEY, Constants.KUBERNETES_POD_KIND_LABEL_VALUE);

        return labels;
    }

    private static String image(Map<String, String> properties) {
        String image = properties.get("Image");

        if (isBlank(image)) {
            throw new IllegalArgumentException("Must provide `Image` attribute.");
        }

        if (!image.contains(":")) {
            return image + ":latest";
        }
        return image;
    }

    private KubernetesInstance createUsingPodYaml(CreateAgentRequest request, PluginSettings settings, KubernetesClient client, PluginRequest pluginRequest) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String podYaml = request.properties().get(POD_CONFIGURATION.getKey());
        String templatizedPodYaml = getTemplatizedPodYamlString(podYaml);

        Pod elasticAgentPod = new Pod();
        try {
            elasticAgentPod = mapper.readValue(templatizedPodYaml, Pod.class);
        } catch (IOException e) {
            //ignore error here, handle this inside validate profile!
            LOG.error(e.getMessage());
        }

        setGoCDMetadata(request, settings, pluginRequest, elasticAgentPod);
        return createKubernetesPod(client, elasticAgentPod);
    }

    public static String getTemplatizedPodYamlString(String podYaml) {
        StringWriter writer = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(podYaml), "templatePod");
        mustache.execute(writer, KubernetesInstanceFactory.getJinJavaContext());
        return writer.toString();
    }

    public static Map<String, String> getJinJavaContext() {
        HashMap<String, String> context = new HashMap<>();
        context.put(Constants.POD_POSTFIX, UUID.randomUUID().toString());
        context.put(Constants.CONTAINER_POSTFIX, UUID.randomUUID().toString());
        context.put(Constants.GOCD_AGENT_IMAGE, "gocd/gocd-agent-alpine-3.5");
        context.put(Constants.LATEST_VERSION, "v17.10.0");
        return context;
    }
}
