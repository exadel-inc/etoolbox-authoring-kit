## Component annotation
In order to create .content(?) file you need to create a Java class and mark it with `@Component` annotation. Views represents authoring nodes(?) (e.g. cq:dialog, cq:design_dialog, cq:editConfig, cq:childEditConfig, cq:htmlTag). All files will be written to the path that is specified by path property.

`@Component` possesses properties that are translated into common attributes of AEM component itself, according to the Adobe specification, thus covering most of the use-cases. See the code snippet below:
```java
@Component(
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
public class ComplexComponentHolder { /* ... */ }
```

### @Dialog annotation
In order to create a dialog you need create a Java class and mark it with `@Dialog` annotation and put this class to Component's views property. All required root attributes and namespace fields for the XML markup of cq:dialog  will be added.

```java
@Dialog(
    name = "myComponent",
    title = "My AEM Component",
    helpPath = "https://www.google.com/search?q=my+aem+component",
    width = 800,
    height = 600,
    extraClientlibs = "cq.common.wcm",
    layout = DialogLayout.TABS (?)
)
public class MyComponentDialog { /* ... */ }
```

### DesignDialog annotation
In order to create a design dialog you need create a Java class and mark it with `@DesignDialog` annotation and put this class to Component's views property. All required root attributes and namespace fields for the XML markup of cq:design_dialog  will be added.

```java
@DesignDialog(
    title = "My AEM Component",
    width = 800,
    height = 600,
    layout = DialogLayout.TABS,
    tabs = {
        @Tab(title = LABEL_TAB_1),
        @Tab(title = LABEL_TAB_2)
    }
)
public class DesignDialogView{ /* ... */ }
```

### EditConfig settings
If you wish to engage such TouchUI dialog features as listeners or in-place editing (those living in *\<cq:editConfig>* node and, accordingly, *_cq_editConfig.xml* file), add `@EditConfig` annotation to your Java class and put this class to Component's views property.

It facilitates setting of the following properties and features:

- Actions
- Empty text
- Inherit
- Dialog layout
- Drop targets
- Form parameters
- In-place editing
- Listeners

#### In-place editing configurations
To specify in-place editing configurations for your component, populate the  *inplaceEditing* property of `@EditConfig` annotation like follows.

```java
@Dialog(name = "componentName")
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
    String field1;
}
```
Note that if you use `type = EditorType.PLAINTEXT`, there is an additional required *textPropertyName* value. If you do not specify a value for that, same string as for *propertyName* will be used.

There is the possibility to create [multiple in-place editors](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/multiple-inplace-editors.html) like in the following snippet:
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
public class CustomPropertiesDialog {
    @DialogField
    @TextField
    String field1;
}
```
#### RichText configuration for the in-place editing
With an in-place configuration of `type = EditorType.TEXT`, a *richTextConfig* may be specified with syntax equivalent to that of `@RichTextEditor` component annotation.
Here is a very basic example of "richTextConfig" for an in-place editor
```
@InplaceEditingConfig (
    type = EditorType.TEXT, ...
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
class DialogSample { /* ... */ }
```
Ever simpler, you can specify the richText field to "extend" RTE configuration specified for a Touch-UI dialog elsewhere in your project:

```
@InplaceEditingConfig (
    type = EditorType.TEXT,
    richText = @Extends(value = HelloWorld.class, field = "myRteAnnotatedField"),
    richTextConfig = @RichTextEditor(/* ... */)
)
```
From the above snippet you can see that *richText* and *richTextConfig* work together fine. Configuration inherited via *richText* can be altered by whatever properties specified in *richTextConfig*. If you use both in the same `@InplaceEditingConfig`, plain values, such as strings and numbers, specified for the `@Extends`-ed field are overwritten by their correlates from *richTextConfig*. But array-typed values (such as *features*, *specialCharacters*, *formats*, etc.) are actually merged. So you can design a fairly basic set of features, styles, formats to store in a field somewhere in your project and then implement several *richTextConfig*-s with more comprehensive and different feature sets.

### ChildEditConfig settings
Apart from *cq:editConfig* itself, the Adobe Granite gives you possibility to define some in-place editing features for the children of the current component. This is done via the *cq:childEditConfig* node having generally the same structure as cq:editConfig.

You should(?) add `@ChildEditConfig` annotation to your Java class and put this class to Component's views property.

It facilitates setting of the following properties and features:

- Actions
- Drop targets
- Listeners

Usage is as follows:
```
@Dialog(name = "parentComponent")
@ChildEditConfig(
    actions = {"edit", "copymove"}
)
public class Dialog {
    @DialogField
    @TextField
    String field1;
}
```
#### Altering field's decoration tag with @HtmlTag
To create a specific [decoration tag](https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/decoration-tag.html) for your widget, you need to mark your Java class with `@HtmlTag` and put this class to Component's views property. Then the `cq:htmlTag` node will be added to your component's nodeset.
```java
@HtmlTag(
    className = "my-class",
    tagName = "span"
)
public class MyComponentDialog { /* ... */ }
```
