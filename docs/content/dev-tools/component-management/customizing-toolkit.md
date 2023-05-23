<!--
layout: content
title: Customization
seoTitle: Customization - Exadel Authoring Kit
order: 6
-->

## Customizing the ToolKit to your needs

### Custom annotations. Annotation scopes

When creating markup for the Granite UI, the ToolKit handles data from the project's source code. Often it comes from Java annotations like `@AemComponent`, `@EditConfig`, or, e.g., `@DatePicker`.

You can create such annotations yourself. In the most basic case, you only need to declare an arbitrary annotation and attach the `@AnnotationRendering` meta-annotation.

`@AnnotationRendering` allows specifying what properties will be automatically mapped to the underlying node's attributes and in what *scope*.

#### Custom annotation scope

The notion of *scope* refers to the region of a component in which the current annotation/handler is effective. The most common scopes are enumerated in the `Scopes` class. These are the component scope (roughly maps to the *.content.xml* file in a component's folder as we see in the project source files), *\<cq:dialog>*, *\<cq:design_dialog>*, *\<cq:editConfig>*, *\<cq:childEditConfig>*, *\<cq:htmlTag>*. There can be custom scopes for specific cases. Whenever the scope is not specified, the default (or "all-included") scope is assumed.

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
public class MyComponent {/* ... */
}
```

In this sample, the ToolKit essentially creates two Granite nodes: one that would be represented by *.content.xml* in the project's source code and one represented by *_cq_dialog.xml*. The former is based on `@AemComponent` and the latter on `@Dialog` because this is the default functionality of the ToolKit.

The `@CustomDialogAnnotation` will also affect the Granite UI markup. Its `@AnnotationRendering` meta-annotation determines that it will affect only the *_cq_dialog.xml* as follows from `scope = Scopes.CQ_DIALOG` (be aware that the *scope* property can also accept an array of values).

You can omit the *scope* property. Then the appropriate scope will be decided on from other annotations attached to the current class. That is, if the class is `@Dialog`-annotated and a custom annotation is missing a *scope*, it is assumed that the custom annotation is also bound to the dialog scope. But if the class has its `@EditConfig` specified but no `@Dialog`, it is assumed that the custom annotation is within the *\<cq:editConfig>* scope, etc.

#### How to control the automapping

From `@CustomDialogAnnotation`, the following property values will be automatically mapped: *field1*, *field2*, and *field3*. That is because they have the "mappable" property type. Automatic mapping works for `string`s (and string arrays); `long`s (and long arrays), `double`s (and double arrays), `boolean`s (and boolean arrays); `enum` types (ane enum arrays). However, it does not work for `Class<?>`-typed properties or annotation types.

There is a way to restrict automatic mapping to particular properties by specifying them in the following way: `@AnnotationRendering(properties = {"field1", "field2"})`. You can also turn off automapping completely without removing `@AnnotationRendering`. To do this you specify `@AnnotationRendering(value = {})` or `@AnnotationRendering(value = "none")`.

You can also set a prefix for all the properties rendered via the current annotation. Just use `@AnnotationRendering(prefix="some_value")`.

#### @PropertyRendering

More settings for the mapping flow can be defined at the individual property level. See the following example:

```java

@AnnotationRendering(properties = "all") // "all" is implied by default but added here for readability
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

*name* is used to alter the attribute's name in JCR (in particular, to use a prefixed name or to prepend a namespace). On the contrary,*ignorePrefix* allows for stripping off a name prefix if it was defined at an upper level (e.g., in the FieldSet).

*scope* can be specified to define what JCR node (XML file) will contain this particular property. Useful to distribute properties between, e.g., *_content.xml* and *_cq_dialog.xml* files.

*allowBlank* determines that even empty or blank property values will be rendered (default behavior is to skip them). On the contrary, *ignoreValues* ensures that a particular non-blank value (or an array of values) will be skipped as the negligible default. *ignoreValues* accepts strings like `"42"` or `"false"` to ensure that even a numeric or boolean value matching the string can be skipped.

*transform* is primarily useful for `enum`-typed arguments. It provides a way to, for example, render Java enum values in lowercase, which is the standard for Granite settings. Important: if an enum value is set to be skipped via *ignoreValues*, specify the already transformed writing and not the original.

*valueType* allows you to control how a value is stored in JCR. For example, a value of type `boolean` would be by default rendered as `{Boolean}true` or `{Boolean}false`. If you need the type hint skipped, make the ToolKit perceive the value as a string by specifying `valueType = String.class`.

### Custom handlers

ToolKit annotations are rendered with *Handlers* (even an automatically mapped annotation is processed via an undercover "automapping handler"). All the out-of-box annotations are supplemented with bundled handlers, but you can declare custom ones as well.

Here is an example of how a custom handler can look:

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
        target.attribute("multifieldSpecial", "This is added to MultiFields");
    }
}
```

Every custom handler is characterized by the following features:

1) Marked with the `@Handles` annotation;
2) Implements `Handler`;
3) Overrides the `accept(Source, Target)` method in which the payload logic is run.

Usually, the ToolKit initializes one instance of every handler and manages it as a *singleton*, so a developer is expected to avoid assigning handler-wide *states*. All the logic should be processed within the `accept(Source, Target)`method or in methods called from the latter.

#### @Handles

`@Handles` is the marker of a handler. This annotation exposes the following properties.

*value* is a single `Class` reference or an array of classes. The classes are indeed the annotation types. This setting determines what annotations of a component class or its member will trigger the execution of the current handler.

There is no restriction regarding what annotations can be handled; built-in ones (then the current handler will provide some "additional" handling); custom ones; and even third-party annotations are covered. A custom annotation doesn't necessarily have to be marked with something like `@AnnotationRendering`; the absence of the meta-annotation would only lead to the automapping not being performed.

*scope* is an optional property determining in what [scope](#custom-annotation-scope) this handler will operate. If not specified, the scope will be decided on by querying for the  *scope* of the handled annotation. The default is all applicable scopes.

*before* and *after* parameters allow for arranging the sequence of handling. If neither is specified, the handlers are executed in the following sequence: first the built-in handlers hooked to this annotation, then custom handlers, in alphabetical order by name.

#### Source object

The first argument of a handler's `accept` method is the [Source](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/handlers/Source.html). This is a generic data provider that matches the entity (a Java class or a class member) the handler is called for. If the current handler is invoked due to an annotation attached to a class, the *Source* represents the class itself. But if the annotation was attached to a method or a field, the *Source* stands for the underlying member.

The *Source* is further specified by calling the `adaptTo()` method that accepts the only argument: the adapter type. You can, for example, call `source.adaptTo(Annotation[].class)` to get the array of annotations attached to the source-reflected class or class member. Also, you can specify a particular annotation like `source.adaptTo(DialogField.class)`. If the referred annotation is not present, `null` is returned. To mitigate null pointer management you can call the `tryAdaptTo()` method that will return an `Optional` object.

There are predefined source adapters. For instance, a source object can be adapted to `MemberSource` that has several properties specific for member-based sources (refer to this [javadoc](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/handlers/MemberSource.html) for details).

You can create your own adapter that will encapsulate some reasonable logic. See the following code snippet which shows how to virtually turn a ToolKit annotation into a writable Java entity:

```java

