<!--
layout: md-content
title: Installation
-->
## Installation

### Using precompiled artifacts

1) Insert dependency to the core module in the _\<dependencies>_ section of the POM file of your **bundle** module:
```xml
<dependency>
   <groupId>com.exadel.etoolbox</groupId>
   <artifactId>etoolbox-authoring-kit-core</artifactId>
   <version>2.1.0</version> <!-- prefer the latest stable version whenever possible -->
    <scope>provided</scope> <!-- do not use compile or runtime scope!-->
</dependency>
```
2) Insert plugin's config in the _\<plugins>_ section of the POM file of your **package** module:
```xml

<plugin>
    <groupId>com.exadel.etoolbox</groupId>
    <artifactId>etoolbox-authoring-kit-plugin</artifactId>
    <version>2.1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>aem-authoring</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- MANDATORY: Place here the path to the node under which your component nodes are stored -->
        <componentsPathBase>jcr_root/apps/projectName/components</componentsPathBase>
        <!-- OPTIONAL: specify root package for component classes -->
        <componentsReferenceBase>com.acme.project.samples</componentsReferenceBase>
        <!-- OPTIONAL: specify list of exceptions that would cause this plugin to terminate -->
        <terminateOn>ALL</terminateOn>
    </configuration>
</plugin>
```
Follow [Plugin settings](docs/content/components/plugin-settings.md) to learn more about the plugin's configuration.

### Installing assets

For many of the ToolKit's features to work properly, namely *DependsOn* and *Lists*, you need to deploy the _etoolbox-authoring-kit-all-<version>.zip_ package to your AEM author instance.

If you are using <u>ready artifacts</u>, the easiest way is to append the cumulative _all_ package to one of your content packages. Since the package is small, this will not hamper your deployment process.

You need to do two steps.
1) Insert the dependency into the cumulative _all_ module in the _\<dependencies>_ section of the POM file of your **package**:
```xml
<dependency>
    <groupId>com.exadel.etoolbox</groupId>
    <artifactId>etoolbox-authoring-kit-all</artifactId>
    <version>2.1.0</version>
    <type>content-package</type>
</dependency>
```
2) Then specify the subpackage in the _Vault_ plugin  you are using (refer to your content plugin documentation for particulars).
 ```xml
    <plugin>
        <groupId>com.day.jcr.vault</groupId>
        <artifactId>content-package-maven-plugin</artifactId>
        <extensions>true</extensions>
        <configuration>
            <!-- ... -->
            <subPackages>
                <subPackage>
                    <groupId>com.exadel.etoolbox</groupId>
                    <artifactId>etoolbox-authoring-kit-all</artifactId>
                    <filter>true</filter>
                </subPackage>
            </subPackages>
            <targetURL>http://${aem.host}:${aem.port}/crx/packmgr/service.jsp</targetURL>
        </configuration>
    </plugin>
```

#### Compiling and deploying by hand

Feel free to clone the project sources and run ```mvn clean install``` from the project's root folder. The plugin and the API artifacts will be installed in the local .m2 repository.

You can run the build with the *install-assets* profile like `mvn clean install -Pinstall-assets`. Then the project will be deployed to an AEM instance. You might need to change the following values in the *properties* part of the project's main _POM_ file:
```
<aem.host>10.0.0.1</aem.host> <!-- Your AEM instance address or hostname -->
<aem.port>4502</aem.port> <!-- Your AEM instance port -->
```

You can also use <u>other means</u> to get the content package deployed to the AEM instance by adding it manually via the _Package Manager_ or posting to an HTTP endpoint.

### Troubleshooting installation issues

1) Add the _etoolbox-authoring-kit-plugin_ after the rest of plugins in your package.

2) For the plugin to work properly, make sure that the _\<dependencies>_ section of your package POM file contains all the dependencies that are required by the components in the corresponding bundle. For example, if the plugin is expected to build UI for a Java class that refers to the ACS Commons bundle, the dependency to the ACS Commons must be present in the package as well as in the bundle (however it is not directly needed by the package itself).

3) Make sure that the dependency section of your package POM file where AEM components are situated includes a dependency to the bundle where their Java backend is declared.
