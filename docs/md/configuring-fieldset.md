[Main page](../../README.md)

## Grouping fields with FieldSet

* @FieldSet
* Resource type: /libs/granite/ui/components/coral/foundation/form/fieldset
* See spec: [FieldSet](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fieldset/index.html)

The *FieldSet* is used to logically group a number of fields. The goal is achieved with the use of an external or a nested class that encapsulates grouping fields. Then an *\<OtherClass>*-typed field is declared and `@FieldSet` annotation is added.

The ToolKit will guess the kind of group of widgets to render through the type of the underlying field, but you might as well specify a particular type by setting the *value* property.

Hierarchy of classes is honored so that a *FieldSet*-producing class may extend another class from the same or even foreign scope. Proper field order within a fieldset can be guaranteed through the use of *ranking* values (see chapter on `@DialogField` in [Defining dialog fields, setting attributes](widget-annotations.md)).

Names of fields added to a FieldSet may share a common prefix and postfix.

Prefix is specified in *namePrefix* property. This can be a simple word or a string trailed with slash. In the latter case, values assigned to the FieldSet's fields will be directed to a subnode of the resource being edited. Technically, the prefix string can contain more slashes that will build up to a relative path. This path will be properly respected by the fields of the fieldset.

Postfix is specified in *namePostfix* property. This is used mostly to distinguish between fields that refer to different fieldset instances of the same type. But it is also possible to define a relative path to a prop via the postfix.

By default, the rendered fieldset has a visible margin, therefore, its fields are not aligned to the same vertical line as the widgets outside. If you do not need a margin around the fieldset, add `@Attribute(className="u-coral-noMargin")`

```java
public class DialogWithFieldSet {
    @FieldSet(title = "Field set example", namePrefix="fs-") // you could as well specify type of FieldSet other than FieldSetExample via "value" property
    private FieldSetExample fieldSet;

    private static class FieldSetExample extends ParentFieldSetExample {
        // Rankings are not necessary. They are put here to show the way a parent's field can be
        // rendered after the fields of a subclass, and not before
        @DialogField(ranking = 1)
        @TextField
        String field2;

        @DialogField(ranking = 2)
        @TextField
        String field3;

        @DialogField(ranking = 3)
        @TextField
        String field4;
    }

    private static class ParentFieldSetExample {
        @DialogField(ranking = 4)
        @TextField
        String field1;
    }
}
```
