[Main page](../../README.md)

## Additional properties of components, dialogs and fields

### Attributes of fields
Components Touch UI dialogs honor the concept of [global HTML attributes](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/docs/server/commonattrs.html) added to rendered HTML tags. To set them via AEM-Dialog-Plugin, you use the @Attribute annotation.
```java
public class DialogWithHtmlAttributes {
    @DialogField
    @TextField
    @Attribute(
        id = "field1-id",
        className = "field1-attribute-class",
        data = {
            @Data(name = "field1-data1", value = "value-data1"),
            @Data(name = "field1-data2", value = "value-data2")
    })
    String field1;
}
```

### Additional properties

You can define an additional property for a Granite UI widget or overwrite an existing one with the `@Property` annotation. This will be rendered into an appropriate subnode under the component's *\<cq:dialog>* or *\<cq:design_dialog>* node and will affect the behavior of a dialog widget.

A `@Property` has its *name* and *value* attributes. The non-blank *name* can be the name of the property to write into, or else can contain a relative path. The relative path can be defined in such a way that the substring before the ultimate `/` represents the path, and the substring after the ultimate `/` represents the property name.

```java
public class MyComponent {
    @DialogField
    @TextField
    @Property(name = "simpleProperty", value = "value")
    @Property(name = "../parentNodeProperty", value = "value")
    @Property(name = "../siblingNode/property", value = "value")
    String field;
}
```

### Common (component-wide) properties

Yet another mechanism available is to specify custom properties at Java class level. This can be used:

- for setting entire component's attributes (those exposed in *.content.xml* file);
- for setting attributes of *\<cq:dialog>* root node (*_cq_dialog.xml* file);
- for setting attributes of *\<cq:editConfig>* root node (*_cq_editConfig.xml* file).

For these goals the `@CommonProperties` annotation is designed. It accepts similar arguments to those of `@Properties` annotation. Yet you can also specify the XML scope for each `@CommonProperty` (this exactly means: in which of the XML trees, or files, the attribute will be stored, default is *.content.xml*) and a relative path to the root node. See the code snippet:

```java
@CommonProperties({
    @CommonProperty(name = "stringAttr", value = "Hello World"), // goes to .content.xml by default
    @CommonProperty(scope = Scopes.CQ_DIALOG, name = "numericAttribute", value = "{Long}-1000"),
    @CommonProperty(scope = Scopes.CQ_EDIT_CONFIG, name = "arrayAttribute", value = "[any,many,minny,moe]"),
    @CommonProperty(
        scope = XmlScope.CQ_EDIT_CONFIG,
        path = "/root/inplaceEditing/config/rtePlugins/edit/htmlPasteRules/table",
        name = "allow",
        value = "{Boolean}true"
    ),
    @CommonProperty(
        scope = XmlScope.CQ_DIALOG,
        path = "//*[@size='L']",
        name = "size",
        value = "S"
    )
})
public class CustomPropertiesDialog {/* ... */}
```

Pay attention to the third and forth `@CommonProperty`-s. Specifying the *path* value gives the ability to traverse to any child node of the prepared XML with use of an XPath-formatted string.

`@CommonProperties` are rendered after the XML tree is completed. Thus, setting them provides a kind of "last-chance" alternation of your Touch UI logic (can also be used for debugging). For example, the last `@CommonProperty` in the sample uses the power of XPath to change *size* attribute of every single node where size has been set to *"L"*. Only make sure that the *path* points to at least one truly existing XML node.

Note that XPath parser is namespace-agnostic. That is why you need to use */root/inplaceEditing...* instead of */jcr:root/cq:inplaceEditing...* in the sample above.

