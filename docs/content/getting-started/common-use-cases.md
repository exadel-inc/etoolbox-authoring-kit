<!--
layout: content
title: Common Use Cases
order: 2
-->
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


Many more code snippets are available in the [Component management](../dev-tools/component-management/component-structure.md) section.
