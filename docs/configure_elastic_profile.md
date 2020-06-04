# Kubernetes Elastic Agent plugin for GoCD

## Create an Elastic Agent Profile

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Agent Configurations_**.

    ![Elastic Profiles][1]

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
            
            ![Create elastic profile using config properties][2]

        - Pod Configuration
            1. Specify GoCD elastic agent Pod Yaml configuration.
            
            ![Create elastic profile using pod configuration][3]

        - Remote File
            1. Specify the file name and type (`json` or `yaml`)

            ![Create elastic profile using remote file configuration][4]

    5. Save your profile.
    

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

In the pod spec, specify the `volumes` section if not present and include the contents specified below:

```yaml
volumes:
  - name: ssh-secrets
    secret:
      defaultMode: 420
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

[1]: images/profiles-page.png  "Elastic profiles"
[2]: images/profile.png "Create elastic profile using config properties"
[3]: images/profile-with-pod-yaml.png "Create elastic profile using pod configuration"
[4]: images/profile_with_remote_file.png "Create elastic profile using remote file configuration"
