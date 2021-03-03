[Main page](../../README.md)
## Container inheritance
In *AEM Authoring Toolkit*, if an annotated Java class extends another class where dialog fields exist, these fields also become part of the dialog. This might sound non-obvious, because Java itself restricts visibility of class members declared of superclass member  while elements of parent AEM entities are generally accessible via their children (see _overlaying_).

Same way, containers, such as tabs or accordion panels, defined in a superclass are "inherited" by the subclass, and the layout instructions (`@Place`) are in effect even if referring to the superclass container.

If you do not want to have some "inherited" containers in yor dialog, add the `@Ignore` annotation as follows:
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
@Ignore(sections = {"First tab", "Second tab"})
public class TestTabsExtension { /* ... */}
```
Note that `@Ignore` setting is *not* inherited, unlike fields themselves, and works only for the class where it was specified.

See also: [Fields inheritance and ways to cancel it](reusing-code.md)
