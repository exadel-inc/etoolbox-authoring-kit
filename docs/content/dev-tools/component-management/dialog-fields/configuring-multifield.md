<!--
layout: content
title: Multiplying fields
navTitle: Configuring Multifield
seoTitle: Configuring Multifield - Exadel Authoring Kit
order: 5
-->

## MultiField

* [@MultiField](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/MultiField.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/multifield
* See spec: [Multifield](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html)

Multifields are used to facilitate multiple (repeating) instances of same fields or groups of fields. The logic of this component relies on the presence of a nested class encapsulating one or more fields to be repeated.

Reference to that class is passed to `@MultiField`'s *value* property. Just as for `@FieldSet`, if you omit this value, it is guessed from the underlying field type, be it a *SomePlainType* or a *Collection\<WithTypeParameter>*.

Multifields allow you to specify the `deleteHint` (true/false) or `typeHint` values that will produce HTTP request parameters in line with Apache Sling specification for [@Delete](https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#delete) and [@TypeHint](https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#typehint).

AEM multifields exist in two flavors. *Simple* multifields usually contain only one authorable field per item. If a user creates many items, the values are stored as an array. *Composite* multifields store their values in subnodes of the current resource's node. This is mainly useful when there are several authorable fields per item.

By default the ToolKit renders a simple multifield when there is only one authorable field to manage in the reflected class, and creates a composite multifield if there are more. However, you can create a composite multifield for a class with only one authorable field by specifying `forceComposite = true`.

### Simple multifield

```java
public class SimpleMultiFieldDialog {
    @DialogField(label = "Multi")
    @MultiField(
        deleteHint = false, // Optional. Use if you don't want the value(-s) deleted in on the server when not set in dialog
        typeHint = "String[]" // Optional. If specified, will become the Sling @TypeHint parameter
    )
    List<MultiFieldContainer> containers;

    static class MultiFieldContainer {
        @DialogField
        @TextField
        String dialogItem;
    }
}
```

### Composite multifields

```java
public class CompositeMultiFieldDialog {
    @DialogField
    @MultiField(MultiCompositeField.class)
    String multiComposite;

    private static class MultiCompositeField {
        @DialogField
        @TextField
        String multiText;

        @DialogField
        @Checkbox(text = "Multi CheckBox")
        String checkboxMulti;
    }
}
```

```java
public class CompositeMultiFieldDialog2 {
    @DialogField(label = "Multi")
    @MultiField(forceComposite = true)
    List<MultiFieldContainer> containers;

    static class MultiFieldContainer {
        @DialogField
        @TextField
        String dialogItem;
    }
}
```

Note that the inheritance of class(-es) encapsulating multifield items works here the same way as for the `@FieldSet`.

## @Multiple

The easiest way to create a *MultiField* is with the `@Multiple` annotation. Just add it to the Java class field where a widget annotation is already present. A *simple* multifield containing this particular widget will be created on the fly.

On the other hand, if you add `@Multiple` to a field marked with `@FieldSet`, a *composite* multifield will be created (much like the one you could have adding `@MultiField` annotation itself). Moreover, you can add `@Multiple` to a mere `@MultiField`-marked field and enjoy a sort of "multifield of multifields."

<em>If you choose to use `@Multiple` together with `@FieldSet`, make sure that the `@DialogField` annotation is also present. In other `@FieldSet` use cases it is not necessary to add `@DialogField`.</em>

Please note that `@Multiple` is primarily designed for easy, "quick give me a multifield out of my single widget" cases. For more complicated cases it lacks tweaking capabilities that `@MultiField` itself has.

<hr/>
<h2 id="see-also" class="h3">See also</h2>

- [Grouping fields with FieldSet](./configuring-fieldset.md)
