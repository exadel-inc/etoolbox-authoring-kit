<!--
layout: content
title: Reusing code and making it brief
navTitle: Reusing Code
seoTitle: Reusing Code - Exadel Authoring Kit
order: 5
-->

## Members inheritance and how to cancel it

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
            // The "value" parameter is mandatory while "source" can be skipped.
            // In this case, the current class is implied.
            // You can also assign the reserved value _Super.class to "source".
            // Thus, the field/method from the superclass will be ignored
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, value= "field1"),
            @ClassMember("field2")
    })
    public static class ComponentDialog extends MultipleFieldsDialog {/* ... */}
```

This setting works similarly for dialog classes and `FieldSets` and `Multifields`. It is also possible to control secondary containers (*FieldSets* and *Multifields*) in a more flexible manner.

If we add an `@Ignore(@ClassMember(...))` instruction with its *source* pointing to a FieldSet or Multifield class in the current dialog, the field will be ignored in all the fieldsets/multifields of the given type that are declared within the dialog.

But if we need to ignore a field in one particular FieldSet of Multifield within the dialog, we may add `@Ignore(members = @ClassMember(...))` to that same field below the `@FieldSet` or `@Multifield` annotation, accordingly. The *source* parameter is naturally skipped in this case. The setting will take effect for the field and will not affect others.

The `@Ignore` setting is *not* inherited, unlike fields themselves, and works only for the class where it was specified.

## Sections inheritance

With the ToolKit, dialog sections (tabs or accordion panels) declared in a superclass are also "inherited" by the descendant class, and the layout instructions (`@Place`) are in effect even if referring to a superclass container.

Surely, there's a possibility to ignore an "inherited" section as well. Add the `@Ignore` annotation to the current class or `@Tabs`/`@Accordion`/`@FixedColumns`-annotated class member and fill in its *sections* property. It accepts a single string equal to the title of the ignored section, or an array of strings.

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

## Replacing members

Apart from ignoring class members, there is an option to replace an (ancestral) field or method with another one. This may be used to virtually "override" a field from superclass preserving the same field name (but with no duplicating this time). Take a look at the following sample:
```
    @DialogField
    @TextField
    @Replace(@ClassMember(source = _Super.class, value = "supertext"))
    private String text;
```
This way, the *text* field in the superclass will be removed from the rendering workflow, but the *text* field from the current class will remain. Moreover, the latter be placed exactly where the overridden field would reside unless another *ranking* value is set.

If you omit the *source* part from `@Replace`, the current class will be assumed. Otherwise, if you omit the *value*, the same-named field or method from the specified source class will be assumed.

## @Extends-ing fields annotations

Several dialog fields, such as the RichTextEditor field, may require vast and sophisticated annotation code. If there are multiple such fields in your Java files, they may become overgrown and difficult to maintain. You will also probably face the need to copy the lengthy annotation listings between fields (e.g. if you plan to use several RTE boxes with virtually the same set of toolbar buttons, plugins, etc.).

One of the powerful features of the ToolKit is its extension/inheritance technique that helps to cope with that issue.

Suppose that you have marked a `private String sampleText;` in your *HelloWorld.java* with several ToolKit annotations and wish to use the same set of annotations for `private String anotherField;` in this or another class.

To achieve this, add the `@Extends` annotation (the one that contains a pointer to *sampleText*) to *anotherText* field. Whatever widget annotation you defined for the *sampleText* field will now be "inherited" by *anotherText*.
<br>You can still add another `@TextField` to *anotherText* with properties that were not specified in *sampleText* or have different values there. Thereby "inheritance with overriding" is achieved. See the following snippet:
```java
public class CustomPropertiesDialog {
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

## Scripting widget properties' values

Imagine that you have a fieldset `ButtonFieldSet` that comprises settings for a button such as "label", "hyperlink", "does it open in a new window?" and so on. Naturally, this fieldset contains a number of annotated fields, such as `@TextField`, `@Checkbox`, etc., and is reused across several components.

Now suppose that the requirement is such that in some of your components your "hyperlink" field must have the default value _https://google.com_ and in some other components it should be _https://bing.com_. Also, in some cases the "open in a new window" checkbox should be checked by default, and in some other cases it should be unchecked.

In this situation you would like to use some _variable_ (or. in other words, _scripted_) values with your reusable fieldset So that in `MyComponentA` you use the the fieldset with "default hyperlink" set to Google, and in `MyComponentB` you use the same fieldset with "default hyperlink" set to Bing.

Here is a sample of how you can achieve this with the ToolKit:

```java
public class MyComponentA {
    @FieldSet
    @Scripted({
            @Setting(name = "defaultHyperlink", value = "https://google.com"),
            @Setting(name = "openInNewWindow", value = "{Boolean}true")
    })
    private ButtonFieldSet buttonFieldSet;
}
```

```java
public class MyComponentB {
    @FieldSet
    @Scripted({
            @Setting(name = "defaultHyperlink", value = "https://bing.com"),
            @Setting(name = "openInNewWindow", value = "{Boolean}false")
    })
    private ButtonFieldSet buttonFieldSet;
}
```

```java
public class ButtonFieldSet {
    @DialogField
    @TextField
    private String label;

    @DialogField
    @TextField(value = "${ @defaultHyperlink }")
    private String hyperlink;

    @DialogField
    @Checkbox(value = "${ @openInNewWindow }")
    private boolean openInNewWindow;
}
```

The overall idea is that you declare a "named variable" with the `@Setting` annotation and then you "insert" it elsewhere into a String-typed property of another annotation using the string template like `${@variableName}` (the format `@{ @variableName }` is also supported).

The string template does not have to be the only content of a property value. You can combine it with other text, like `value = "https://www.google.com/${@defaultPath}"`.

Every kind of property can be scripted except for properties of the `@Setting` annotation itself.

Surely, you cannot pass a string template to a boolean-typed or a string-typed property. There is however a workaround. If you need to turn a non-string value into a variable, pass it via an additional `@Property` annotation like in the following example:

```java
public class MyComponent {
    @FieldSet
    @Setting(name = "minValue", value = "{Double}0.0")
    @Setting(name = "maxValue", value = "{Double}100.0")
    private MyFieldset myFieldset;
}

// ...
public class MyFieldset {
    @DialogField
    @NumberField(min = 0 /* an optional un-scripted default */, max = 1 /* another optional default */)
    @Property(name = "min", value = "${@minValue}")
    @Property(name = "max", value = "${@maxValue}")
    private double value;
}

```

##### What are the places you can declare your settings in?

Basically, the ToolKit supports four sorts of settngs:
1) Settings that are attached to the same class member they are used (usually does not make much sense but can be used in some scenarios).
2) Settings that are attached to the class where the said member is declared _or to any of its superclasses_.
3) Settings that are attached to the member of another class that has the type of the class in which the settings are used. In the code this, is referred to as the "upstream member". To put it simple, if there is a field named _title_ in a class named _MyFieldset_ and there is field named _titleFieldset_ of type _MyFieldset_ in a class named _MyComponent_, then `MyComponent.titleFieldset` in the upstream member for `MyFieldset.title`. If you annotate `MyComponent.titleFieldset` with `@Setting`, this setting is respected when rendering an annotation declared at `MyFieldset.title` as the upstream setting.
4) Settings that are attached to the class where the upstream member is declared _or to any of its superclasses_.

