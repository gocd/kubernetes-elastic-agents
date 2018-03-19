## 1.0.0 - 2018-03-19

### Added
- Assign work to kubernetes GoCD agent based on job identifier.
- Restrict creation of new pods when pending pod count is already at user specified threshold.
- Support for agent status report with following information -
    - Pod information
    - GoCD agent information
    - Pod events
    - Pod logs
    - Pod configuration in form of YAML file
- Support to configure plugin settings using service account token and CA cert of cluster.

**_NOTE: _** *_Requires GoCD version 18.2.0 or higher. Plugin will not work with the older version of GoCD._*

 
## 0.0.1 - 2017-10-26

Initial release of plugin.