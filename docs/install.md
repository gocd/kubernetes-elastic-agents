# Kubernetes Elastic Agent plugin for GoCD

## Prerequisites

* GoCD server version v20.9.0 or above
* Kubernetes Cluster

## Installation

* Download the right version of the plugin from [the Releases](https://github.com/gocd/kubernetes-elastic-agents/releases) page. The direct link to the latest released version is: https://github.com/gocd/kubernetes-elastic-agents/releases/latest
* Copy the file `kubernetes-elastic-agent-plugin-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external` 
and restart the server. 
* The `GO_SERVER_DIR` is usually `/var/lib/go-server` on Linux and `C:\Program Files\Go Server` 
on Windows.

## Configuring the plugin

1. Configure a [Cluster Profile](configure_cluster_profile.md)

2. Configure an [Elastic Profile](configure_elastic_profile.md)

3. Configure job to use an elastic agent profile

    1. Click the gear icon on **_Pipeline_**.
    
    ![Pipeline][1]
    
    2. Click on **_Stages_**.
    3. Create or edit a job.
    4. Enter the name of an elastic profile in the Job Settings tab.
    
    ![Configure a job][2]
    
    5. Save your changes


[1]: images/pipeline.png  "Pipeline"
[2]: images/configure-job.png  "Configure a job"