@Adapts(Source.class) // Indicates the adaptable. For all source adaptations, the value should be "Source"
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

Adapter instances are retained for a *Source* through the handling chain. Therefore, you can assign values to custom adapters and be sure that the same value can be retrieved in, for example, another handler processing some other annotation attached to the same class or class member.

#### Target object

In a ToolKit's handler, *Target* stands for an abstraction of rendering a target. Each *Target* instance represents a future Granite UI entity or a corresponding XML node. It can have its attributes, a parent target, and an ordered collection of child targets (nodes) the same way that Granite/XML nodes do.

The ToolKit API is designed in a way to make operation *Targets* more convenient than operation "bare" XML DOM entities. There are 40+ methods that comprise functional areas such as:

- Retrieving/setting of the (tag)name, prefix, and postfix;
- Retrieving/creating child targets by name or by relative path (multi-segment paths are supported);
- Traversing the targets tree upward and downward and finding a matching sub- or super-target by criteria;
- Moving, inserting, or deleting child targets;
- Setting an attribute to a target;
- Mapping a "whole" annotation to a target (its properties become the target's attributes);
- Removing attributes, etc.

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
                .attribute("intArray", new long[]{1, 2, 3}) // assigns an array
                .createChild("subitem1")
                .getParent() // returns the parent node of the last processed node
                .createChild("subitem2")
                .attribute("dateCreated", Date.from(calendar.getInstance().toInstant())); // assigns a date
        }

        Target subitem0 = testable.getTarget("item0/subitem0"); // traverses the target tree
        Target subitem1 = subitem0.getTarget("../../item0/subitem0"); // ".." returns a parent target, or else the current one if the parent is null

        Assert.assertNotNull(testable); // '..' returns the current node if there's no parent
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

