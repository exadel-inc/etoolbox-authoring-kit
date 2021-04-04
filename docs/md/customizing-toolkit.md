[Main page](../../README.md)

## Customizing ToolKit to your needs

### Custom annotations. Annotation scopes

When creating markup for the Granite UI, the ToolKit handles data that comes from the project's source code. Most of the time this is  Java annotations such as `@AemComponent`, `@EditConfig` or, to say, `DatePicker.

You can create such annotations yourself. In the most basic case, the only thing you need is to declare an arbitrary annotation and attach the `@MapProperties` meta-annotation to it.

`@MapPropeerties` plays the double role. First, it certifies that the current annotation is the ToolKit-handled annotation that should produce some markup. Second, it allows specifying what properties of the annotation will be automatically mapped to the underlying node's attributes, and in what *scope*.

The notion of <u><i>scope</i></u> defines the region of a component in which the current annotation (and also a handler - see below) is effective. The most common scopes are enumerated in the `Scopes` class. These are the component scope (roughly maps to the *.content.xml* file immediately under a component's folder as we see it in the project source files), *\<cq:dialog>*, *\<cq:design_dialog>*, *\<cq:editConfig>*, *\<cq:childEditConfig>*, *\<cq:htmlTag>*. There can be custom scopes for specific cases. Whenever the scope is not specified, the default (or "all-included") scope is assumed.

Consider the following example:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MapProperties(scope = Scopes.CQ_DIALOG)
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

Basically, in this sample the ToolKit creates two Granite nodes: the one that would be represented by *.content.xml* in the project's source code, and the one represented by *_cq_dialog.xml*. The former is based on `@AemComponent` and the latter - on `@Dialog` because this is the pre-defined functionality of the ToolKit.

The `@CustomDialogAnnotation` will also affect the Granite UI markup. Its `@MapProperties` meta-annotation determines that it will affect merely the *_cq_dialog.xml* as follows from `scope = Scopes.CQ_DIALOG` (mind that the *scope* property can as well accept an array of scopes).

You can omit the *scope* property setting. Then the appropriate scope will be decided on from the set of other annotations attached to the current class. (That is, if the class is `@Dialog`-annotated, and a custom annotation is missing *scope*, it is assumed that the current annotation is also bound to the dialog scope. But if the class has its `@EditConfig` specified but no `@Dialog`, it is assumed that the custom annotation is within the *\<cq:editConfig>* scope, etc.)

From `@CustomDialogAnnotation` the following property values will be automatically mapped: *field1*, *field2*, and *field3*. That is because they have the "mappable" property type. Automatic mapping works for `string`s (and string arrays); `long`s (and long arrays), `double`s (and double arrays), `boolean`s (and boolean arrays); `enum` types (ane enum arrays). However, it does not work for `Class<?>`-typed properties, and annotation types.

There is the way to restrict automatic mapping

### Debugging custom logic

You can debug the ToolKit's plugin while building your AEM app. In order to do it run your build in debug mode e.g.:
```
mvnDebug clean install -Pinstall-assets
```

Afterwards you can set breakpoints in your IDE, start a debugging session and connect to the build process. Default port is 8000.
