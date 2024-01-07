# Kubernetes Elastic Agent plugin for GoCD

### Configure Cluster Profile

1. Log in to the GoCD server as admin and navigate to **_Admin_** _>_ **_Elastic Agent Configurations_**.

1. Click on the **_Add_** button and select `Kubernetes Elastic Agent Plugin` from the plugin ID dropdown.

1. Specify `Cluster Profile Name` for the new cluster.

1. Optionally specify `Go Server URL`. If your GoCD server has a [secure site URL][secure site URL]
   configured, then the secure site URL is used as a default. Otherwise, a URL must be specified here.

1. Optionally specify `Agent auto-register timeout (in minutes)`. This defaults to 10 (minutes) if not provided.

1. Optionally specify `Maximum pending pods`. This defaults to 10 (pods) if not provided.

1. Optionally specify `Cluster Information`.<br/>
   Since plugin version `4.x`, when the server is running on Kubernetes the plugin 
   will auto-configure itself based on standard Kubernetes environment variables and `ServiceAccount` tokens automounted
   into the pod, so none of the values here need to be configured in many cases.

   1. If you **use the [GoCD Helm Chart](https://artifacthub.io/packages/helm/gocd/gocd)**, and intend to create agents in the
      same cluster as the GoCD server, ensure the service account is enabled in the Chart (default behaviour) and you 
      have nothing else mandatory to configure.
   
   2. If **not using the Helm chart**, create a `ServiceAccount` that has the following permissions in its linked roles
      for the target cluster you want to create/manage elastic agents as Kubernetes pods.
      
      | Resource       | Actions |
      |----------------| ------- |
      | nodes          | list    |
      | events         | list    |
      | pods, pods/log | *       |
      
      If the plugin is using a non-default namespace, then the pods and pods/log permissions
      can be limited to that namespace (using a role + role binding), and the plugin
      will still work. Nodes list and events list need to be attached at the cluster
      level (using a cluster role + cluster role binding) regardless of the namespace chosen.

      If you are comfortable with cluster-wide permissions you can refer to the [example within the GoCD official helm
      chart](https://github.com/gocd/helm-chart/blob/master/gocd/templates/gocd-cluster-role.yaml).
 
   3. If you are **running your server in Kubernetes**, ensure the service account token linked to the above can be
      auto-mounted into the GoCD server pod, and you also have nothing further mandatory to configure.

   4. If you are **running outside Kubernetes**, or **need to override the defaults**, continue:
      - Optionally specify `Cluster URL`. Mandatory if running server outside Kubernetes.
      - Optionally specify `Namespace`. Mandatory if running server outside Kubernetes. Note: If you have multiple
        GoCD servers with cluster profiles pointing to the same Kubernetes cluster,
        make sure that the namespace used by each GoCD server is different.
        Otherwise, the plugin of one GoCD server will end up terminating pods
        started by the plugin in the other GoCD servers.
      - Optionally specify `Security token`. This should be a Kubernetes API token linked to a service account which has
        the permissions noted above. Since Kubernetes is moving away from legacy (indefinite expiry) tokens, specifying
        the token here is not recommended, as it cannot be auto-refreshed. However, if you need to get such a token
        create a new secret like the below in the same namespace as the service account:
        
        ```yaml
        kind: Secret
        apiVersion: v1
        metadata:
          name: gocd-sa-secret
          namespace: gocd
          annotations:
            kubernetes.io/service-account.name: gocd
        type: kubernetes.io/service-account-token
        ```
        Extract the token value to paste into the config with something like the below (you may want to directly put onto
        your clipboard to avoid the value appearing in your shell)
        ```shell
        kubectl get secret gocd-sa-secret -o json | jq -r '.data.token' | base64 --decode
        ```

      - Optionally specify `Cluster CA certificate data`. This should be the PEM format certificate
        of the Kubernetes API server. Similar to the above, this can be found from the service account token secret:
        ```shell
        kubectl get secret gocd-sa-secret -o json | jq -r '.data."ca.crt"' | base64 --decode
        ```
      - Optionally specify the `Cluster request timeout` (in milliseconds).


!["Kubernetes Cluster Profile"][1]


[1]: images/cluster-profile.png     "Kubernetes Cluster Profile"
[secure site URL]: https://docs.gocd.org/current/installation/configuring_server_details.html
