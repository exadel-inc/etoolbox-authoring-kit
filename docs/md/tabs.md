[Main page](../../README.md)
## Laying out your components with Tabs
### @Tab
There are several ways to create tabbed dialogs. First, you may need to mark a nested class of your @Dialog-annotated class with @Tab annotation. The _title_ property of @Tab will be used as the tab's node name, non-alphanumeric characters skipped (for example, `@Tab(title="First tab title!")` will produce _\<firstTabTitle>_ tag)
```java
@Dialog(layout = Layout.TABS)
public class Dialog {
    @Tab(title = "First tab")
    static class Tab1 {
        @DialogField(label="Field on the first tab")
        @TextField
        String field1;
    }
    @Tab(title = "Second tab")
    static class Tab2 {
        @DialogField(label="Field on the first tab")
        @TextField
        String field2;
    }
}
```
(Note the `layout = DialogLayout.TABS` assignment. This is to specify that the dialog *must* display fields encapsulated in nested classes per corresponding tabs. If `layout` is skipped, or set to its default `FIXED_COLUMNS` value, tabs will not show and only "immediate" fields of the basic class will be displayed).

The other way of laying out tabs is to define array of `@Tab` within `@Dialog` annotation. Then, to settle a field to a certain tab you will need  to add `@Place` annotation to this particular field.  The values of `@Place` must correspond to the *title* value of the desired tab. This is a somewhat more flexible technique which avoids creating nested classes and allows freely moving fields. You only need to ensure that tab title is specified everywhere in the very same format, no extra spaces, etc.
```java
@Dialog(
    name = "test-component",
    title = "test-component-dialog",
    tabs = {
        @Tab(title = "First tab"),
        @Tab(title = "Second tab"),
        @Tab(title = "Third tab")
    }
    // layout = Layout.TABS is implied by default here because of "tabs" property set
)
public class TestTabs {
    @DialogField(label = "Field on the first tab")
    @TextField
    @PlaceOn("First tab")
    String field1;

    @DialogField(label = "Field on the second tab")
    @TextField
    @PlaceOn("Second tab")
    String field2;

    @DialogField(description = "Field on the third tab")
    @TextField
    @PlaceOn("Third tab")
    String field3;
}
```
[Inheritance](container-inheritance.md)
