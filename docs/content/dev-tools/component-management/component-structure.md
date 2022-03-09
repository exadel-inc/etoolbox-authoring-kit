<!--
layout: content
title: Component Structure
order: 1
-->
## Creating AEM Components with ToolKit annotations

[@AemComponent](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/main/AemComponent.html) is your entry point to creating component authoring interfaces, such as a *Dialog*, a _Design dialog_, or an _In-place editing config_. When added to a Java class, this annotation must contain generic properties of the component such as *title*, *description*, etc.

Additionally, `@AemComponent` can contain references to other Java classes that can be referred to as “views”. If, for instance, you need *editConfig*, you can add the `@EditConfig` to the Java class where the `@AemComponent` annotation is already present. You can also add `@EditConfig` to another Java class and put a reference to that class in the *views* collection of `@AemComponent`.

Take note that you need either to put an annotation such as `@Dialog` or `@EditConfig` in the same Java class as `@AemComponent` or add it to another class and then add the class reference to the *views* collection. You don't need to do both.

See the code snippet below, which displays all currently supported `@AemComponent` properties:

```java
@AemComponent(
    path = "content/my-component",
    title = "My AEM Component",
    description = "test component",
    componentGroup = "My Component Group",
    resourceSuperType = "resource/super/type",
    disableTargeting = true,
    views = {
        DesignDialogView.class,
        DialogView.class,
        HtmlTagView.class,
        ChildEditConfigView.class,
        EditConfig.class
    }
)
public class ComplexComponentHolder {/* ... */}
```
Pay attention to the *path* property. This can be set in two ways. First is the "relative" path that will be resolved from the point specified by the plugin's componentsPathBase setting. This is a common way. Otherwise, you can store an "absolute" path that will be resolved from the root of the package. The absolute path starts with jcr_root folder or any folder that goes immediately under jcr_root. Usually, it would be something like /apps/vendor/components/myComponent.

By default, the plugin searches for the directory specified in *path* and triggers an exception if unable to find one. This is justified because a non-existing folder usually means an invalid component path (a typo in *path*, or some misconfiguration).

