[Main page](../../README.md)

## Customizing ToolKit to your needs

### Custom annotations. Annotation scopes

When creating markup for the Granite UI, the ToolKit handles data that comes from the project's source code. Most of the time this is  Java annotations such as `@AemComponent`, `@EditConfig` or, to say, `DatePicker.

You can create such annotations yourself. In the most basic case, the only thing you need is to declare an arbitrary annotation and attach the `@AnnotationRendering` meta-annotation to it.

`@AnnotationRendering` allows specifying what properties of the annotation will be automatically mapped to the underlying node's attributes, and in what *scope*.

#### Custom annotation scope

The notion of *scope* speaks of the region of a component in which the current annotation/handler is effective. The most common scopes are enumerated in the `Scopes` class. These are the component scope (roughly maps to the *.content.xml* file immediately under a component's folder as we see it in the project source files), *\<cq:dialog>*, *\<cq:design_dialog>*, *\<cq:editConfig>*, *\<cq:childEditConfig>*, *\<cq:htmlTag>*. There can be custom scopes for specific cases. Whenever the scope is not specified, the default (or "all-included") scope is assumed.

Consider the following example:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(
    scope = Scopes.CQ_DIALOG,
    prefix = "some:"
)
public @interface CustomDialogAnnotation {
    String field1() default "";
    long field2();
    boolean field3() default false;
    Class<?> field4();
}

@AemComponent(
    path = "path/to/my/component",
    title = "My AEM Component"
)
@Dialog
@CustomDialogAnnotation(
    field1 = "value1",
    field2 = 42,
    field3 = true,
    field4 = Object.class
)
public class MyComponent {/* ... */}
```

Basically, in this sample the ToolKit creates two Granite nodes: the one that would be represented by *.content.xml* in the project's source code, and the one represented by *_cq_dialog.xml*. The former is based on `@AemComponent` and the latter - on `@Dialog` because this is the default functionality of the ToolKit.

The `@CustomDialogAnnotation` will also affect the Granite UI markup. Its `@AnnotationRendering` meta-annotation determines that it will affect merely the *_cq_dialog.xml* as follows from `scope = Scopes.CQ_DIALOG` (mind that the *scope* property can as well accept an array of scopes).

You can omit the *scope* property. Then the appropriate scope will be decided on from the set of other annotations attached to the current class. (That is, if the class is `@Dialog`-annotated, and a custom annotation is missing *scope*, it is assumed that the current annotation is also bound to the dialog scope. But if the class has its `@EditConfig` specified but no `@Dialog`, it is assumed that the custom annotation is within the *\<cq:editConfig>* scope, etc.)

#### How to control the automapping

From `@CustomDialogAnnotation` the following property values will be automatically mapped: *field1*, *field2*, and *field3*. That is because they have the "mappable" property type. Automatic mapping works for `string`s (and string arrays); `long`s (and long arrays), `double`s (and double arrays), `boolean`s (and boolean arrays); `enum` types (ane enum arrays). However, it does not work for `Class<?>`-typed properties, and annotation types.

There is the way to restrict automatic mapping to particular properties by specifying them in the following way: `@AnnotationRendering(properties = {"field1", "field2"})`. You can also turn off automapping completely without removing `@AnnotationRendering`. To do this you specify `@AnnotationRendering(value = {})` or `@AnnotationRendering(value = "none")`.

You can also set a prefix for all the properties rendered via the current annotation. Use `@AnnotationRendering(prefix="some_value")` for that.

#### @PropertyRendering

More settings for the mapping flow can be defined at the individual property level. See the following example:

```java
@AnnotationRendering(properties = "all") // "all" is implied by default but added for greater readability
public @interface CustomAnnotation {
    @PropertyRendering(
        name = "some:text",
        scope = Scopes.CQ_DIALOG,
        allowBlank = true,
        transform = StringTransformation.CAPITALIZE
    )
    String text();

    @PropertyRendering(
        name = "some:checked",
        ignoreValues = "false",
        valueType = String.class
    )
    boolean checked();
}
```

*name* is used to alter the name of the current attribute in JCR (in particular, to use a namespaced name) so that it is not equal to the name of the property. On the contrary,*ignorePrefix*, allows stripping off a name prefix if defined at an upper level (e.g. in the FieldSet).

*scope* helps to alter the scope (whether it has been set at the `@AnnotationRendering` level or remains the default one) for this particular property.

*allowBlank* determines that even empty or blank property values will be rendered (default behavior is to skip them because they are most of the time signal of uninitialized properties). On the contrary, *ignoreValues* makes sure that a particular non-blank value (or an array of values) will be skipped as the negligible default. *ignoreValues* accepts strings like `"42"` or `"false"` to make sure that even a numeric or boolean value matching the string can be skipped.

*transform* is primarily useful for `enum`-typed arguments. It provides a way to e.g. render Java enum values in lowercase which is the standard for Granite settings. Important: if an enum value is set to be skipped via *ignoreValues*, specify the already transformed writing and not the original.

*valueType* allows altering the way a value is rendered in JCR. For example a value of type `boolean` would be by default rendered as `{Boolean}true` or `{Boolean}false`. If you need the type hint skipped, make the ToolKit perceive the value as string by specifying `valueType = String.class`.


### Custom handlers

ToolKit annotations are rendered with *Handlers* (even an automatically mapped annotation is in fact processed via an undercover "automapping handler"). All the out-of-box annotations are supplemented with bundled handlers, but you can as well declare custom ones.

Here is an example of how a custom handler can look like:

```java
@Handles(
    value = MultiField.class,
    scope = Scopes.CQ_DIALOG,
    before = MyCustomHandler.class,
    after = MyAnotherCustomHandler.class
)
public static class CustomMultifieldHandler implements Handler {

    @Override
    public void accept(Source source, Target target) {
        target.attribute("multifieldSpecial", "This is added to Multifields");
    }
}
```

Every custom handler is characterized by the following traits:
1) it is marked with the `@Handles` annotation;
2) it implements `Handler`;
3) as the `Handler` implementer, it overrides the `accept(Source, Target)` method in which the payload logic is run.

Normally the ToolKit initializes one instance of every handler and manages it as a *singleton*, so a developer is expected to avoid assigning handler-wide *states*. All the logic should be processed within the `accept(Source, Target)` method or in methods called from the latter.

#### @Handles

`@Handles` is the basic marker of a handler. This annotation exposes the following properties.

*value* is a single `Class` reference, or an array of classes. The classes are indeed the annotation types. This setting determines what annotations of a component class or its member will trigger the execution of the current handler.

There is no restriction regarding what annotations can be handled: built-in ones (then the current handler will provide some "additional" handling); custom ones, or even third-party annotations. A custom annotation doesn't necessarily have to be marked with e.g. `@AnnotationRendering`: the absence of the meta-annotation would only lead to the automapping not being performed.

*scope* is an optional property determining in what [scope](#custom-annotation-scope) this handler will operate. If not specified, the scope will be decided on by querying for the  *scope* of the handled annotation. Default is all applicable scopes.

*before* and *after* parameters allow arranging the sequence of handling (either build-in or custom handlers can, again, be anchored to). If neither is specified, the handlers are executed in the following default order: first the built-in handlers hooked to this annotation; then custom handlers, in alphabetical order of their names.

#### Source object

The first argument of a handler's `accept` method is the `Source` - a generic data provider that matches the entity (a Java class, or a class member) the handler is called for. If the current handler is invoked dut to an annotation attached to a class, the *Source* represents, roughly speaking, the class itself. But if the annotation was a method's or a field's annotation, the *Source* stands for the underlying member.

The *Source* is further specified by calling the `adaptTo()` method which accepts the only argument - the adapter type. You can e.g. call `source.adaptTo(Annotation[].class)` to get the array of annotations attached to the source-reflected class, or class member. Also, you can specify a particular annotation like `source.adaptTo(DialogField.class)`. If the referred annotation is not actually present, `null` is returned. To mitigate null pointer management you can call `tryAdaptTo()` method that will return an `Optional` object.

There is a bunch of predefined source adapters. For instance, a source object can be adapted to `MemberSource` that has several properties specific for member-based sources (refer to this [javadoc](../../core/src/main/java/com/exadel/aem/toolkit/api/handlers/MemberSource.java) for details).

You can create your own adapter that would encapsulate some reasonable logic. See the following code snippet which shows how to virtually turn a ToolKit annotation into a writable Java entity:

```java
@Adapts(Source.class) // Indicates the adaptable. For source adaptations the value should be Source.class
public class WritableDialogField {

    private int ranking;

    // Instance constructor *must* be public and accept the only argument - the instance of adaptable
    public WritableDialogField(Source source) {
        if (source == null || source.adaptTo(DialogField.class) == null) {
            return;
        }
        this.ranking = source.adaptTo(DialogField.class).ranking;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
}

@Handles(CustomAnnotation.class)
public class CustomHandler implements Handler {
    @Override
    public void accept(Source source, Target target) {
        int ranking = source.adaptTo(DialogField.class).ranking();
        source.adaptTo(WritableDialogField.class).setRanking(ranking + 1);
    }
}
```
Adapter instances are retained for a *Source* through the whole handling chain. Therefore, you can actually assign values to custom adapters and be sure that the same value can be retrieved in e.g. another handler processing some other annotation attached to the same class or class member.

#### Target object

In a ToolKit's handler, *Target* stands for an abstraction of rendering target. Basically, each *Target* instance represents a future Granite UI entity, or a corresponding XML node. It can have its attributes, a parent target, and an ordered collection of child targets (nodes) in the same way as Granite/XML nodes do.

The ToolKit API is designed in a way to make operation *Targets* more convenient than operation "bare" XML DOM entities. There are 40+ methods that comprise such functional areas as:
- retrieving/setting of the (tag)name, prefix and postfix;
- retrieving/creating child targets by name or by the relative path; multi-segment paths are supported;
- traversing the targets tree upward and downward; finding a matching sub- or super-target by criteria;
- moving, inserting, deleting child targets;
- setting an attribute to a target;
- mapping a whole annotations to a target, its properties becoming the target's attributes;
- removing attributes, etc.

Consider the following example:

```java
@Handles(CustomAnnotation.class)
public class CustomHandler implements Handler {

    @Override
    public void accept(Source source, Target target) {
        String memberName = source.getName();
        target
            .attributes(source.adaptTo(DialogField.class)) // maps all the props of @DialogField to target's attributes
            .attribute("processed", true)  // assigns a boolean-type attribute
            .attribute("sourceName", memberName)
            .createChild("items"); // creates a child target by name

        for (int i = 0; i < 10; i++) {
            target.getOrCreateChild("items/item" + i) // retrieves a child target by relative path
                .attribute("ordinal", i + 1) // assigns a numeric attribute
                .attribute("intArray", new long[] {1, 2, 3}) // assigns an array
                .createChild("subitem1")
                .getParent() // returns the parent node of the last processed node
                .createChild("subitem2")
                .attribute("dateCreated", Date.from(calendar.getInstance().toInstant())); // assigns a date
        }

        Target subitem0 = testable.getTarget("item0/subitem0"); // traverses the target tree
        Target subitem1 = subitem0.getTarget("../../item0/subitem0"); // ".." returns a parent target, or else the current one if parent is null

        // The following coder will help to retrieve

        Assert.assertNotNull(testable); // '..' path returns the current node if no parent
        Assert.assertEquals("subitem0", testable.getTarget("item2/subitem2/../../item0/subitem0").getName());
        Assert.assertNull(testable.getTarget("item2/subitem3/../../item0/subitem0"));

        Assert.assertEquals(TIER_1_CHILD_COUNT / 2, testable.findChildren(t -> t.getName().startsWith(NN_SUBITEM)).size());
        Assert.assertEquals(3, testable.findChildren(t -> t.getAttribute(PN_ORDINAL).equals("{Long}1")).size());
        Target subsubitem = testable.findChild(t -> !t.getTarget("../../..").equals(t.getTarget("../..")));
        Assert.assertEquals(NN_SUBSUBITEM + 0, subsubitem.getName());
        Assert.assertEquals(testable, subsubitem.findParent(t -> t.getName().equals(NN_ROOT)));

    }
}
```

In this example, the *items* node is created within the given *target*. Then *items* are populated with ten subnodes each having its *ordinal* attribute with the values 1.. 10. Each of the subnodes will be given two children of its own with the names *subitem1* and *subitem2*.

The following code would help to retrieve a collection of nodes (regardless the nested level) names of which end with "1":
```
List<Target> firstChildren = currentTarget.findChildren(t -> t.getName().endsWith("1"))
```

Next is the way to collect all the targets within the target tree that are grandchildren of nodes with the container resource type:
```
List<Target> containerDescendants = currentTarget.findChildren(t -> StringUtils.defaultString(t.getTarget("../..").getAttribute("sling:resourceType")).equals("granite/ui/components/coral/foundation/container"));
```

And the next snippet shows how to retrieve an ancestor with the particular name:
```
Target root = currentTarget.findParent(t -> t.getName().equals("jcr:root"))
```

There are much more possibilities. For deeper detail, see the inline documentation on [Target](../../core/src/main/java/com/exadel/aem/toolkit/api/handlers/Target.java).

Same as the *Source*, *Target* is an adaptable entity. By default, *Target* adapts to `DomAdapter` with the possibility to be serialized to an XML DOM document. You can also apply any custom adapters in the way describe in the "Source object" division.


### Debugging custom logic

You can debug the ToolKit's plugin while building your AEM app. In order to do it run your build in debug mode e.g.:
```
mvnDebug clean install -Pinstall-assets
```

Afterwards you can set breakpoints in your IDE, start a debugging session and connect to the build process. Default port is 8000.
