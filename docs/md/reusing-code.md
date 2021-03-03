[Main page](../../README.md)
## Members inheritance and ways to cancel it

Same as dialog tabs, dialog fields are "inherited" across the Java classes. This is true for dialog fields bound to Java class methods. The *Toolkit* provides same facility for Java class fields as well. However, there is the possibility to "cancel" a superclass-bound field from rendering in a current dialog or a narrower container. Add `@Ignore` annotation to the current class:
```java
    @Dialog(
            name = "component-dialog",
            title = "Dialog Title",
            layout = DialogLayout.TABS
    )
    @Ignore({
            // The "name" parameter is mandatory while "source" may be skipped
            // In such case, current class is implied
            // You can also assign the reserved value _Super.class to "source"
            // Thus, the field from superclass will be skipped from rendering
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field1"),
            @ClassMember(name = "field2")
    })
    public static class ComponentDialog extends MultipleFieldsDialog { /* ... */ }
```
This setting works similarly for dialog classes, and also _FieldSets_ and _Multifields_. Yet there is the possibility to control secondary containers (FieldSets and Multifields) in an even more flexible manner.

If we add an `@Ignore(@ClassMember(...))` instruction with its _source_ pointing to a FieldSet or Multifield class in the current dialog, the field will be ignored in all fieldsets / multifields of the given type that are declared within the dialog.

But if we need to ignore a field in one particular FieldSet of Multifield within the dialog, we may add `@Ignore(@ClassMember(...))` to that very field, below `@FieldSet` or `@Multifield` annotation accordingly. The _source_ parameter is naturally skipped in this case. The setting will take effect for the field and will not affect others.

Same as for tabs ignoring, the `@Ignore` setting is *not* inherited, unlike fields themselves, and works only for the class where it was specified.

## Replacing members

Apart from ignoring fields, there is an option to replace an (ancestral) field with another one. This may be used to virtually "override" a field from superclass preserving the same field name (but with no duplicating this time). Take a look at the following sample:
```
    @DialogField
    @TextField
    @Replace(@ClassMember(source = _Super.class, name = "supertext"))
    private String text;
```
This way, the "text" field in the superclass will be removed from the rendering workflow, but the "text" field from the current class will remain. Moreover, the latter be placed exactly where the overridden field would reside unless another _ranking_ value is set.

If you omit the _source_ part from `@Replace`, the current class will be supposed. Otherwise, if you omit the _name_, the same-named field from the specified source class will be supposed.

## @Extends-ing fields annotations
Several dialog fields, such as RichTextEditor field, may require vast and sophisticated annotation code. If there are multiple such fields in your Java files, they may become overgrown and difficult to maintain. Moreover, you will probably face the need to copy the lengthy annotation listings between fields, e.g. if you plan to use several RTE boxes with virtually the same set of toolbar buttons, plugins, etc.

One of the powerful features of **AEM Authoring Toolkit** is its extension/inheritance technique that helps to cope with that issue.

Suppose that you have marked private String sampleText; in your HelloWorld.java class with several AEM Authoring Toolkit annotations and wish to use the same set of  annotations for private String anotherField; in this very or other class.

To achieve this, add to the *anotherText* field the `@Extends` annotation pointing to *sampleText*. Whatever field-specific annotation you defined for the *sampleText* field will now be "inherited" by *anotherText*. You still can add another `@TextField` to *anotherText* with properties that were not specified in *sampleText* field or have different values there. Thereby "inheritance with overriding" is achieved. See the following snippet:
```
public class CustomPropetiesDialog {
    @DialogField(label = "My text field")
    @Extends(value = HelloWorld.class, field = "sampleText")
    @TextField(emptyText = "Enter your text here")
    private String anotherText;
    /* ... */
}
```
The plugin will first look for the *sampleText* field in *HelloWorld* class, and if found, will use that field's `@DialogField` and `@DatePicker` annotations to prepare XML markup for the current field. For such properties as *label* or *emptyText* that have local "overrides", the local values will be used, rest will be taken from the *anotherText* field.

Note that it is possible that the "parent" field in its own turn `@Extends`-es some third "grandparent" field, so rendering starts from "grandparent" (same as it is with inheriting class members in object-oriented programming).

Yet make sure that all the fields involved have the same component annotation. A field marked with, say, `@DatePicker` will not extend some `@Checkbox` field, and so on.

Also mind that when you extend a field and add another field-specific annotation to override some properties (like in the sample above), property values are either replaced or appended (like adding values from an array-typed property of "child" to the array-typed property of "parent"), but not erased. You cannot replace a non-empty value of a "parent" with a blank, or empty, value of a "child". So take care to design you "inheritance tree" starting from fields with more abstract, less populated component annotations, and then shifting to more specific ones.