However, there is a way to make the plugin create a folder in the package when missing. This can be useful for single-use components (such as Exadel Toolbox Lists' items), or components that do not need a specific HTML or JSP file. In such a case add `writeMode = WriteMode.CREATE` to your `@AemComponent`.

### @Dialog

`@Dialog` is used for defining component's Touch UI dialog by creating and populating *\<cq:dialog>* node.

```java
@Dialog(
    title = "My AEM Dialog",
    helpPath = "https://www.google.com/search?q=my+aem+component",
    width = 800,
    height = 600,
    extraClientlibs = "cq.common.wcm",
    forceIgnoreFreshness = true
)
public class MyComponentDialog {/* ... */}
```
If you specify *title* in `@Dialog`, it will override the title specified in @AemComponent. Skip this if you need to have the same title rendered for the component itself and for the dialog.

Pay attention to the *forceIgnoreFreshness* option. When set to true, it forces the entire dialog to ignore freshness of the Granite UI form beside the dialog. This will allow any component that resides within the dialog to display its default value (if specified) regardless of whether the underlying resource is being created anew (a "fresh" one) or just being edited.

Most Granite UI components don't manage default values when editing a "non-fresh" resource. Some offer their own implementation of *forceIgnoreFreshness* (most notably, the Select component). However, specifying *forceIgnoreFreshness* at the `@Dialog` level makes the rest behave in the way the Select does.


### @DesignDialog

`@DesignDialog` is used for defining component's Touch UI dialog by creating and populating *\<cq:design_dialog>* node.

```java
@DesignDialog(
    title = "My AEM Component",
    width = 800,
    height = 600
)
public class DesignDialogView {/* ... */}
```

If you specify *title* in `@DesignDialog`, it will override the title specified in `@AemComponent`. Skip this if you need to have the same title rendered for the component itself and for the design dialog.

### Dialog layouts

Both the Touch UI dialog and design dialog can have either a relative simple or a complex structure. This is why they can either have a plain "all in the same screen" display or be organized with use of nested sections, or containers.

In the first case, no specific setup is required. A dialog is automatically assigned the *"fixed column"* style.

Otherwise, a dialog can be rendered in one or more tabs, or be organized as an *accordion* with one or more panels. To achieve this, you need to put the `@Tabs` or `@Accordion` annotation respectively beside your `@Dialog`/`@DesignDialog`. See [Laying out your dialog](./dialog-layout.md) for details.

### EditConfig settings

If you wish to engage Touch UI dialog features like listeners or in-place editing (those living in *\<cq:editConfig>* node and, accordingly, *_cq_editConfig.xml* file), add an `@EditConfig` annotation to your Java class (same as above, you can as well add the annotation to a separate class and then put the reference to this class into @AemComponent's *views* property).

[@EditConfig](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/editconfig/EditConfig.html) facilitates setting of the following properties and features:

- Actions
- Empty text
- Inherit
- Dialog layout
- Drop targets
- Form parameters
- In-place editing
- Listeners

Here is a basic sample of an `@EditConfig` with several of the parameters specified:
```java
@EditConfig(
        emptyText = "Input here",
        listeners = {
            @Listener(
                event = ListenerConstants.EVENT_AFTER_INSERT,
                action = ListenerConstants.ACTION_REFRESH_PAGE
            ),
            @Listener(
                event = ListenerConstants.EVENT_AFTER_DELETE,
                action = ListenerConstants.ACTION_REFRESH_PAGE
            )
        },
        dropTargets = {
                @DropTargetConfig(
                        nodeName = "logo",
                        accept = "image/.*",
                        groups = "media",
                        propertyName = "logo/fileReference"
                ),
                @DropTargetConfig(
                        nodeName = "background",
                        accept = "image/.*",
                        groups = "media",
                        propertyName = "background/fileReference"
                )
        },
        formParameters = {
                @FormParameter(name = "param1", value = "value1"),
                @FormParameter(name = "param2", value = "value2")
        }
)
public class ImageEditor {/* ... */}
```
#### In-place editing configurations

To specify in-place editing configurations for your component, populate the *inplaceEditing* property of `@EditConfig` annotation as follows:

```java
@EditConfig(
    inplaceEditing = @InplaceEditingConfig(
        type = EditorType.TEXT,
        editElementQuery = ".editable-header",
        name = "header",
        propertyName = "header"
    )
)
public class CustomPropertiesDialog {
    @DialogField
    @TextField
    String header;
}
```

Note that if you use `type = EditorType.PLAINTEXT`, there is an additional required *textPropertyName* value. If you do not specify a value, the same *propertyName* string will be used.

It’s also possible to create [multiple in-place editors](https://experienceleague.adobe.com/docs/experience-manager-65/developing/components/multiple-inplace-editors.html?lang=en) as in the following snippet

```java
@Dialog(name = "componentName")
@EditConfig(
    inplaceEditing = {
        @InplaceEditingConfig(
            type = EditorType.PLAINTEXT,
            editElementQuery = ".editable-headline",
            name = "headline",
            propertyName = "headline"
        ),
        @InplaceEditingConfig(
            type = "CustomType",
            editElementQuery = ".editable-description",
            name = "description",
            propertyName = "description"
        )
    }
)
public class CustomPropertiesDialog {/* ... */}
```

#### RichText configuration for in-place editing

With an in-place configuration of `type = EditorType.TEXT`, a *richTextConfig* can be specified with syntax equivalent to that of a [@RichTextEditor annotation](./dialog-fields/configuring-rte.md).
Here is a very basic example of a *richTextConfig* for an in-place editor

```java
@InplaceEditingConfig (
    type = EditorType.TEXT,
    richTextConfig = @RichTextEditor(
        features = {
            RteFeatures.UNDO_UNDO,
            RteFeatures.UNDO_REDO,
            RteFeatures.Popovers.MISCTOOLS,
            RteFeatures.Panels.TABLE,
            RteFeatures.FULLSCREEN_TOGGLE
        },
        icons = @IconMapping(command = "#misctools", icon = "fileCode"),
        htmlPasteRules = @HtmlPasteRules(
            allowBold = false,
            allowImages = false,
            allowLists = AllowElement.REPLACE_WITH_PARAGRAPHS,
            allowTables = AllowElement.REPLACE_WITH_PARAGRAPHS
        )
    )
)
class DialogSample {/* ... */}
```

Even more simply, you can specify the *richText* field to "extend" RTE configuration for a Touch UI dialog elsewhere in your project:

```
@InplaceEditingConfig (
    type = EditorType.TEXT,
    richText = @Extends(value = HelloWorld.class, field = "myRteAnnotatedField"),
    richTextConfig = @RichTextEditor(/* ... */)
)
```

From the above snippet, you can see that *richText* and *richTextConfig* work together fine. A configuration inherited via *richText* can be altered by whatever properties that are specified in *richTextConfig*.

If you use both in the same `@InplaceEditingConfig`, plain values, such as strings and numbers, specified for the `@Extends`-ed field are overwritten by their correlates from *richTextConfig*.

On the other hand, array-typed values (such as *features*, *specialCharacters*, *formats*, etc.) are actually merged. So you can design a fairly basic set of features, styles, and formats to store in a field somewhere in your project and then implement several *richTextConfig*-s with more comprehensive feature sets.

### ChildEditConfig settings

In addition to *\<cq:editConfig>* itself, Adobe Granite makes it possible to define some in-place editing features for the current component’s children. This is done via the *\<cq:childEditConfig>* with the same general structure as *\<cq:editConfig>*.

It facilitates the setting of the following properties and features:

- Actions
- Drop targets
- Listeners

Usage:

```java
@ChildEditConfig(
    actions = {"edit", "copymove"}
)
public class Dialog {
    @DialogField
    @TextField
    String field1;
}
```

### Altering a decoration tag for a field with @HtmlTag

To create a specific [decoration tag](https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/decoration-tag.html) for your widget, you need to mark your Java class with `@HtmlTag` and put this class to the Component's views property. Then the *\<cq:htmlTag>* node will be added to your component's nodeset.

```java
@HtmlTag(
    className = "my-class",
    tagName = "span"
)
public class MyComponentDialog {/* ... */}
```
## Page properties dialogs

You can use an approach similar to the one described above for creating a page properties dialog.

Specify an absolute path to the JCR node that refers to the page in your `@AemComponent` like `@AemComponent(path = "/apps/my/components/pages/page"}`, or else provide a relative path. Even if your *componentsPathBase* points to an AEM components' root, you can traverse up and to the "pages" sibling node like this: `@AemComponent(path = "../pages/page"}`.

Page properties dialogs are most often composed of tabs. It is a common practice for page properties dialogs to include an existing tab resource instead of enumerating components one by one. This is because tabs are often reused and overlayed across the page hierarchy. You can include an existing tab as follows:

```java
@AemComponent(
        path = "../pages/my-page",
        resourceSuperType = "my/components/pages/page",
        title = "Page properties"
)
@Dialog
@Tabs(@Tab(title = "Basic"))
public class DummyComponent {

    @Include(path = "my/components/pages/my-page/tabs/tabMetadata")
    @Place("Basic")
    private String basicTabHolder;
}
```
Else, you can add components to the tab as usual. Note that usually in a Page properties dialog tab components are wrapped in an additional column so that they get centered on the screen and do not span the whole of the screen width. This can be achieved through a nested column.

```java
@AemComponent(
        path = "../pages/my-page",
        resourceSuperType = "my/components/pages/page",
        title = "Page properties"
)
@Dialog
@Tabs(@Tab(title = "Basic"))
public class DummyComponent {

    // The title is not actually rendered, so you may specify any meaningful string
    @FixedColumns(@Column(title = "Tab column"))
    @Place("Basic")
    private int tabHolder; // Type of the field does not matter as well

    @DialogField(label = "Enter text")
    @TextField
    @Place("Tab column")
    private String text;

    @DialogField(label = "Enter description")
    @TextField
    @Place("Tab column")
    private String description;

}
```
Else you can use a nested class annotated with `FixedColumns` for that purpose.

***
#### See also

- [Laying out your dialog](./dialog-layout.md)
- [Configuring RichTextEditor](./dialog-fields/configuring-rte.md)
- [@Extends-ing fields annotations](./reusing-code.md#extends-ing-fields-annotations)
