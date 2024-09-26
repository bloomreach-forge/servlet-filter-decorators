<!--
  Copyright 2024 Bloomreach B.V. (http://www.bloomreach.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  -->
## Using the Servlet Filter Decorators plugin

### Configuration

#### Properties of a configuration set 

|Property               | Type            | Default         | Description 
|------------------------|-----------------|-----------------|------------- 
|`enabled`              | boolean         | true            | Enable this configuration or not.
|`hostnames`            | multiple string |                 | **Mandatory** list of hostnames, matching a browser request to this configuration set (NOTE: compiled as java regex Pattern).   
|`contextpaths`            | multiple string |                 | **Mandatory** list of context paths, matching number of hostnames.   
|`hostheader` | string          | X-Forwarded-Host | Name of the request header that is used for host detection.



### example configuration:
```yaml

        jcr:primaryType: hipposys:moduleconfig
          enabled: true
          hostheader: X-Forwarded-Host
          hostnames: [localhost, 127.0.0.1, .*onehippo\.com, .*onehippo\.org]
          contextpaths: [/site, /site, /, /]

```