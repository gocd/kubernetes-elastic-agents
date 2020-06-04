# Kubernetes Elastic Agent plugin for GoCD

## Requirements

* GoCD server version v19.3.0 or above
* Kubernetes Cluster

## Installation

Copy the file `build/libs/kubernetes-elastic-agent-plugin-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external` 
and restart the server. The `GO_SERVER_DIR` is usually `/var/lib/go-server` on Linux and `C:\Program Files\Go Server` 
on Windows.

## Configuration

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
    7. Optionally, Specify `Cluster ca certificate data`.
    
    !["Kubernetes Cluster Profile"][1]

## Create an Elastic Agent Profile

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Agent Configurations_**.

    ![Elastic Profiles][2]

2. Click on **_New Elastic Agent Profile_** to create new elastic agent profile for a cluster.
    1. Specify a name for the elastic agent profile.
    2. Select `Kubernetes Elastic Agent Plugin` for **_Plugin id_**
    3. Verify Cluster Profile Id of the newly defined Elastic Agent Profile.
    4. Configure Kubernetes GoCD elastic agent Pod using:
        - Config Properties
            1. Specify GoCD elastic agent docker image name.
            2. Specify Maximum Memory limit. Container memory will be limit to the value specified here.
            3. Specify Maximum CPU limit. Container memory will be limit to the value specified here.
            4. Optionally specify Environment Variables. These variables are passed to the container for use.
            
            ![Create elastic profile using config properties][3]

        - Pod Configuration
            1. Specify GoCD elastic agent Pod Yaml configuration.
            
            ![Create elastic profile using pod configuration][4]

        - Remote File
            1. Specify the file name and type (`json` or `yaml`)

            ![Create elastic profile using remote file configuration][7]

    5. Save your profile.
    
    

### Configure job to use an elastic agent profile

1. Click the gear icon on **_Pipeline_**.

![Pipeline][5]

2. Click on **_Stages_**.
3. Create or edit a job.
4. Enter the name of an elastic profile in the Job Settings tab.

![Configure a job][6]

5. Save your changes

## Troubleshooting

### More logging

You can enable more logging using the insructions below:

#### If you are on GoCD version 19.6 and above:

Edit the file `wrapper-properties.conf` on your GoCD server and add the following options. The location of the `wrapper-properties.conf` can be found in the [installation documentation](https://docs.gocd.org/current/installation/installing_go_server.html) of the GoCD server.

 ```properties
# We recommend that you begin with the index `100` and increment the index for each system property
wrapper.java.additional.100=-Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug
```

If you're running with GoCD server 19.6 and above on docker using one of the supported GoCD server images, set the environment variable `GOCD_SERVER_JVM_OPTIONS`:

 ```shell
docker run -e "GOCD_SERVER_JVM_OPTIONS=-Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug" ...
```

---

#### If you are on GoCD version 19.5 and lower:

##### **On Linux:**

Enabling debug level logging can help you troubleshoot an issue with this plugin. To enable debug level logs, edit the file `/etc/default/go-server` (for Linux) to add:

```shell
export GO_SERVER_SYSTEM_PROPERTIES="$GO_SERVER_SYSTEM_PROPERTIES -Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug"
```

If you're running the server via `./server.sh` script:

```shell
$ GO_SERVER_SYSTEM_PROPERTIES="-Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug" ./server.sh
```

The logs will be available under `/var/log/go-server`

##### **On Windows:**

Edit the file `config/wrapper-properties.conf` inside the GoCD Server installation directory (typically `C:\Program Files\Go Server`):

```
# config/wrapper-properties.conf
# since the last "wrapper.java.additional" index is 15, we use the next available index.
wrapper.java.additional.16=-Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug
```

The logs will be available under the `logs` folder in the GoCD server installation directory.

### Pods getting terminated while running jobs

If you have multiple GoCD servers with cluster profiles pointing to the same Kubernetes cluster, make sure that the namespace is different.

Otherwise, the plugin of one GoCD server will end up terminating pods started by the plugin in the other GoCD servers, since those pods
won't register with it.

---

[1]: images/cluster-profile.png     "Kubernetes Cluster Profile"
[2]: images/profiles-page.png  "Elastic profiles"
[3]: images/profile.png "Create elastic profile using config properties"
[4]: images/profile-with-pod-yaml.png "Create elastic profile using pod configuration"
[5]: images/pipeline.png  "Pipeline"
[6]: images/configure-job.png  "Configure a job"
[7]: images/profile_with_remote_file.png "Create elastic profile using remote file configuration"
