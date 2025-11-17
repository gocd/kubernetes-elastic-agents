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

import cd.go.contrib.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagent.utils.Size;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

import static cd.go.contrib.elasticagent.Constants.*;
import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.*;
import static cd.go.contrib.elasticagent.utils.Util.isBlank;
import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.text.MessageFormat.format;

public class KubernetesInstanceFactory {
    public KubernetesInstance create(CreateAgentRequest request, PluginSettings settings, KubernetesClient client, PluginRequest pluginRequest) {
        String podSpecType = request.properties().get(POD_SPEC_TYPE.getKey());
        if (podSpecType != null) {
            return switch (podSpecType) {
                case "properties" -> createUsingProperties(request, settings, client, pluginRequest);
                case "remote" -> createUsingRemoteFile(request, settings, client, pluginRequest);
                case "yaml" -> createUsingPodYaml(request, settings, client, pluginRequest);
                default -> throw new IllegalArgumentException(String.format("Unsupported value for `PodSpecType`: %s", podSpecType));
            };
        }
        else {
            if (Boolean.parseBoolean(request.properties().get(SPECIFIED_USING_POD_CONFIGURATION.getKey()))) {
                return createUsingPodYaml(request, settings, client, pluginRequest);
            } else {
                return createUsingProperties(request, settings, client, pluginRequest);
            }
        }
    }

    private KubernetesInstance createUsingProperties(CreateAgentRequest request, PluginSettings settings, KubernetesClient client, PluginRequest pluginRequest) {
        String containerName = format("{0}-{1}", KUBERNETES_POD_NAME_PREFIX, UUID.randomUUID().toString());

        Container container = new Container();
        container.setName(containerName);
        container.setImage(image(request.properties()));
        container.setImagePullPolicy("IfNotPresent");
        container.setSecurityContext(new SecurityContextBuilder().withPrivileged(privileged(request)).build());

        container.setResources(getPodResources(request));

        ObjectMeta podMetadata = new ObjectMeta();
        podMetadata.setName(containerName);

        PodSpec podSpec = new PodSpec();
        podSpec.setContainers(List.of(container));

        Pod elasticAgentPod = new Pod("v1", "Pod", podMetadata, podSpec, new PodStatus());

        setGoCDMetadata(request, settings, pluginRequest, elasticAgentPod);

        return createKubernetesPod(client, elasticAgentPod);
    }

    private Boolean privileged(CreateAgentRequest request) {
        final String privilegedMode = request.properties().get(PRIVILEGED.getKey());
        if (isBlank(privilegedMode)) {
            return false;
        }
        return Boolean.valueOf(privilegedMode);
    }

    private void setGoCDMetadata(CreateAgentRequest request, PluginSettings settings, PluginRequest pluginRequest, Pod elasticAgentPod) {
        elasticAgentPod.getMetadata().setCreationTimestamp(KUBERNETES_POD_CREATION_TIME_FORMAT.format(Instant.now()));

        setContainerEnvVariables(elasticAgentPod, request, settings, pluginRequest);
        setAnnotations(elasticAgentPod, request);
        setLabels(elasticAgentPod, request);
    }

