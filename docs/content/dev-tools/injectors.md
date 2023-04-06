<!--
layout: content
title: Injectors
seoTitle: Injectors - Exadel Authoring Kit
order: 4
-->
## Enhancing AEM Components' back-end with advanced Injectors

[Injectors](https://sling.apache.org/documentation/bundles/models.html#custom-injectors-1) are a powerful feature of
Apache Sling that is massively used across modern AEM projects. Injectors pass data directly to the Java classes that
back AEM components. This reduces boilerplate code and simplifies authoring as well.

Exadel Toolbox Authoring Kit (*ToolKit*) offers a number of Injectors you can use in your projects: both bound to other
ToolKit features and independent.

### Injector for EToolbox Lists

You can use it to inject the content of an EToolbox List directly into your Sling model. This injector can be used with
either a field, a method, or a constructor argument. You can retrieve either a *Collection* / *List* of list items or a
map depending on the type of the underlying variable (or else the return type of the underlying method).

To get the collection / list of items, use a notation as the following:

```java
public class SampleModel {
    // ...
    @EToolboxList("/content/etoolbox-lists/contentList")
    private List<SimpleListItem> itemsListResource;
    // ...
}

```

Note that your injected field's type can be either a `List` or an array, like `SimpleListItem[]`. Concerning the type of
items, you can specify a valid Sling model, or a *Resource*, or else *Object*. In the latter case, *Resource*-s will be
injected in place of *Object*-s. In the snippets we use the out-of-the-box *SimpleListItem*. However, any valid Sling
model will go (if it is adapted from a *Resource*). Generally, this injector works according to the same principles as the [ListHelper](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/core/lists/utils/ListHelper.html) utility class.

To get the map, type it as:

```java
public class SampleModel {
    // ...
    @EToolboxList(value = "/content/etoolbox-lists/contentList", keyProperty = "textValue")
    private Map<String, EToolboxListInjectorTest.LocalListItemModel> itemsMapTestModel;
    // ...
}
```

The optional *keyProperty* parameter is responsible for which of the properties of a list item is to be used as the map
key. By default it is the *jcr:title* property.

When applying the injector to a constructor, please use the following format:

```java
public class SampleModel {
    // ...
    @Inject
    public TestModelEToolboxList(@EToolboxList("/content/etoolbox-lists/myList") @Named List<Resource> listResource) {
        this.itemsListResourceFromMethodParameter = listResource;
    }
    // ...
}
```

### Injector for a child (or relative) resource

it is often needed to inject a relative resource (by its path) or a derived Sling model into the current model. Usually,
this is achieved through such annotations as *@ChildResource* or *@ChildResourceWithRequest*. However both solutions
have their limitations. To bypass some of them you can use the `@Child` annotation from the *ToolKit*.

Among the advantages are:

- ability to inject any resource by an absolute or relative path (in fact not only a child or a "grandchild", but also a
  parent, or an unrelated resource, or even the current resource itself);
- ability to inject a secondary Sling model adapted from such resource (or else from a resource wrapped in a synthetic
  request);
- ability to select particular properties from a target resource used for injection and/or adaptation into a secondary
  model. This way, you can create and manage several "virtual" resources from one "real" resource.

Consider the following code samples:

```java
public class SampleModel {
    // ...
    @Child
    private Resource childResource; // The direct child of the current resource by the self-implied name "childResource" is injected

    @Child(name = "list")
    private Resource listResource; // The direct child of the current resource by the name "list" is injected

    @Child(name = "./list")
    private Object listResource; // Same as above. If a field is of "Object" type, Resource is injected

    @Child(name = "..")
    private Resource parent; // The direct parent of the current resource is injected

    @Child(name = "/content/myPage/jcr:content/myResource")
    private ListItemModel modelByAbsolutePath; // An adaptation of the current resource to the "ListItemModel" class is injected

    @Child(name = "some/path/nestedListItem")
    private ListItemModel modelByRelativePath; // Same as above; a relative path is used
    // ...
}
```

The other example will show how to virtually "split" one resource into several "sub-resources" representing different
entities (e.g., fieldsets) in the course of injection. Consider you have to manage a resource with the properties like:

```
jcr:primaryType = "nt:unstructured"
sling:resourceType = "some/resource/type"
fieldset1_title = "Hello"
fieldset1_description = "Earth"
fieldset2_title = "Goodbye"
fieldset2_description = "Moon"
```

There are two obvious "sub-resources" within this structure. They can be addressed to separately like in the sample:

```java
public class SampleModel {
    // ...
    @Child(prefix = "fieldset1_")
    private TextDescriptionFieldset first;

    @Child(prefix = "fieldset2_")
    private TextDescriptionFieldset second;
    // ...
}
```

While the referenced model itself can have the following listing:

```java
@Model(adaptables = Resource.class)
public class TextDescriptionFieldset {

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String description;
}
```

Combined, these pieces will produce the following result (pseudocode):

```
first.text == "Hello";
first.description = "Earth";
second.text == "Goodbye";
second.description == "Moon";
```

The same principle applies to the *postfix* property.

Note: the `@Child` annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, use the notation like `(@Child @Named SomeModel argument)` and annotate the constructor itself with `@Inject`.

### Injector for a list of child resources

The `@Children` injector follows much the same pattern as the `@Child` annotation. The differences are:

- it is used with fields, methods, or constructor arguments that are typed as *Collection* (*List*) or an array;
- the path specified in the annotation doesn't point to the particular resource, but a parent resource *children of
  which* will be injected;
- there's the ability to filter children with a predicate.

See the following samples:

```java
public class SampleModel {

    @Children
    private List<Resource> list; // Will inject children of the subresource named "list" of the current resource

    @Children(name = "./list")
    private List<Resource> resourceList; // Same as above. You could also specify "list" without "./"

    @Children(name = "./")
    private List<Object> ownList; // Will inject children of the current resource

    @Children(name = "/content/nested-node", prefix = "prefix_")
    // Will inject children of "/content/nested-node" adapted to the "ListItemModel" class.
    // Will only consider the properties names of which start with "prefix_"
    private ListItemModel[] listItemModelsWithPrefix;

    @Children(name = "/content/nested-node", postfix = "-secondary")
    // Will inject children of "/content/nested-node" adapted to the "ListItemModel" class.
    // Will only consider the properties names of which end with "-secondary"
    private List<ListItemModel> listItemModelsWithPrefix;
}
```

Children's filters can be specified as references to classes that implement `Predicate<Resource>`:

```java
public class SampleModel {

    @Children(name = "list", filters = DateIsNotFuture.class)
    private List<ListItemModel> listItemModels;

    // ...

    public class DateIsNotFuture implements Predicate<Resource> {

        @Override
        public boolean test(Resource value) {
            Calendar date = value.getValueMap().get("date", Calendar.class);
            return date != null && !date.after(Calendar.getInstance());
        }
    }
}
```

Some filters are supplied out of the box. E.g., `NonGhostFilter` allows filtering out "ghost component" resources (those with the resource type "*wcm/msm/components/ghost*"). The `NonNullFilter` filters out invalid/null children.

Note: the `@Children` annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, write it like `(@Children @Named("path") List<ListItemModel> argument)` or
else `(@Children(name = "path") @Named List<ListItemModel> argument)` and also annotate the constructor itself with `@Inject`.

### Injector for request parameters

The `@RequestParam` annotation is used to inject a request parameter. The annotated member can be of type *String* or *Object*, then a value coerced to string is injected. Else, the parameter can be of type *RequestParameter* (including a list or an array of that type) or *RequestParameterMap* so that the corresponding objects obtained via the *SlingHttpServletRequest* could be injected.

Note: this annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, use the notation like `(@RequestParam @Named String argument)` and annotate the constructor itself with `@Inject`.

### Injector for request selectors

The `@RequestSelectors` annotation can be used to inject Sling request selectors. If the annotated member is of type *
String* or *Object*, the "whole" selector string is injected. But if the annotated member represents an array or a list
of strings or objects, selectors are injected one by one in the underlying *List* or array.

See the code samples:

```java
public class SampleModel {
    // ...
    @RequestSelectors
    private String selectorsString; // Will inject all selectors like "selector1.selector2.selector3"

    @RequestSelectors
    private List<String> selectorsList; // Will inject the list of selectors

    @RequestSelectors
    private String[] selectorsArray; // Will inject the array of selectors
    // ...
}
```

Note: this annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, use the notation like `(@RequestSelectors @Named String argument)` and annotate the constructor itself with `@Inject`.

### Injector for request suffix

The `@RequestSuffix` is used to inject a Sling request suffix. If the annotated member is of type *String* or *Object*, the string value of suffix is injected. If the annotated member is of type *Resource*, the injector will inject the corresponding JCR resource.

Note: this annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, write it like `(@RequestSuffix @Named String argument)` and annotate the constructor itself with `@Inject`.

### Injector for request attributes

The `@RequestAttribute` is an advanced variant of the Sling models API annotation sharing the same name. Same as its Sling API prototype, it allows to a assign a value of the request's attribute to a Java class member. It is more universal and "forgiving" as it comes to value types, though.

With EAK Authoring Kit's `@RequestAttrbiute`, you can
- Inject attribute values of any reference or primitive type. Boxed types are cast as needed;
- Inject arrays and `List`-s / `Set`-s. If the attribute value is of an array type, it can be injected into either an array-typed or `List`/`Set`-typed class member. Similarly, a collection can be injected into either an array, a `List`. or a `Set`;
- Inject into a Java class member of a "widening" numeric type (e.g. an `int` value into a `long`-typed or `double`-typed field, etc.);
- Inject an implementation of an interface or an abstract class into a Java class member typed as the ancestor class / interface.

A usual case for `@RequestAttribute` is processing data passed via `data-sly-use` like in the following sample:
```html
<sly data-sly-use.model0="${'com.acme.project.MyModel' @ foo='Hello World', bar=42}"></sly>
<sly data-sly-use.model1="${'com.acme.project.MyModel' @ foo='Hello World', bar=[42, 43]}"></sly>
```
Both instructions would work well with a model designed like the following:
```java
@Model(adaptables = SlingHttpServletRequest.class)
public class MyModel {

    @RequestAttribute
    private CharSequence foo; // injects "Hello World" in both cases

    @RequestAttribute(name = "foo")
    private String foo2; // injects "Hello World" in both cases

    @RequestAttrbute
    private int bar; // injects 42 in the first case, 0 in the second case

    @RequestAttrbute(name = "bar")
    private long[] barArray; // injects {42L} in the first case, {42L, 43L} in the second case

    @RequestAttrbute(name = "bar")
    private List<Integer> barList; // injects Arrays.asList(42L) in the first case, Arrays.asList(42L, 43L) in the second case
}
```
Thus, `@RequestAttribute` makes integration with HTL more straightforward as it massively eliminates the need to guess what exact value type is being passed into the Sling model under the hood.

### Injector for I18n

The `@I18N` annotation can be used to inject either the OOTB `I18n` object or a particular internationalized value. Therefore, it is legitimate to use this annotation with an *I18n*-typed or a *String*-typed class member (plus with an *Object*-typed member which is then considered a string).

The behavior of *I18N* depends on the current locale. By default, the locale is guessed from the path of the page the current resource belongs to, or else the *jcr:language* property of that page. That is, a resource with the path like `/content/site/us/en/myPage/jcr:content/resource` or `/content/site/us-en/myPage/jcr:content/resource` will be considered belonging to the *en_US* locale.

You can override this guessing in two ways:
- directly specify the *locale* parameter of `@I18N`. The *locale* can contain either a two-char language token, or a 5-char language-and-country in one of the following formats: *en-us*, *en_us*, *en/us*. Mind that the language token always comes first, and the country token comes second;
- or specify a reference to a locale detector. A locale detector is a class which implements `Function<Object, Locale>`. The *Object* argument is the adaptable (usually a request or a resource). There are two predefined locale detectors: the `PageLocaleDetector` (it implements guessing by the page path as described above; you don't have to specify it manually), and the `NativeLocaleDetector`. If the latter is specified, the returned locale is effectively *null* which triggers the native AEM behavior - the locale is then derived from the logged user's preferences.

If both *locale* and *localeDetector* are specified, *locale* takes precedence.

When using `@I18N` with a String-typed class member you can specify the value to be internationalized. Use either to *value* property of `@I18N` or the standard `@Named` annotation. If none of these is present, the string to internationalize will derive from the name of the underlying class member.

Please take into account that AEM operates with resource bundles specified by both language and country. So if you specify a locale as *"en"* in one place and *"en-us"* elsewhere, these may be considered as two different locales and different resource bundles.

```java
public class SampleModel {
    // ...
    @I18N
    private I18n i18n;

    @I18N(locale = "it-it")
    private I18n i18nItalian;

    @I18N(localeDetector = NativeLocaleDetector.class)
    private I18n i18nFromUserPreferences;

    @I18N(locale = "it")
    @Named("Hello world")
    private String helloWorld; // Injects the "Ciao mondo" value if such is present in the resource bundle

    @I18N(value = "Hello world", localeDetector = MyDetectorReturnsItalian.class)
    private String helloWorld2; // Same as above
}
```

Note: this annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, write it like `(@I18N @Named String argument)` and annotate the constructor itself with `@Inject`.

