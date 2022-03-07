<!--
layout: md-content
title: Dialog Field
order: 1
-->
## Defining dialog fields

Dialog fields are the Granite UI / Touch UI entities usually built around an HTTP web form element (`<input>`). Granite
UI provides a vast scope of dialog elements, such as text fields, RTEs, date pickers, path pickers, etc.

<small>All of these are generally referred to as "widgets" in the document below, although the Granite UI documentation
would most of the time name them "components." Our special naming is introduced not to mix up the components as "
building bricks" of a dialog with "true" AEM components that represent a Java backend _plus_ the package folder.</small>

The ToolKit makes use of `@DialogField` annotation and the set of specific annotations, such as `@TextField`, `@Checkbox`,
`@DatePicker`, etc., as discussed further below. They can be applied to either a class field or a
method (methods of both class and interface are supported).

### DialogField

* [@DialogField](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/DialogField.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/field
* See
  spec: [Field](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/field/index.html)

Used for defining common properties of a dialog field, such as *name* attribute (says under which name the value will be
persisted, equals to the class' field name if not specified), *label*, *description*, *required*, *disabled*, *
wrapperClass*, and *renderHidden*. In addition, `@DialogField` makes it possible to order fields inside a container by
specifying a *ranking* value.

Typically, `@DialogField` is used in a pair with a widget annotation (e.g. `@TextField`).

```java

@Dialog
public class Dialog {
    @DialogField(
        label = "Field 1",
        description = "This is the first field",
        wrapperClass = "my-class",
        renderHidden = true,
        ranking = 5,
        validation = "foundation.jcr.name" // may as well accept an array of strings
    )
    @TextField
    String field1;
}
```

Please note: if `@DialogField` is specified but a widget annotation is not, the field will not be rendered. That's
because `@DialogField` exposes only the common information and does not specify which HTML component to use.

The other way around, you can specify a widget annotation and omit the `@DialogField`. A field like this will be
rendered (without *label* and *description*, etc.), but its value will not be persisted. This usage may be handy if you
need a merely "temporary" or "service" field.

In cases when the dialog class extends another class having some fields marked with widget annotations, relevant fields
from both the superclass and child class are rendered. Members from the superclass and child class (even those sharing
the same name) are considered different and rendered separately.

Still, namesake fields may interfere if rendered within the same container (dialog or tab). Therefore, avoid “field name
collisions” between a superclass and a child class where possible. Even so, if you wish to do some deliberate "field
overriding," refer to the [chapter](reusing-code.md) speaking about the use of `@Extends`, `@Replace`, and `@Ignore`.

Unless manually aligned with `@Place` annotation, the fields are sorted in order of their *ranking*. If several members
have the same (or default) *ranking*, they are rendered in the order of their appearance in the source code. Class
fields appear before class methods. Members collected from ancestral classes have precedence over those from child
classes.

There are specific recommendations concerning fields' and methods' ordering. See
the [Ordering widgets](#ordering-widgets) section below.

## Setting widgets' common attributes

Components of Touch UI dialogs honor the concept
of [global HTML attributes](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/docs/server/commonattrs.html)
added to rendered HTML tags. To set them via the ToolKit, you use the @Attribute annotation.

Also, you can assign additional properties of Granite UI components or re-write existing ones, even change, for
instance, the *sling:resourceType* of a widget, with use of `@Property` annotation.

Read more on this in the [Additional properties](additional-properties.md) chapter.

## Placing widgets

In a plain Granite UI dialog, widgets are situated one under another, in the order of the corresponding Java class
members. Note: as of ToolKit 2 the order is guaranteed for widgets based on Java fields, but may occasionally change for
widgets based on Java methods. To guarantee the order of widgets based on Java class methods
use `@Place(before=.../after=...)` as described below.

If there's a more robust layout, such as a tabbed or accordion-shaped dialog, you would want to distribute widgets
between sections (tabs or panels, accordingly). To do that, you specify `@Place("Section title")` next to your widget
annotation.

There are in-dialog container widgets as well. E.g., you may want to place `@Accordion` within a tab of the tabbed
layout.

There are several ways to distribute widgets in such an in-dialog container. First is to declare a nested class, declare
your widgets attached to the fields of this class, and then introduce a field or method with the return type that
matches the nested class:

```
    @Tabs({
            @Tab(title = "First"),
            @Tab(title = "Second")
        })
    private TabsFieldset tabsHolder;

    private static class TabsFieldset {
        @TextField
        @Place("First")
        private String field1;

        @TextField
        @Place("Second")
        private String field2;
    }

```

Note that the `TabsFieldset` can be extending another class. Its widget set is subject to
inheriting [in the usual way](reusing-code.md).

The same result can be achieved by just referring to section titles of an in-dialog container from "outside":

```
    @TextField
    @Place("First")
    private String field1;

    @Tabs({
            @Tab(title = "First"),
            @Tab(title = "Second")
        })
    private TabsFieldset tabsHolder;

    @TextField
    @Place("Second")
    private String field2;
```

You can combine both ways. If you have a nested class and some "outside" members claiming a place in the same section,
the members from the nested class will come first.

You can easily fancy the situation when an in-dialog container lays within a dialog layout section (e.g., there's a
container declared by `@Tabs` within a tab of the tabbed layout). In this situation, the value of `@Place("...")` is
resolved to the title of either the layout tab or the in-dialog container tab, whatever is the first match. That is why
you would want to give unique titles to all of your containers.

However, there is a way to specify the precise "path" to the container. Type it as a slash-delimited "pseudo-path by
titles", e.g., `@Place("My layout section title/My in-dialog section title")`. Any number of "nested" titles is
supported.

## Ordering widgets

Widgets created upon class fields are placed in dialogs in the same order as they appear in the source class. If a class
extends another class that contains more widgets, the "ancestral" ones are placed before the "child" ones.

If there are widgets built upon both class fields and methods, the field-based ones come first.

This behavior can be altered in two ways. First is the usage or *ranking* property of `@DialogField`. The ranking is an
integer value, no matter negative or positive. Fields with smaller numbers come first. Ranking values persist across the
superclass - child class relation and can be used to "insert" members from a subclass in between fields of a superclass.

In some respects using *rankings* is not quite convenient. That is why there is another mechanism. You can
attach `@Place(before = @ClassMember("anotherFieldName"))`
or `@Place(after = @ClassMember(source = Another.class, value "anotherFieldName"))` to the field or method you want to
be precisely placed.

The *before* and *after* parameters accept a `@ClassMember` argument. In its turn, the `@ClassMember` can contain the
name of a relative field/method and optionally a reference to a class. When no class reference is specified, the current
one is assumed.

In the following sample you see the way to move a method from a subclass before field from a superclass and then the
field from the same subclass before the former:

```java
public class MyComponentAncestor {
    @DialogField
    @TextArea
    private String description;
}

public class MyComponent extends MyComponentAncestor {
    @DialogField
    @TextField
    @Place(before = @ClassMember(source = MyComponentAncestor.class, value = "text"))
    private String getName() {/* ... */}

    @DialogField
    @TextField
    @Place(before = @ClassMember("getName")) // The class reference is not specified: therefore, the current class
    private Strning namePrefix;
}
```

The resulting widget sequence will be: the text field for *namePrefix*, the text field for *name* (as in "getName()"),
and then the text area for *description*.

<u>Important notice</u>: it is recommended that you always use `@Place(before/after)` or *ranking* with
<u>method-based widgets</u> because it is not guaranteed that the JVM reports methods in the same order (unlike class
fields).

***

#### See also

- [Configuring RichTextEditor](configuring-rte.md)
- [Laying out your dialogs](dialog-layout.md)
- [Grouping fields with FieldSet](configuring-fieldset.md)
- [Multiplying fields](configuring-multifield.md)
- [Additional properties of components, dialogs and fields](additional-properties.md)
- [Programming dynamic dialog behavior: DependsOn plugin client library](depends-on.md)
- [Feeding data to selection widgets with OptionProvider](../../option-provider.md)
