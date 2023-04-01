package cd.go.contrib.elasticagent.model.reports.agent;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.EnvVar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class GoCDContainerDetails {
    private String name;
    private String image;
    private String imagePullPolicy;
    private List<String> command;
    private List<EnvironmentVariable> env;
    private Boolean ready;
    private Integer restartCount;

    public static GoCDContainerDetails fromContainer(Container container, ContainerStatus containerStatus) {
        GoCDContainerDetails containerDetails = new GoCDContainerDetails();

        containerDetails.name = container.getName();
        containerDetails.image = container.getImage();
        containerDetails.imagePullPolicy = container.getImagePullPolicy();

        containerDetails.command = container.getCommand();
        containerDetails.env = new ArrayList<>();
        for (EnvVar var : container.getEnv()) {
            containerDetails.env.add(new EnvironmentVariable(var.getName(), var.getValue()));
        }
        if (containerStatus != null) {
            containerDetails.ready = containerStatus.getReady();
            containerDetails.restartCount = containerStatus.getRestartCount();
        }
        else {
            containerDetails.ready = false;
            containerDetails.restartCount = 0;
        }

        return containerDetails;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public String getCommand() {
        return String.join("\n", command);
    }

    public String getEnv() {
        return env == null ? "" : env.stream().map(EnvironmentVariable::toString).collect(Collectors.joining("\n"));
    }

    public String getReady() {
        return String.valueOf(ready);
    }

    public String getRestartCount() {
        return String.valueOf(restartCount);
    }

    private static class EnvironmentVariable {
        private final String name;
        private final String value;

        public EnvironmentVariable(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return format("{0}: {1}", getName(), getValue());
        }
    }
}
