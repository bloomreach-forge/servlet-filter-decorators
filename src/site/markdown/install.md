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
## Installation of the Servlet Filter Decorators  plugin

These instructions assume a brXM project based on the Bloomreach website archetype, i.e. a Maven multi-module project 
with parent pom `org.onehippo.cms7:hippo-cms7-release` and consisting of at least three sub-modules: cms, site and repository-data.

### Forge Repository
In the main pom.xml of the project, in the repositories section, add this repository if it is not configured there yet. 

```
<repository>
  <id>bloomreach-forge</id>
  <name>Bloomreach Forge maven 2 repository.</name>
  <url>https://maven.bloomreach.com/maven2-forge/</url>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
  <releases>
    <updatePolicy>never</updatePolicy>
  </releases>
  <layout>default</layout>
</repository>
```

### Dependency management 
Add this property to the properties section of the root pom.xml:

    <hippo.forge.servlet-filter-decorators.version>version.number</hippo.forge.servlet-filter-decorators.version>

Select the correct version for your project. See the [release notes](release-notes.html) for more information on which 
version is applicable.

Add these dependencies to the `<dependencyManagement>` section of the root pom.xml:

```
  <dependency>
      <groupId>org.onehippo.forge.servlet-filter-decorators</groupId>
      <artifactId>servlet-filter-decorators-hst</artifactId>
    <version>${hippo.forge.servlet-filter-decorators.version}</version>
  </dependency>
  <dependency>
      <groupId>org.onehippo.forge.servlet-filter-decorators</groupId>
      <artifactId>servlet-filter-decorators-cms</artifactId>
    <version>${hippo.forge.servlet-filter-decorators.version}</version>
  </dependency>
```

### Installation in site application

Add this dependency to the `<dependencies>` section of the site/pom.xml. It contains the site decorator filter.

```
  <dependency>
      <groupId>org.onehippo.forge.servlet-filter-decorators</groupId>
      <artifactId>servlet-filter-decorators-hst</artifactId>
  </dependency>
```

Add the following filter to the site's web.xml. It should be defined as **second** filter mapping in chain so just after 
CharacterEncodingFilter (in a standard brXM project).

```  
  <filter>
    <filter-name>WrapperFilter</filter-name>
    <filter-class>org.onehippo.forge.servlet.decorators.hst.HstDecorateFilter</filter-class>
  </filter>
  <filter>
    <filter-name>UnWrapperFilter</filter-name>
    <filter-class>org.onehippo.forge.servlet.decorators.hst.HstUndecorateFilter</filter-class>
  </filter>

  <!--  decorate mapping -->
  <filter-mapping>
    <filter-name>WrapperFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

  <filter-mapping>
    <filter-name>FOR_EXAMPLE_SOME_SECURITY_FILTER</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
    <!--  un-decorate mapping -->
  <filter-mapping>
    <filter-name>UnWrapperFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```

### Installation in CMS application

Always add the following dependency to the `<dependencies>` section of the cms/pom.xml. It contains default 
configuration and the CMS filter.

```
  <dependency>
       <groupId>org.onehippo.forge.servlet-filter-decorators</groupId>
       <artifactId>servlet-filter-decorators-cms</artifactId>
  </dependency>
```

**Optionally, install the CMS filter.**


```  
  <filter>
    <filter-name>WrapperFilter</filter-name>
    <filter-class>org.onehippo.forge.servlet.decorators.cms.CmsDecoratorFilter</filter-class>
  </filter>
  <filter>
    <filter-name>UnWrapperFilter</filter-name>
    <filter-class>org.onehippo.forge.servlet.decorators.cms.CmsUndecorateFilter</filter-class>
  </filter>

  <!--  decorate mapping -->
  <filter-mapping>
    <filter-name>WrapperFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

  <filter-mapping>
    <filter-name>FOR_EXAMPLE_SOME_SECURITY_FILTER</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
    <!--  un-decorate mapping -->
  <filter-mapping>
    <filter-name>UnWrapperFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```

Rebuild your project and distribute. In case you start with an existing repository don't forget to add *-Drepo.bootstrap=true*
to your startup options.