In this example, the *items* node is created within the given *target*. Then *items* are populated with subnodes, each having the *ordinal* attribute with the values 1.. 10. Each one will be given two children of its own with the names *subitem1* and *subitem2*.

The following code would help retrieve a collection of nodes (regardless the nested level), the names of which end with "1":

```
List<Target> firstChildren = currentTarget.findChildren(t -> t.getName().endsWith("1"))
```

Next is the way to collect all the targets within the target tree that are grandchildren of nodes with the container resource type:

```
List<Target> containerDescendants = currentTarget.findChildren(t -> StringUtils.defaultString(t.getTarget("../..").getAttribute("sling:resourceType")).equals("granite/ui/components/coral/foundation/container"));
```

Another snippet shows how to retrieve an ancestor with a particular name:

```
Target root = currentTarget.findParent(t -> t.getName().equals("jcr:root"))
```

There are many more possibilities. For greater detail, see the inline documentation on [Target](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/handlers/Target.html).

Just like *Source*, *Target* is an adaptable entity. By default, *Target* adapts to `DomAdapter` with the possibility of being serialized to an XML DOM document. You can apply any custom adapters in the way described in the "Source object" section.

### Debugging custom plugin logic

You can debug the ToolKit's plugin while building your AEM project. In order to do so, you need to:

1) Make sure that the two projects are open each in its own window of your IDE:
- the Toolkit itself,
- and the "target" project (the one that contains the Toolkit plugin in its POM file).
2) Launch the build of the **target** project with `mvndebug` instead of `mvn`. Do it like this:
```
mvnDebug clean install -PautoInstallPackage
```
(Mind that the build won't technically start off before the next step. `mvnDebug` just establishes a listener service and waits for an incoming connection).
3) Switch to the ToolKit's IDE window. Start the remote debug session with _localhost_ as the host and port _8000_. Most convenient is to create a new "Run/debug configuration" with these parameters in your IDE. The rest of the parameters will remain default.
4) Now the actual build process starts. You can set breakpoints in the plugin's code and operate as usual.

Read more on debugging a Maven plugin e.g. [here](https://spin.atomicobject.com/2020/08/20/maven-debugging-intellij/).

### Running integration tests

Starting from version _2.3.0_, the ToolKit supports integration tests powered by [Selenide](https://selenide.org). They are mainly for checking the browser-bound functions as well as checking the connection to a live AEM server and/or 3rd-party services on the Internet.

The integration tests are localed in the `it.tests` module. Important: unlike unit tests, integration tests are not run in frames of a "regular" build. To run them, you need to specify the dedicated Maven profile like the following:
```
mvn clean install -Pintegration
```

As the integration tests start, a synthetic content package containing test data will be created and deployed to a live AEM instance (the one specified by the _aem.host_ and/or _aem.port_ properties). You can find the data in AEM under _/apps/etoolbox-authoring-kit-test_ and also in the same folders under _/conf_ and _/content_.

If the live AEM instance is not available, the tests will fail.

The login and password are needed to install the package. You may specify them with _aem.login_ and _aem.password_ properties if they are not the default _admin/admin_ pair.

After the integration tests are run, the synthetic package and its content are removed from the live AEM instance. You may, however, want them to stay, e.g., to manually re-run the test cases that failed. For that purpose, specify the _nouninstall=true_ property either in the Maven file or in the command line.

A complete command line for running integration tests with different properties specified may look like the following:
```
mvn clean install -PautoInstallPackage -Pintegration -D"aem.host"=192.168.0.81 -D"aem.port"=8080 -D"aem.login"=siteadmin -D"aem.password"=MyPa$$w0rd -Dnouninstall=true

```
