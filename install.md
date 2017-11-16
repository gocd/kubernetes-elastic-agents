# Kubernetes Elastic Agent plugin for GoCD

## Requirements

* GoCD server version v17.9.0 or above
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
    2. Specify `Agent auto-register Timeout`
    3. Specify `Kubernetes Cluster URL`.
    4. Optionally specify `Kubernetes Cluster Username`, `Kubernetes Cluster Password` and `Kubernetes Cluster CA Certificate`, for secure Kubernetes Cluster.

    !["Kubernetes Plugin settings"][1]

### Configure Autoregister Properties

As per [GoCD docs](https://docs.gocd.org/current/advanced_usage/agent_auto_register.html), you will need to add the following keys to autoregister.properties:

    agent.auto.register.elasticAgent.agentId
    agent.auto.register.elasticAgent.pluginId

The plugin provides values for both of these keys as environment variables ``GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID`` and ``GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID`` respectively.

Here is an example of how to dynamically add those values:

    sed -i "s/agent.auto.register.elasticAgent.agentId=\(.*\)/agent.auto.register.elasticAgent.agentId=${GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID}/g" /var/lib/go-agent/config/autoregister.properties
    sed -i "s/agent.auto.register.elasticAgent.pluginId=\(.*\)/agent.auto.register.elasticAgent.pluginId=${GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID}/g" /var/lib/go-agent/config/autoregister.properties

**NOTE:** If GoCD is running as a user other than root, ensure to chown the user and group of autoregister.properties back to the non-root user after making the above changes.

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
