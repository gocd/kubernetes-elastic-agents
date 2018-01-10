package cd.go.contrib.elasticagent.model.reports.agent;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.EnvVar;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GoCDContainerDetails {
    private String name;
    private String image;
    private String imagePullPolicy;
    private List<String> command;
    private ArrayList<EnvironmentVariable> env;
    private Boolean ready;
    private Integer restartCount;

    public static GoCDContainerDetails fromContainer(Container container, ContainerStatus containerStatus) {
        GoCDContainerDetails containerDetails = new GoCDContainerDetails();

        containerDetails.name = container.getName();
        containerDetails.image = container.getImage();
        containerDetails.imagePullPolicy = container.getImagePullPolicy();

        containerDetails.command = container.getCommand();
        containerDetails.env = new ArrayList<EnvironmentVariable>();
        for (EnvVar var : container.getEnv()) {
            containerDetails.env.add(new EnvironmentVariable(var.getName(), var.getValue()));
        }

        containerDetails.ready = containerStatus.getReady();
        containerDetails.restartCount = containerStatus.getRestartCount();

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
        return StringUtils.join(command, '\n');
    }

    public String getEnv() {
        return StringUtils.join(env, '\n');
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
            return String.format("%s: %s", getName(), getValue());
        }
    }
}
