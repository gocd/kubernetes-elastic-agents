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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

import static cd.go.contrib.elasticagent.Constants.*;
import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static cd.go.contrib.elasticagent.executors.GetProfileMetadataExecutor.*;
import static cd.go.contrib.elasticagent.utils.Util.getSimpleDateFormat;
import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class KubernetesInstanceFactory {
    public KubernetesInstance create(CreateAgentRequest request, PluginSettings settings, KubernetesClient client, PluginRequest pluginRequest) {
        String podSpecType = request.properties().get(POD_SPEC_TYPE.getKey());
        if (podSpecType != null) {
            switch (podSpecType) {
                case "properties":
                    return createUsingProperties(request, settings, client, pluginRequest);
                case "remote":
                    return createUsingRemoteFile(request, settings, client, pluginRequest);
                case "yaml":
                    return createUsingPodYaml(request, settings, client, pluginRequest);
                default:
                    throw new IllegalArgumentException(String.format("Unsupported value for `PodSpecType`: %s", podSpecType));
            }
        }
        else {
            if (Boolean.valueOf(request.properties().get(SPECIFIED_USING_POD_CONFIGURATION.getKey()))) {
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
        podSpec.setContainers(Arrays.asList(container));

        Pod elasticAgentPod = new Pod("v1", "Pod", podMetadata, podSpec, new PodStatus());

        setGoCDMetadata(request, settings, pluginRequest, elasticAgentPod);

        return createKubernetesPod(client, elasticAgentPod);
    }

    private Boolean privileged(CreateAgentRequest request) {
        final String privilegedMode = request.properties().get(PRIVILEGED.getKey());
        if (StringUtils.isBlank(privilegedMode)) {
            return false;
        }
        return Boolean.valueOf(privilegedMode);
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
            LOG.debug(format("[Create Agent] Setting memory resource limit on k8s pod: {0}.", new Quantity(valueOf((long) mem.toMegabytes()), "M")));
            long memory = (long) mem.toBytes();
            limits.put("memory", new Quantity(valueOf(memory)));
        }

        String maxCPU = request.properties().get("MaxCPU");
        if (StringUtils.isNotBlank(maxCPU)) {
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
        Pod pod = client.pods().create(elasticAgentPod);
        return fromKubernetesPod(pod);
    }

    KubernetesInstance fromKubernetesPod(Pod elasticAgentPod) {
        KubernetesInstance kubernetesInstance;
        try {
            ObjectMeta metadata = elasticAgentPod.getMetadata();
            DateTime createdAt = DateTime.now().withZone(DateTimeZone.UTC);
            if (StringUtils.isNotBlank(metadata.getCreationTimestamp())) {
                createdAt = new DateTime(getSimpleDateFormat().parse(metadata.getCreationTimestamp())).withZone(DateTimeZone.UTC);
            }
            String environment = metadata.getLabels().get(ENVIRONMENT_LABEL_KEY);
            Long jobId = Long.valueOf(metadata.getLabels().get(JOB_ID_LABEL_KEY));
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

        labels.put(CREATED_BY_LABEL_KEY, PLUGIN_ID);
        labels.put(JOB_ID_LABEL_KEY, valueOf(request.jobIdentifier().getJobId()));

        if (StringUtils.isNotBlank(request.environment())) {
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
        String templatizedPodYaml = getTemplatizedPodSpec(podYaml);

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

        File podSpecFile = new File(String.format("pod_spec_%s", UUID.randomUUID().toString()));
        try {
            FileUtils.copyURLToFile(new URL(fileToDownload), podSpecFile);
            LOG.debug(format("Finished downloading %s to %s", fileToDownload, podSpecFile));
            String spec = FileUtils.readFileToString(podSpecFile, UTF_8);
            String templatizedPodSpec = getTemplatizedPodSpec(spec);
            elasticAgentPod = mapper.readValue(templatizedPodSpec, Pod.class);
            setPodNameIfNecessary(elasticAgentPod, spec);
            FileUtils.deleteQuietly(podSpecFile);
            LOG.debug(format("Deleted %s", podSpecFile));

        } catch (IOException e) {
            //ignore error here, handle this inside validate profile!
            LOG.error(e.getMessage());
        }
        setGoCDMetadata(request, settings, pluginRequest, elasticAgentPod);
        return createKubernetesPod(client, elasticAgentPod);
    }

    private void setPodNameIfNecessary(Pod elasticAgentPod, String spec) {
        if (!spec.contains(POD_POSTFIX)) {
            String newPodName = elasticAgentPod.getMetadata().getName().concat(String.format("-%s", UUID.randomUUID().toString()));
            elasticAgentPod.getMetadata().setName(newPodName);
        }
    }


    public static String getTemplatizedPodSpec(String podSpec) {
        StringWriter writer = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(podSpec), "templatePod");
        mustache.execute(writer, KubernetesInstanceFactory.getJinJavaContext());
        return writer.toString();
    }

    private static Map<String, String> getJinJavaContext() {
        HashMap<String, String> context = new HashMap<>();
        context.put(POD_POSTFIX, UUID.randomUUID().toString());
        context.put(CONTAINER_POSTFIX, UUID.randomUUID().toString());
        context.put(GOCD_AGENT_IMAGE, "gocd/gocd-agent-alpine-3.9");
        context.put(LATEST_VERSION, "v20.3.0");
        return context;
    }
}
