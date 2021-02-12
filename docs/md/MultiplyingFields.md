## Multiplying Fields
### MultiField
@MultiField  (Granite name: Multifield, resource type: /libs/granite/ui/components/coral/foundation/form/multifield)

Used to facilitate multiple (repeating) instances of same fields or same groups if fields as described in [Adobe's Granite UI manual on MultiField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html). The logic of the component relies on the presence of a nested class encapsulating one or more fields to be repeated. Reference to that class is passed to `@MultiField`'s *field* property. Same as for `@FieldSet`, if you omit this value, it is guessed from the underlying field type, be it a *SomePlainType* or a *Collection\<WithTypeParameter>*.

See below how it works for a single field repetition, and for a subset of fields multiplied.
#### Simple multi field
```java

public class SimpleMultiFieldDialog {
    @DialogField(label="Multi")
    @MultiField(field = MultiFieldContainer.class)
    String multiField;

    static class MultiFieldContainer {
        @DialogField
        @TextField
        String dialogItem;
    }
}
```
#### Composite multi field
```java

public class CompositeMultiFieldDialog {
    @DialogField
    @MultiField(field = MultiCompositeField.class)
    String multiComposite;

    private static class MultiCompositeField {
        @DialogField
        @TextField(description = "Multi Text")
        String multiText;

        @DialogField(description = "Multi Checkbox")
        @Checkbox(text = "Multi CheckBox")
        String checkboxMulti;
    }
}
```
Note that the inheritance of class(-es) encapsulating multifield items works here the same way as for the `@FieldSet`.

### Multiple
@Multiple

The easiest way to create a *Multifield* is with the `@Multiple` annotation. Just add it to the Java class field where a widget annotation is already present. A *simple multifield* containing this particular widget will be created on the fly.

If you, on the other hand, add `@Multiple` to a field marked with `@Fieldset`, a *composite multifield* will be created (much like the one you could have adding `@Multifield` annotation itself). Moreover, you can add `@Multiple` to a mere `@Multifield`-marked field and enjoy a sort of "multifield of multifields".

Please note, however, that `@Multiple` is primarily designed for easy, "quick give me a multifield out of my single widget without creating a nested class" cases. For more complicated cases, it lacks tweaking capablities that `@Multifield` itself presents.

#### Common attributes of fields
Components TouchUI dialogs honor the concept of [global HTML attributes](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/docs/server/commonattrs.html) added to rendered HTML tags. To set them via AEM-Dialog-Plugin, you use the @Attribute annotation.
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
