[Main page](../../README.md)

## Reusing code and making it brief

### Members inheritance and how to cancel it

Either dialog containers (tabs or accordion panels) or dialog fields are "inherited" across Java classes that *extend* or *implement* other classes.

By default, this is true for dialog fields bound to non-private Java class methods because they are indeed visible across the inheritance tree. But the *Toolkit* provides the same facility for Java class fields as well.

However, it is possible to "cancel" a superclass-bound field from rendering in a current dialog or a narrower container. Add an `@Ignore` annotation to the current class:
```java
    @Dialog(
            name = "component-dialog",
            title = "Dialog Title",
            layout = DialogLayout.TABS
    )
    @Ignore(members = {
            // The "value" parameter is mandatory while "source" can be skipped
            // In such case, current class is implied
            // You can also assign the reserved value _Super.class to "source"
            // Thus, the field/method from the superclass will be skipped
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, value= "field1"),
            @ClassMember("field2")
    })
    public static class ComponentDialog extends MultipleFieldsDialog {/* ... */}
```

This setting works similarly for dialog classes and `FieldSets` and `Multifields`. It is also possible to control secondary containers (*FieldSets* and *Multifields*) in an even more flexible manner.

If we add an `@Ignore(@ClassMember(...))` instruction with its *source* pointing to a FieldSet or Multifield class in the current dialog, the field will be ignored in all the fieldsets/multifields of the given type that are declared within the dialog.

But if we need to ignore a field in one particular FieldSet of Multifield within the dialog, we may add `@Ignore(members = @ClassMember(...))` to that same field below `@FieldSet` or `@Multifield` annotation accordingly. The *source* parameter is naturally skipped in this case. The setting will take effect for the field and will not affect others.

The `@Ignore` setting is *not* inherited, unlike fields themselves, and works only for the class where it was specified.

### Sections inheritance

With the ToolKit, dialog sections (tabs or accordion panels) declared in a superclass are also "inherited" by the descendant class, and the layout instructions (`@Place`) are in effect even if referring to a superclass container.

Surely, there's a possibility to ignore an "inherited" section as well. Add the `@Ignore` annotation to the current class (not a class member this time) and fill in its *sections* property. It accepts a single string equal to the title of the ignored section, or an array of strings.

```java
@Dialog(
    name = "test-component",
    title = "test-component-dialog",
    tabs = {
        @Tab(title = "Fourth tab"),
        @Tab(title = "Fifth tab")
    }
)
// In Toolkit, containers/sections such as tabs, are manipulated by their title strings
@Ignore(sections = {"First tab", "Second tab"})
public class TestTabsExtension {/* ... */}
```
Note that, again, the `@Ignore` setting is *not* inherited and works only for the class where it was specified.

### Replacing members

Apart from ignoring class members, there is an option to replace an (ancestral) field or method with another one. This may be used to virtually "override" a field from superclass preserving the same field name (but with no duplicating this time). Take a look at the following sample:
```
    @DialogField
    @TextField
    @Replace(@ClassMember(source = _Super.class, value = "supertext"))
    private String text;
```
This way, the *text* field in the superclass will be removed from the rendering workflow, but the *text* field from the current class will remain. Moreover, the latter be placed exactly where the overridden field would reside unless another *ranking* value is set.

If you omit the *source* part from `@Replace`, the current class will be assumed. Otherwise, if you omit the *value*, the same-named field or method from the specified source class will be assumed.

### @Extends-ing fields annotations

Several dialog fields, such as the RichTextEditor field, may require vast and sophisticated annotation code. If there are multiple such fields in your Java files, they may become overgrown and difficult to maintain. You will also probably face the need to copy the lengthy annotation listings between fields (e.g. if you plan to use several RTE boxes with virtually the same set of toolbar buttons, plugins, etc.).

One of the powerful features of the ToolKit is its extension/inheritance technique that helps to cope with that issue.

Suppose that you have marked a `private String sampleText;` in your *HelloWorld.java* with several ToolKit annotations and wish to use the same set of annotations for `private String anotherField;` in this or another class.

To achieve this, add the `@Extends` annotation (the one that contains a pointer to *sampleText*) to *anotherText* field. Whatever widget annotation you defined for the *sampleText* field will now be "inherited" by *anotherText*.
<br>You can still add another `@TextField` to *anotherText* with properties that were not specified in *sampleText* or have different values there. Thereby "inheritance with overriding" is achieved. See the following snippet:
```java
public class CustomPropetiesDialog {
    @DialogField(label = "My text field")
    @Extends(value = HelloWorld.class, field = "sampleText")
    @TextField(emptyText = "Enter your text here")
    private String anotherText;
    /* ... */
}
```
The ToolKit will first look for the *sampleText* field in *HelloWorld.java* and, if it is found, will use that field's `@DialogField` and `@DatePicker` annotations to prepare an XML markup for the current field. For properties such as *label* or *emptyText* that have local "overrides", the local values will be used, and the rest will be taken from the *anotherText* field.

Note: it is possible that the "parent" field in its own turn `@Extends`-es some third "grandparent" field, so rendering starts from "grandparent" (same as it is with inheriting class members in object-oriented programming).

You should still make sure that all the fields involved have the same component annotation. A field marked with, say, `@DatePicker` will not extend some `@Checkbox` field, and so on.

Also pay attention so that when you extend a field and add another field-specific annotation to override some properties (like in the sample above), property values are either replaced or appended (like adding values from an array-typed property of "child" to the array-typed property of "parent"), but not erased. You cannot replace a non-empty value of a "parent" with a blank, or empty, value of a "child." So take care to design your "inheritance tree" starting from fields with more abstract, less populated component annotations, and then shifting to more specific ones.
