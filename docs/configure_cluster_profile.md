# Kubernetes Elastic Agent plugin for GoCD

### Configure Cluster Profile

1. Log in to the GoCD server as admin and navigate to **_Admin_** _>_ **_Elastic Agent Configurations_**.

1. Click on the **_Add_** button and select `Kubernetes Elastic Agent Plugin` from the plugin ID dropdown.

1. Specify `Cluster Profile Name` for the new cluster.

1. Optionally specify `Go Server URL`. If your GoCD server has a [secure site URL][secure site URL]
   configured, then the secure site URL is used as a default. Otherwise, a URL must be specified here.

1. Optionally specify `Agent auto-register timeout (in minutes)`. This defaults to 10 (minutes) if not provided.

1. Optionally specify `Maximum pending pods`. This defaults to 10 (pods) if not provided.

1. Specify `Cluster URL`.

1. Optionally specify `Namespace`. If not provided, the plugin will launch GoCD
   agent pods in the default Kubernetes namespace. Note: If you have multiple
   GoCD servers with cluster profiles pointing to the same Kubernetes cluster,
   make sure that the namespace used by each GoCD server is different.
   Otherwise, the plugin of one GoCD server will end up terminating pods
   started by the plugin in the other GoCD servers.

1. Specify `Security token`. This should be a Kubernetes API token linked to a service account which has the
   following permissions:

   | Resource       | Actions |
   | -------------- | ------- |
   | nodes          | list    |
   | events         | list    |
   | pods, pods/log | *       |

   If the plugin is using a non-default namespace, then the pods and pods/log permissions
   can be limited to that namespace (using a role + role binding), and the plugin
   will still work. Nodes list and events list need to be attached at the cluster
   level (using a cluster role + cluster role binding) regardless of the
   namespace chosen.

   If you are comfortable with cluster-wide permissions you can refer to the [example within the GoCD official helm
   chart](https://github.com/gocd/helm-chart/blob/master/gocd/templates/gocd-cluster-role.yaml).

1. Specify `Cluster CA certificate data`. This should be the base-64-encoded certificate
   of the Kubernetes API server. It can be omitted in the rare case that the Kubernetes API
   is configured to serve plain HTTP.

1. Optionally specify the `Cluster request timeout` (in milliseconds).


!["Kubernetes Cluster Profile"][1]


[1]: images/cluster-profile.png     "Kubernetes Cluster Profile"
[secure site URL]: https://docs.gocd.org/current/installation/configuring_server_details.html
