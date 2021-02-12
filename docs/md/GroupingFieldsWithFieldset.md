## Grouping fields with FieldSet
### FieldSet
@FieldSet (Granite name: FieldSet, resource type: /libs/granite/ui/components/coral/foundation/form/fieldset)

Used to logically group a number of different fields as described in [Adobe's Granite UI manual on FieldSet](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fieldset/index.html). This goal is achieved by an external or a nested class that encapsulates grouping fields. Then an *\<OtherClass>*-typed field is declared, and `@FieldSet` annotation is added.

The `@FieldSet` will guess the kind of group of widgets to render through the type of the underlying field. But you may as well specify some particular type by setting the *source* property.

Hierarchy of classes is honored (so that a *FieldSet*-producing class may extend another class from same or even foreign scope. Proper field order within a fieldset can be guaranteed by use of *ranking* values (see chapter on `@DialogField` in [Widget annotations A-Z](docs/md/WidgetAnnotations.md)).

Names of fields added to a FieldSet may share a common prefix specified in *namePrefix* property. This can be a simple word, or a string trailed with slash. In the latter case, values assigned to the FieldSet's fields will directed to a subnode of the resource being edited.

If you do not need a margin around the fieldset added by default, add `@Attribute(className="u-coral-noMargin")`
```java
public class DialogWithFieldSet {
    @FieldSet(title = "Field set example", namePrefix="fs-") // you could as well specify type of FieldSet other
    private FieldSetExample fieldSet;                        // that FieldSetExample via 'source' property

    static class FieldSetExample extends ParentFieldSetExample {
        // Constructors are omitted
        // Rankings are not necessary, put here to show the way a parent's field can be
        // rendered after the fields of a subclass, and not before
        @DialogField(ranking = 1)
        @TextField
        String field6;

        @DialogField(ranking = 2)
        @TextField
        String field7;

        @DialogField(ranking = 3)
        @TextField
        String field8;
    }

    private static class ParentFieldSetExample {
        //Constructors are omitted for simplicity
        @DialogField(ranking = 4)
        @TextField
        String field6;
    }
}
```
