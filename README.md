# Kubernetes Elastic agent plugin for GoCD

Kubernetes Elastic Agent Plugin provides production grade support to run GoCD Elastic Agents on Kubernetes Cluster. 

# Documentation

Read about setting up a Kubernetes cluster and using the GoCD elastic agent for Kubernetes in our [documentation](https://docs.gocd.org/current/gocd_on_kubernetes/).

### Configuring SSH keys for Kubernetes Elastic Agents

For accessing repositories over SSH, you need to add SSH keys to the elastic agent pod spec. Generate a new keypair, fetch the host key for the [host] you want to connect to and create the secret. The secret is structured to hold the entire contents of the .ssh folder on the GoCD agent.

_Note: The steps provided below are for the official GoCD agent images listed on [DockerHub](https://hub.docker.com/u/gocd)._

###### Create a Kubernetes secret

```bash

$ ssh-keygen -t rsa -b 4096 -C "user@example.com" -f gocd-agent-ssh -P ''
$ ssh-keyscan [host] > gocd_known_hosts
$ kubectl create secret generic gocd-agent-ssh \
    --from-file=id_rsa=gocd-agent-ssh \
    --from-file=id_rsa.pub=gocd-agent-ssh.pub \
    --from-file=known_hosts=gocd_known_hosts
```
Be sure to add the contents of `gocd-agent-ssh.pub` to your [host].

###### Configure elastic agent profile

In the pod spec, specify the `volumes` section if not present and include the contents specified below:

```yaml
volumes:
  - name: ssh-secrets
    secret:
    secretName: gocd-agent-ssh

```

In the container spec, specify the `volumeMounts` section if not present and include the contents specified below:

```yaml
volumeMounts:
  - name: ssh-secrets
    readOnly: true
    mountPath: /home/go/.ssh

```

# Installation

Documentation for installation is available [here](install.md)

## Building the code base

To build the jar, run `./gradlew clean build`

## Deploy on dev machine

To build the jar, run `./gradlew deploy -Pserver_path=~/gocd`

## License

```plain
Copyright 2018 ThoughtWorks, Inc.

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
