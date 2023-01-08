# Kubernetes Elastic Agent plugin for GoCD

### Configure Cluster Profile

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Agent Configurations_**
2. Click on **_Add_** button and select `Kubernetes Elastic Agent Plugin` from the plugin ID dropdown.
    1. Specify `Cluster Profile Name` for the new cluster
    1. Optionally specify `Go Server URL`, if GoCD secure site URL is not configured.
    2. Optionally Specify `Agent auto-register timeout (in minutes)`, Defaults to `10` (mintues).
    3. Optionally Specify `Maximum pending pods`, Defaults to `10` (pods).
    4. Specify `Cluster URL`.
    5. Optionally Specify `Namespace`, Defaults to `default`. Note: If you have multiple GoCD servers with cluster profiles pointing to the same Kubernetes cluster, make sure that the namespace is different. Otherwise, the plugin of one GoCD server will end up terminating pods started by the plugin in the other GoCD servers.
    6. Specify `Security token`, The token must have permission to perform the following operations -
        ```
        - nodes: list, get
        - events: list, watch
        - namespace: list, get
        - pods, pods/log: *
        ```
    7. Optionally, Specify `Cluster CA certificate data`.
    
    !["Kubernetes Cluster Profile"][1]
    

[1]: images/cluster-profile.png     "Kubernetes Cluster Profile"
