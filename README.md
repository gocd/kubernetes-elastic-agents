# Kubernetes Elastic agent plugin for GoCD

Kubernetes Elastic Agent Plugin provides production grade support to run GoCD Elastic Agents on Kubernetes Cluster. 

# Documentation

Read about setting up a Kubernetes cluster and using the GoCD elastic agent for Kubernetes in our [documentation](https://docs.gocd.org/current/gocd_on_kubernetes/).

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
