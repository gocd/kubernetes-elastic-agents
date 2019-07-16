# Kubernetes Elastic agent plugin for GoCD

Kubernetes Elastic Agent Plugin provides production grade support to run GoCD Elastic Agents on Kubernetes Cluster. 

# Documentation

Read about setting up a Kubernetes cluster and using the GoCD elastic agent for Kubernetes in our [documentation](https://docs.gocd.org/current/gocd_on_kubernetes/introduction.html).

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

### Pull image from private registry

###### Create a Kubernetes secret

```bash
kubectl create secret docker-registry \
	my-docker-registry \
	--namespace gocd \
	--docker-server=<docker_server_url> \
	--docker-username=<username> \
	--docker-password=<password> \
	--docker-email=<email>
```


###### Configure pod yaml

In the pod spec, specify the `imagePullSecrets` section:

```yaml
imagePullSecrets:
  - name: my-docker-registry
```

# Installation

Documentation for installation is available [here](install.md)

## Building the code base

To build the jar, run `./gradlew clean build`

## Deploy on dev machine

To build the jar, run `./gradlew deploy -Pserver_path=~/gocd`

## Troubleshooting

### Enable Debug Logs

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

#### If you are on GoCD version 19.5 and lower:

* On Linux:

    Enabling debug level logging can help you troubleshoot an issue with this plugin. To enable debug level logs, edit the file `/etc/default/go-server` (for Linux) to add:

    ```shell
    export GO_SERVER_SYSTEM_PROPERTIES="$GO_SERVER_SYSTEM_PROPERTIES -Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug"
    ```

    If you're running the server via `./server.sh` script:

    ```shell
    $ GO_SERVER_SYSTEM_PROPERTIES="-Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug" ./server.sh
    ```

* On windows:

    Edit the file `config/wrapper-properties.conf` inside the GoCD Server installation directory (typically `C:\Program Files\Go Server`):

    ```
    # config/wrapper-properties.conf
    # since the last "wrapper.java.additional" index is 15, we use the next available index.
    wrapper.java.additional.16=-Dplugin.cd.go.contrib.elasticagent.kubernetes.log.level=debug
    ```

## License

```plain
Copyright 2019 ThoughtWorks, Inc.

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
