# Kubernetes Elastic agent plugin for GoCD

Kubernetes Elastic Agent Plugin provides production grade support to run GoCD Elastic Agents on Kubernetes Cluster. 

Table of Contents
=================

  * [Building the code base](#building-the-code-base)
  * [Documentation](#documentation)
  * [Install and configure the plugin](/docs/install.md)
    * [Prerequisites](/docs/install.md#prerequisites)
    * [Installation](/docs/install.md#installation)
    * [Configuration](/docs/install.md#configuring-the-plugin)
        * [Configure Cluster Profile](/docs/configure_cluster_profile.md)
        * [Configure Elastic Profile](/docs/configure_elastic_profile.md)
            * [Configuring SSH keys](/docs/configure_elastic_profile.md#configuring-ssh-keys-for-kubernetes-elastic-agents)
            * [Pull image from private registry](/docs/configure_elastic_profile.md#pull-image-from-private-registry)
  * [Troubleshooting](/docs/troubleshoot.md)


## Building the code base

To build the jar, run `./gradlew clean test assemble`

# Documentation

Read about setting up a Kubernetes cluster and using GoCD on Kubernetes in [GoCD's documentation](https://docs.gocd.org/current/gocd_on_kubernetes/introduction.html). Installation and configuration documentation for this plugin is available [here](docs/install.md).

## License

```plain
Copyright 2022 Thoughtworks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
