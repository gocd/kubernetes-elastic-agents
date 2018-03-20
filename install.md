# Kubernetes Elastic Agent plugin for GoCD

## Requirements

* GoCD server version v18.2.0 or above
* Kubernetes Cluster

## Installation

Copy the file `build/libs/kubernetes-elastic-agent-plugin-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external` 
and restart the server. The `GO_SERVER_DIR` is usually `/var/lib/go-server` on Linux and `C:\Program Files\Go Server` 
on Windows.

## Configuration

### Configure Plugin Settings

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Plugins_**
2. Click on **_Settings icon_** of `Kubernetes Elastic Agent Plugin` to update plugin settings configuration.
    1. Optionally specify `Go Server URL`, if GoCD secure site URL is not configured.
    2. Optionally Specify `Agent auto-register Timeout`, Defaults to `10 mintues`.
    3. Optionally Specify `Maximum pending pods`, Defaults to `10 pods`.
    4. Specify `Kubernetes Cluster URL`.
    5. Optionally Specify `Namespace`, Defaults to `default`.
    6. Specify `Security token`, The token must have permission to do following operations -
        ```
        - nodes: list, get
        - events: list, watch
        - namespace: list, get
        - pods, pods/log: *
        ```
    7. Optionally, Specify `Cluster ca certificate data`.
    
    !["Kubernetes Plugin settings"][1]

## Create an elastic profile

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Agent Profiles_**
    ![Elastic Profiles][2]

2. Click on **_Add_** to create new elastic agent profile
    1. Specify `id` for profile.
    2. Select `Kubernetes Elastic Agent Plugin` for **_Plugin id_**
    3. Configure Kubernetes GoCD elastic agent Pod using:
       - Config Properties
            1. Specify GoCD elastic agent docker image name.
            2. Specify Maximum Memory limit. Container memory will be limit to the value specified here.
            3. Specify Maximum CPU limit. Container memory will be limit to the value specified here.
            4. Optionally specify Environment Variables. These variables are passed to the container for use.
            
            ![Create elastic profile using config properties][3]

        - Pod Configuration
            1. Specify GoCD elastic agent Pod Yaml configuration.
            
            ![Create elastic profile using pod configuration][4]
    4. Save your profile.
    
    

### Configure job to use an elastic agent profile

1. Click the gear icon on **_Pipeline_**

![Pipeline][5]

2. Click on **_Quick Edit_** button

![Quick edit][6]

3. Click on **_Stages_**
4. Create/Edit a job
5. Enter the `unique id` of an elastic profile in Job Settings

![Configure a job][7]

6. Save your changes

## Troubleshooting

Enabling debug level logging can help you troubleshoot an issue with the elastic agent plugin. To enable debug level logs, edit the `/etc/default/go-server` (for Linux) to add:

```bash
export GO_SERVER_SYSTEM_PROPERTIES="$GO_SERVER_SYSTEM_PROPERTIES -Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug"
```

If you're running the server via `./server.sh` script —

```
$ GO_SERVER_SYSTEM_PROPERTIES="-Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug" ./server.sh
```


[1]: images/plugin-settings.png     "Kubernetes Plugin settings"
[2]: images/profiles-page.png  "Elastic profiles"
[3]: images/profile.png "Create elastic profile using config properties"
[4]: images/profile-with-pod-yaml.png "Create elastic profile using pod configuration"
[5]: images/pipeline.png  "Pipeline"
[6]: images/quick-edit.png  "Quick edit"
[7]: images/configure-job.png  "Configure a job"