    private ResourceRequirements getPodResources(CreateAgentRequest request) {
        ResourceRequirements resources = new ResourceRequirements();
        HashMap<String, Quantity> limits = new HashMap<>();

        String maxMemory = request.properties().get("MaxMemory");
        if (!isBlank(maxMemory)) {
            Size mem = Size.parse(maxMemory);
            LOG.debug(format("[Create Agent] Setting memory resource limit on k8s pod: {0}.", new Quantity(valueOf((long) mem.toMegabytes()), "M")));
            long memory = (long) mem.toBytes();
            limits.put("memory", new Quantity(valueOf(memory)));
        }

        String maxCPU = request.properties().get("MaxCPU");
        if (!isBlank(maxCPU)) {
            LOG.debug(format("[Create Agent] Setting cpu resource limit on k8s pod: {0}.", new Quantity(maxCPU)));
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
        existingAnnotations.put(JOB_IDENTIFIER_LABEL_KEY, request.jobIdentifier().toJson());
        pod.getMetadata().setAnnotations(existingAnnotations);
    }

    private KubernetesInstance createKubernetesPod(KubernetesClient client, Pod elasticAgentPod) {
        LOG.info(format("[Create Agent] Creating K8s pod with spec: {0}.", elasticAgentPod.toString()));
        Pod pod = client.pods().resource(elasticAgentPod).create();
        return fromKubernetesPod(pod);
    }

    KubernetesInstance fromKubernetesPod(Pod elasticAgentPod) {
        KubernetesInstance kubernetesInstance;
        try {
            ObjectMeta metadata = elasticAgentPod.getMetadata();
            Instant createdAt = Instant.now();
            if (!isBlank(metadata.getCreationTimestamp())) {
                createdAt = Constants.KUBERNETES_POD_CREATION_TIME_FORMAT.parse(metadata.getCreationTimestamp(), Instant::from);
            }
            String environment = metadata.getLabels().get(ENVIRONMENT_LABEL_KEY);
            Long jobId = Long.valueOf(metadata.getLabels().get(JOB_ID_LABEL_KEY));
            kubernetesInstance = new KubernetesInstance(createdAt, environment, metadata.getName(), metadata.getAnnotations(), jobId, PodState.fromPod(elasticAgentPod));
        } catch (DateTimeParseException e) {
            throw new RuntimeException(e);
        }
        return kubernetesInstance;
    }

    private static List<EnvVar> environmentFrom(CreateAgentRequest request, PluginSettings settings, String podName, PluginRequest pluginRequest) {
        ArrayList<EnvVar> env = new ArrayList<>();
        String goServerUrl = isBlank(settings.getGoServerUrl()) ? pluginRequest.getSeverInfo().getSecureSiteUrl() : settings.getGoServerUrl();
        env.add(new EnvVar("GO_EA_SERVER_URL", goServerUrl, null));
        String environment = request.properties().get("Environment");
        if (!isBlank(environment)) {
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

        labels.put(CREATED_BY_LABEL_KEY, PLUGIN_ID);
        labels.put(JOB_ID_LABEL_KEY, valueOf(request.jobIdentifier().getJobId()));

        if (!isBlank(request.environment())) {
            labels.put(ENVIRONMENT_LABEL_KEY, request.environment());
        }

        labels.put(KUBERNETES_POD_KIND_LABEL_KEY, KUBERNETES_POD_KIND_LABEL_VALUE);

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
        String templatizedPodYaml = getTemplatedPodSpec(podYaml);

        Pod elasticAgentPod = new Pod();
        try {
            elasticAgentPod = mapper.readValue(templatizedPodYaml, Pod.class);
            setPodNameIfNecessary(elasticAgentPod, podYaml);
        } catch (IOException e) {
            //ignore error here, handle this inside validate profile!
            LOG.error(e.getMessage());
        }

        setGoCDMetadata(request, settings, pluginRequest, elasticAgentPod);
        return createKubernetesPod(client, elasticAgentPod);
    }

    private KubernetesInstance createUsingRemoteFile(CreateAgentRequest request, PluginSettings settings, KubernetesClient client, PluginRequest pluginRequest) {
        String fileToDownload = request.properties().get(REMOTE_FILE.getKey());
        String fileType = request.properties().get(REMOTE_FILE_TYPE.getKey());

        Pod elasticAgentPod = new Pod();
        ObjectMapper mapper;
        if ("json".equalsIgnoreCase(fileType)) {
            mapper = new ObjectMapper(new JsonFactory());
        }
        else if ("yaml".equalsIgnoreCase(fileType)) {
            mapper = new ObjectMapper(new YAMLFactory());
        }
        else {
            throw new IllegalArgumentException("RemoteFileType value should be one of `json` or `yaml`.");
        }

        Path podSpecFile = Path.of(String.format("pod_spec_%s", UUID.randomUUID()));
        try (InputStream downloadStream = new URL(fileToDownload).openStream()){
            Files.copy(downloadStream, podSpecFile);
            LOG.debug(format("Finished downloading {0} to {1}", fileToDownload, podSpecFile));
            String spec = Files.readString(podSpecFile, UTF_8);
            String templatedPodSpec = getTemplatedPodSpec(spec);
            elasticAgentPod = mapper.readValue(templatedPodSpec, Pod.class);
            setPodNameIfNecessary(elasticAgentPod, spec);
            Files.deleteIfExists(podSpecFile);
            LOG.debug(format("Deleted {0}", podSpecFile));
        } catch (IOException e) {
            //ignore error here, handle this inside validate profile!
            LOG.error(e.getMessage());
        }
        setGoCDMetadata(request, settings, pluginRequest, elasticAgentPod);
        return createKubernetesPod(client, elasticAgentPod);
    }

    private void setPodNameIfNecessary(Pod elasticAgentPod, String spec) {
        if (!spec.contains(POD_POSTFIX)) {
            String newPodName = elasticAgentPod.getMetadata().getName().concat(String.format("-%s", UUID.randomUUID()));
            elasticAgentPod.getMetadata().setName(newPodName);
        }
    }


    public static String getTemplatedPodSpec(String podSpec) {
        StringWriter writer = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(podSpec), "templatePod");
        mustache.execute(writer, KubernetesInstanceFactory.getJinJavaContext());
        return writer.toString();
    }

    private static Map<String, String> getJinJavaContext() {
        Map<String, String> context = new HashMap<>();
        context.put(POD_POSTFIX, UUID.randomUUID().toString());
        context.put(CONTAINER_POSTFIX, UUID.randomUUID().toString());
        context.put(GOCD_AGENT_IMAGE, "gocd/gocd-agent-wolfi");
        context.put(LATEST_VERSION, "v24.3.0");
        return context;
    }
}
