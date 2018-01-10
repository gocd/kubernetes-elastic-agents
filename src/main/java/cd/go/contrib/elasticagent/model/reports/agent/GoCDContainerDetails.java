package cd.go.contrib.elasticagent.model.reports.agent;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;

public class GoCDContainerDetails {
    private String name;
    private String image;

    public static GoCDContainerDetails fromContainer(Container container, ContainerStatus containerStatus) {
        GoCDContainerDetails containerDetails = new GoCDContainerDetails();
        containerDetails.name = container.getName();
        containerDetails.image = container.getImage();
        return containerDetails;
    }
}
