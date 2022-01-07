# Exadel Authoring Kit for AEM

![EToolbox Authoring Kit Logo](./docs/img/logo.png)

***
![License](https://img.shields.io/github/license/exadel-inc/etoolbox-authoring-kit)
![Latest release](https://img.shields.io/github/v/release/exadel-inc/etoolbox-authoring-kit?color=%23ed8756)
![Maven Central version](https://img.shields.io/maven-central/v/com.exadel.etoolbox/etoolbox-authoring-kit)
[![javadoc](https://javadoc.io/badge2/com.exadel.etoolbox/etoolbox-authoring-kit-core/javadoc.svg)](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core)
![GitHub repo stats](https://img.shields.io/github/stars/exadel-inc/etoolbox-authoring-kit?style=flat&color=%23FFD700)

Exadel Authoring Kit for AEM (part of Exadel Toolbox), or simply the **ToolKit**, is a set of tools for creating robust Granite/Touch UI dialogs and other authoring interfaces for Adobe Experience Manager&trade; with Java and Maven.

The ToolKit provides the fastest way to supplement an AEM component based on a Sling model or POJO with an autogenerated _Touch UI_ dialog and/or in-place editing interface. It also supports automatic generation of page properties dialogs, design dialogs and similar interface types.

The ToolKit has many options to make authoring interfaces rich, flexible, and responsive with minimal effort. No deep knowledge of Granite&trade;, Coral&trade;, or AEM clientlibs is needed. A scope of fine interactivity features is ready out of the box with minimal to no manual setup.

The ToolKit is compliant with the newest facilities of AEM 6.4/6.5, Granite UI, and Coral v.3+, and has support for Coral v.2.

***
> Learn and practice using the ToolKit with our **sandbox project** under [_samples_](/samples/README.md)
>
***

The ToolKit is developed in the course of Exadel&trade; Marketing Technology Practice (the MarTech) as a part of **Exadel Toolbox** initiative.

It is an open and growing project. The authors sincerely welcome creative input from the AEM community worldwide to bring the best programming techniques and design.

## Installation

### Using precompiled artifacts

1) Insert dependency to the core module in the _\<dependencies>_ section of the POM file of your **bundle** module:
```xml
<dependency>
   <groupId>com.exadel.etoolbox</groupId>
   <artifactId>etoolbox-authoring-kit-core</artifactId>
   <version>2.0.8</version> <!-- prefer latest stable version whenever possible -->
    <scope>provided</scope> <!-- do not use compile or runtime scope!-->
</dependency>
```
2) Insert plugin's config in the _\<plugins>_ section of the POM file of your **package** module:
```xml

<plugin>
    <groupId>com.exadel.etoolbox</groupId>
    <artifactId>etoolbox-authoring-kit-plugin</artifactId>
    <version>2.0.8</version>
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
Follow [Plugin settings](docs/md/plugin-settings.md) to learn more about the plugin's configuration.

### Installing assets

For many of the ToolKit's features to work properly, namely *DependsOn* and *Lists*, you need to deploy the _etoolbox-authoring-kit-all-<version>.zip_ package to your AEM author instance.

If you are using <u>ready artifacts</u>, the easiest way is to append the cumulative _all_ package to one of your content packages. Since the package is small, this will not hamper your deployment process.

You need to do two steps.
1) Insert the dependency into the cumulative _all_ module in the _\<dependencies>_ section of the POM file of your **package**:
```xml
<dependency>
    <groupId>com.exadel.etoolbox</groupId>
    <artifactId>etoolbox-authoring-kit-all</artifactId>
    <version>2.0.8</version>
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

## Troubleshooting installation issues

1) Add the _etoolbox-authoring-kit-plugin_ after the rest of plugins in your package.

2) For the plugin to work properly, make sure that the _\<dependencies>_ section of your package POM file contains all the dependencies that are required by the components in the corresponding bundle. For example, if the plugin is expected to build UI for a Java class that refers to the ACS Commons bundle, the dependency to the ACS Commons must be present in the package as well as in the bundle (however it is not directly needed by the package itself).

3) Make sure that the dependency section of your package POM file where AEM components are situated includes a dependency to the bundle where their Java backend is declared.

## Common use cases

#### 1. Auto-generated Touch UI dialog, edit config and component detail for an AEM component

- <u>In the package module</u>: you only need the _component.html_ file in your _/apps/.../path/to/my/component_ folder
- <u>In the bundle module</u>:
```java
@Model(adaptables = Resource.class) // Sling annotation
@AemComponent( // To mark this class as processed by the ToolKit, and to populate .content.xml
    path = "path/to/my/component",
    title = "Simple Text Component",
    componentGroup = "My Components"
)
@Dialog( // To add cq:dialog to the component node
    width = 600,
    height = 800,
    helpPath = "https://acme.com/docs"
)
@EditConfig( // To add cq:editConfig to the component node
        actions = {ActionConstants.EDIT, ActionConstants.DELETE}
)
public class MyComponentModel {
    @ValueMapValue // Sling annotation
    @DialogField(
        label = "Text Field", // Will create a TextField within the dialog
        description = "Enter the text",
        required = true
    )
    @TextField
    private String text;
}
```

#### 2. Auto-generated Touch UI dialog, design dialog, edit config and component detail for an AEM component (unites multiple Java files)

- <u>In the package module</u>: you only need the _component.html_ file in your _/apps/.../path/to/my/component_ folder
- <u>In the bundle module</u>:
```java
@AemComponent(
    path = "/apps/path/to/my/component", // You can specify either a relative path that will start from the point
    title = "Complex Component",         // specified by the "componentsPathBase" setting, or an "absolute" one
    componentGroup = "My Components",
    views = {
        EditConfigHolder.class,
        DesignDialogHolder.class
    }
)
@Dialog
public class MyComplexComponentPojo {
    @DialogField(
        label = "Text Field",
        description = "Enter the text",
        required = true)
    @TextField
    private String text;
}

/* ...Elsewhere in the code */

@EditConfig(
    dropTargets = @DropTargetConfig(
        accept = "image/.*",
        propertyName = "image/fileReference",
        nodeName = "image"
    ),
    inplaceEditing = {
        @InplaceEditingConfig(
            title = "Header",
            propertyName = "header",
            type = "text",
            editElementQuery = ".editable-header"
        )
    }
)
public interface EditConfigHolder { // Can be reused across many components
}

/* ...Elsewhere in the code */

@DesignDialog(title = "Complex Component Design")
public class DesignDialogHolder { // Can be reused across many components
    @DialogField(
        label = "Path Selector",
        description = "Enter a path"
    )
    @PathField(rootPath = "/content")
    private String getPath(); // ToolKit annotations can be hooked to methods as well as fields
}
```

#### 3. Touch UI dialog for an AEM component with its content organized in tabs; contains a FieldSet
- <u>In the package module</u>: you only need the _component.html_ file in your _/apps/.../path/to/my/component_ folder
- <u>In the bundle module</u>:

```java
@Model(adaptables = Resource.class)
@AemComponent(
        path = "path/to/my/component",
        title = "Composite Component",
        componentGroup = "My Components"
)
@Dialog
@Tabs({
    @Tab(title = "First Tab"),
    @Tab(title = "Second Tab")
})
public class CompositeComponentModel {
    @ValueMapValue
    @DialogField(label = "Text Field")
    @TextField
    @Place("First Tab")
    private String text;

    @Self // Sling injector annotation
    @FieldSet
    @Place("Second Tab")
    private MyFieldSet myFieldSet1;

    @Self // Sling injector annotation
    @FieldSet(namePostfix = "_2")
    @Place("Second Tab")
    private MyFieldSet myFieldSet1;
}

/* ...Elsewhere in the code */

@Model(adaptables = Resource.class)
public class MyFieldSet { // Could as well be a private nested class in CompositeComponentModel
    @ValueMapValue
    @DialogField(label = "Numeric Value")
    @NumberField
    private String number;

    @ValueMapValue
    @DialogField(label = "Description")
    @TextArea
    private String description;
}
```

#### 4. Touch UI dialog for an AEM component with a MultiField
- <u>In the package module</u>: you only need the _component.html_ file in your _/apps/.../path/to/my/component_ folder
- <u>In the bundle module</u>:

```java
@AemComponent(
    path = "path/to/my/component",
    title = "Multifields Sample",
    componentGroup = "My Components"
)
@Dialog
@Tabs(@Tab(title = "General Config"))
public class MultifieldsSample {
    @DialogField(label = "String dictionary")
    @MultiField // This will create the first multifield
    @Place("General Config")
    private List<KeyValuePair> dictionary;

    @DialogField(label = "String array")
    @TextField
    @Multiple // This will create the second multifield consisting of a single text input
    @Place("General Config")
    private String[] strings;

    private static class KeyValuePair {
        @DialogField(label = "Enter Key")
        @TextField
        private String key;

        @DialogField(label = "Enter Value")
        @TextField
        private String value;
    }
}
```

#### 5. Touch UI dialog for an AEM component with dynamically displayed content (this is also a custom item for an Exadel Toolbox List)

- <u>In the package module</u>: you only need the _component.html_ file in your _/apps/.../path/to/list/item_ folder
- <u>In the bundle module</u>:

```java
@Model(adaptables = Resource.class)
@AemComponent(
    path = "path/to/list/item",
    title = "Exadel Toolbox List Item"
)
@Dialog
@ListItem // With this annotation attached, the component will become available for selection when creating an Exadel Toolbox List
public class ListItemSample {
    @ValueMapValue // Required by Sling models
    @DialogField(label = "Text")
    @TextField
    private String text;

    @ValueMapValue // Required by Sling models
    @DialogField(label = "Show description?")
    @Switch
    @DependsOnRef
    private boolean showDescription;

    @ValueMapValue // Required by Sling models
    @DialogField(label = "Description")
    @TextField
    // The underlying field won't be visible unless the Switch is turned on
    @DependsOn(query = "@showDescription")
    // The underlying field will be disabled (not added to a POST request) unless the Switch is turned on
    @DependsOn(query = "!@showDescription", action = DependsOnActions.DISABLED)
    private String description;
}

/* ...Elsewhere in the code */

public class ListConsumer {
    private List<ListItemSample> myList = ListHelper.getList(resourceResolver, "/content/path/to/list", ListItemSample.class);
}
```

#### 6. Touch UI dialog for an AEM page (page properties dialog)

- <u>In the package module</u>: you need the page folder containing page rendering logic
- <u>In the bundle module</u>:

```java
@AemComponent(
        path = "../pages/my-page", // "path" supports traversing parent and sibling nodes
                                   // which is useful if your *componentsPathBase* points to
                                   // exactly AEM components, and you need to reach pages folder
        resourceSuperType = "my/components/pages/page",
        title = "Page properties"
)
@Dialog
@Tabs(@Tab(title = "Basic"))
public class DummyComponent {

    @Include( // It is a common practice to include a tab resource in Page properties, rather
              // than adding particular dialog components
        path = "my/components/pages/my-page/tabs/tabMetadata"
    )
    @Place("Basic")
    private String basicTabHolder;
}
```


Many more code snippets are available in the [Component management](#component-management) section and in the _samples_ project.

## Features reference

#### Component management

- [Component structure](docs/md/component-structure.md)
- [Laying out your dialog](docs/md/dialog-layout.md)
- [Defining dialog fields, setting attributes](docs/md/widget-annotations.md)
    - [Configuring RichTextEditor](docs/md/configuring-rte.md)
    - [Grouping fields with FieldSet](docs/md/configuring-fieldset.md)
    - [Multiplying Fields](docs/md/multiplying-fields.md)
- [Reusing code and making it brief](docs/md/reusing-code.md)
- [Customizing the ToolKit to your needs](docs/md/customizing-toolkit.md)
- [Additional properties of components, dialogs and fields](docs/md/additional-properties.md)

#### Enhanced authoring experience

- [Programming dynamic dialog behavior: DependsOn plugin client library](docs/md/depends-on.md)
- [Managing structured data with Exadel Toolbox Lists](docs/md/etoolbox-lists.md)
- [Feeding data to selection widgets with OptionProvider](docs/md/option-provider.md)

#### Need more documentation?

See the complete API and core module documentation (Javadoc) [here](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/index.html).

## Working with sample code

Examples of how to use the ToolKit and the DependsOn client library are presented in the [Samples](samples) module.

Run `mvn clean install -P install-samples` from the root folder of [Samples](samples) to install the sample project.



