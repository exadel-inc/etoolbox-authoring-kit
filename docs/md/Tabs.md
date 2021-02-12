## Laying out your components with Tabs
### Tab
@Tab annotation

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

The other way of laying out tabs is to define array of `@Tab` within `@Dialog` annotation. Then, to settle a field to a certain tab you will need  to add `@PlaceOn` annotation to this particular field.  The values of `@PlaceOn` must correspond to the *title* value of the desired tab. This is a somewhat more flexible technique which avoids creating nested classes and allows freely moving fields. You only need to ensure that tab title is specified everywhere in the very same format, no extra spaces, etc.
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
#### Tabs inheritance
In *AEM Authoring Toolkit*, if a Java class annotated with `@Dialog` extends another class where potential dialog fields exist, these fields also become the part of the dialog. This may sound inobvious, because Java itself doesn't have the notion of field inheritance while AEM entities have (see _overlaying_).

Same way, tabs defined in a superclass are "inherited" by the subclass, and the `PlaceOn` instructions are in effect.

If you do not want to have some "inherited" tabs in yor dialog, add the `@IgnoreTabs` annotation as follows:
```java
@Dialog(
    name = "test-component",
    title = "test-component-dialog",
    tabs = {
        @Tab(title = "Fourth tab"),
        @Tab(title = "Fifth tab")
    }
)
// In AEM Authoring Toolkit, tabs are manipulated by their title strings
@IgnoreTabs({"First tab", "Second tab"})
public class TestTabsExtension { /* ... */}
```

Note that `@IgnoreTabs` setting is *not* inherited, unlike fields themselves, and works only for the class where it was specified.

See also: [Fields inheritance and ways to cancel it](#fields-inheritance-and-ways-to-cancel-it)