So, as you see it, there is some "stack" of variables that can affect rendering of the current field, belonging to different "scopes". If there are variables with the same name, the values are overridden from the most "remote" scope to the most "close" one. That is, settings declared at level #4 in the list above are overridden by settings declared at level #3, then by level #2, and finally by level #1.

##### Scripting expressions syntax

Expressions within the `${}` or `@{}` brackets are not limited to just settings' names. They basically follow the (simplified) JavaScript syntax. Therefore, you can, for example, refer to a setting with a fallback value like `${@mySetting || 'default value'}` or use a ternary like `${@mySetting ? 'value if true' : 'value if false'}`. You can also use arithmetic operations, string concatenation, and so on.

Inside the expression, you can use the special `@this` object (alias `source`). `@this` refers, naturally, to the class member that is being currently rendered.

With `@this.class` you can get the declaring class of the current member. Then you can retrieve some "properties" of the class, like `@this.class.name` or `@this.class.parent`, or else a collection of `@this.class.ancestors`.

With `@this.context` (alias `@this.upstream`) you can reach the "upstream" class member (see definition above) if the expression is being used inside a fieldset.

There is the way to retrieve a property of a declared annotation with, e.g., `@this.annotation('DialogField').label`. You can also get all the declared annotations with `@this.annotations()`. Same way you can get a particular annotation or all the annotations of the declaring class with `@this.class.annotation('Dialog')` or `@this.class.annotations()[1]`. See more of it in the [test classes](https://github.com/exadel-inc/etoolbox-authoring-kit/tree/master/plugin/src/test/com/exadel/aem/toolkit/plugin/handlers/common/cases/components).

##### Property scripting or DependsOn?

From the first look on them, the interpolatable string templates discussed above appear similar to the [DependsOn](../depends-on/introduction.md) queries. You must however understand the difference between them.

Both techniques alter the view and/or behavior of a Touch UI dialog conditionally.

_DependsOn_ does this dynamically (in runtime) in the browser. In a common scenario, _DependsOn_ is used to modify the state of a dialog widget after the user interacted with another widget of the same dialog (like showing or hiding a text field upon checking or unchecking a box).

_Property scripting_ with `@Setting`s as discussed above does this statically at the time of project building. It is used, e.g., to make a text field inside a fieldset display its default value as "Foo" when used within "MyComponentA" and "Bar" when used within "MyComponentB" -- all without the need to create two different fieldsets. You cannot make a scripted template react to a user action like you would do with a _DependsOn_ query.
