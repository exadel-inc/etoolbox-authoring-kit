<!--
layout: md-content
title: Injectors
tags: test2
-->
[Main page](../../README.md)

## Enhancing AEM Components' back-end with advanced Injectors

[Injectors](https://sling.apache.org/documentation/bundles/models.html#custom-injectors-1) are a powerful feature of
Apache Sling that is massively used across modern AEM projects. Injectors pass data directly to the Java classes that
back AEM components. This reduces boilerplate code and simplifies authoring as well.

Exadel Toolbox Authoring Kit (*ToolKit*) offers a number of Injectors you can use in your projects: both bound to other
ToolKit features and independent.

#### Injector for EToolbox Lists

You can use it to inject the content of an EToolbox List directly into your Sling model. This injector can be used with
either a field, a method, or a constructor argument. You can retrieve either a *Collection* / *List* of list items or a
map depending on the type of the underlying variable (or else the return type of the underlying method).

To get the collection / list of items, use a notation as the following:

```
@EToolboxList("/content/etoolbox-lists/contentList")
private List<SimpleListItem> itemsListResource;

```

Note that your injected field's type can be either a `List` or an array, like `SimpleListItem[]`. Concerning the type of
items, you can specify a valid Sling model, or a *Resource*, or else *Object*. In the latter case, *Resource*-s will be
injected in place of *Object*-s. In the snippets we use the out-of-the-box *SimpleListItem*. However, any valid Sling
model will go (if it is adapted from a *Resource*). Generally, this injector works according to the same principles as the [ListHelper](etoolbox-lists.md) utility class.

To get the map, type it as:

```
@EToolboxList(value = "/content/etoolbox-lists/contentList", keyProperty = "textValue")
private Map<String, EToolboxListInjectorTest.LocalListItemModel> itemsMapTestModel;

```

The optional *keyProperty* parameter is responsible for which of the properties of a list item is to be used as the map
key. By default it is the *jcr:title* property.

When applying the injector to a constructor, please use the following format:

```
@Inject
public TestModelEToolboxList(@EToolboxList("/content/etoolbox-lists/myList") @Named List<Resource> listResource) {
    this.itemsListResourceFromMethodParameter = listResource;
}
```

#### Injector for a child (or relative) resource

it is often needed to inject a relative resource (by its path) or a derived Sling model into the current model. Usually,
this is achieved through such annotations as *@ChildResource* or *@ChildResourceWithRequest*. However both solutions
have their limitations. To bypass some of them you can use the `@Child` annotation from the *ToolKit*.

Among the advantages are:

- ability to inject any resource by an absolute or relative path (in fact not only a child or a "grandchild", but also a
  parent, or an unrelated resource, or even the current resource itself);
- ability to inject a secondary Sling model adapted from such resource;
- ability to select particular properties from a target resource used for injection and/or adaptation into a secondary
  model. This way, you can create and manage several "virtual" resources from one "real" resource.

Consider the following code samples:

```
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

```
@Child(prefix = "fieldset1_")
private TextDescriptionFieldset first;

@Child(prefix = "fieldset2_")
private TextDescriptionFieldset second;
```

While the model itself can have the following listing:

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

#### Injector for a list of child resources

The `@Children` injector follows much the same pattern as the `@Child` annotation. The differences are:

- it is used with fields, methods, or constructor arguments that are typed as *Collection* (*List*) or an array;
- the path specified in the annotation doesn't point to the particular resource, but a parent resource *children of
  which* will be injected;
- there's the ability to filter children with a predicate.

See the following samples:

```
    @Children
    private List<Resource> list; // Will inject children of the subresource named "list" of the current resource

    @Children(name = "./list")
    private List<Resource> resourceList; // Same as above. You could also specify "list" without "./"

    @Children(name = "./")
    private List<Object> ownList; // Will inject children of the current resource

    @Children(name = "/content/nested-node", prefix = "prefix_") // Will inject children of "/content/nested-node" adapted to the "ListItemModel" class. Will only consider the properties names of which start with "prefix_"
    private ListItemModel[] listItemModelsWithPrefix;

    @Children(name = "/content/nested-node", postfix = "-secondary") // Will inject children of "/content/nested-node" adapted to the "ListItemModel" class. Will only consider the properties names of which end with "-secondary"
    private List<ListItemModel> listItemModelsWithPrefix;

```

One or more filters can be specified as references to classes that implement `Predicate<Resource>`:

```
@Children(name = "list", filters = DateIsNotFuture.class)
private List<ListItemModel> listItemModels;

// ...

public class DateIsNotFuture implements Predicate<Resource> {

    @Override
    public boolean test(Resource value) {
        Calendar date = return value.getValueMap().get("date", Calendar.class);
        return date != null && !date.after(Calendar.getInstance());
    }
}
```

Some filters are supplied out of the box. E.g., `NonGhostFilter` allows filtering out "ghost component" resources (those with the resource type "*wcm/msm/components/ghost*"). The `NonNullFilter` filters out invalid/null children.

Note: the `@Children` annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, write it like `(@Children @Named("path") List<ListItemModel> argument)` or
else `(@Children(name = "path") @Named List<ListItemModel> argument)` and also annotate the constructor itself with `@Inject`.

#### Injector for request parameters

The `@RequestParam` annotation is used to inject a request parameter. The annotated member can be of type *String* or *Object*, then a value coerced to string is injected. Else, the parameter can be of type *RequestParameter* (including a list or an array of that type) or *RequestParameterMap* so that the corresponding objects obtained via the *SlingHttpServletRequest* could be injected.

Note: this annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, use the notation like `(@RequestParam @Named String argument)` and annotate the constructor itself with `@Inject`.

#### Injector for request selectors

The `@RequestSelectors` annotation can be used to inject Sling request selectors. If the annotated member is of type *
String* or *Object*, the "whole" selector string is injected. But if the annotated member represents an array or a list
of strings or objects, selectors are injected one by one in the underlying *List* or array.

See the code samples:

```
@RequestSelectors
private String selectorsString; // Will inect all selectors like "seklector1.selector2.selector3"

@RequestSelectors
private List<String> selectorsList; // Will inject the list of selectors

@RequestSelectors
private String[] selectorsArray; // Will inject the array of selectors
```
Note: this annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, use the notation like `(@RequestSelectors @Named String argument)` and annotate the constructor itself with `@Inject`.

#### Injector for request suffix

The `@RequestSuffix` is used to inject a Sling request suffix. If the annotated member is of type *String* or *Object*, the string value of suffix is injected. If the annotated member is of type *Resource*, the injector will inject the corresponding JCR resource.

Note: this annotation can be used with either a field, a method, or a constructor argument. When using with a
constructor, write it like `(@RequestSuffix @Named String argument)` and annotate the constructor itself with `@Inject`.
