<!--
layout: content
title: Managing structured data with Exadel Toolbox Lists
navTitle: Etoolbox Lists
seoTitle: Etoolbox Lists - Exadel Authoring Kit
-->

Exadel Toolbox Lists, or simply *Lists*, is an accessory of the ToolKit that represents a contribution to the Exadel Toolbox package

The Lists provide a flexible way to create, store and retrieve lists of structured items, e.g. a list of HTTP status codes and their descriptions:
```
+ list
  + item1
    - statusCode = "200"
    - description = "OK"
  + item2
    - statusCode = "404"
    - description = "Not Found"
```

A List consists of a number of items. The structure and authoring dialog of every item is defined by an arbitrary AEM component (a.k.a. *Item Component*).
Each list is an AEM page, which means that it can be placed anywhere in the content structure. Lists can be created, edited, localized and published via Touch UI interface.

## Creating a new List

Lists can be created and managed either from the common Sites Console, or from the dedicated Lists Console (Tools -> EToolbox -> Lists). Click Create -> List and specify the list's *Title*, *Name* and *Item Component*.

For a new installation, there will be the only Item Component in stock. It is the *Simple List Item* - the one that consists of *jcr:title* and *value* fields to cover the most basic use cases.

If you need a more developed list item, declare it in Java the same way you would do it for a regular AEM component; then add the `@ListItem` marker annotation.

There's no limitation to the complexity of List Item. Here is how an item for storing an HTTP status code and its metadata could look like:

```java
@AemComponent(
    path = "listItems/statusCode",
    writeMode = WriteMode.CREATE, // might not be needed: see note below
    title = "Status Code List Item"
)
@Dialog
@ListItem
public class StatusCodeListItem {
    @DialogField(label = "Status Code")
    @NumberField(min = 200, max = 599)
    private int code;

    @DialogField(label = "Description")
    @TextField
    private String description;

    @DialogField(label = "Status Icon")
    @ImageUpload
    private String icon;
}
```
The List Items have a default view which enumerates all non-system properties. However, this view can be customized in several ways:
1) You can add an Item preview section to a List Item. To do this, create a new `itemPreview.html` file in your Item's folder (in our case, `/apps/listItems/statusCode`). This preview will be displayed next to the info section.
2) You can also customize the info section itself. To do this, create a new `itemInfo.html` file in your Item's folder. This view will be displayed instead of the default info section.

Note: if you create your List Item anew, and do not have special requirements regarding how it is rendered (i.e. your List Item is not a reusable component for ordinary pages), you don't need to manually create the component folder. Just add `writeMode = WriteMode.CREATE` to the `@AemComponent`. Ignore this if you are actually reusing a pre-existing component.

## Editing a List

The Lists can be edited similarly to any other page. You can change the type of *Item Component* used in a list (even after the list has been populated with data) via the page properties.

## Creating Lists in code
[ListHelper](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/core/lists/utils/ListHelper.html) is a helper class that provides the ability to create a new EToolbox List from Java code. You can create a List out of a collection of *SimpleListItem*-s, or a collection of arbitrary Sling models, or a list of `Map` instances, or else a list of Sling *Resource*-s:
```

ResourceResolver currentResourceResolver = /*... */;
String path = "/content/lists/myNewList";
List<MyModel> models = Arrays.asList(new MyModel("First"), new MyModel("Second"));

Page myNewList = ListHelper.createList(resourceResolver, path, models);

// ...

var propertyMaps = Arrays.asList(ImmutableMap.of("foo", "bar", "answer": 42), /* ... */);
Page myAnotherList = ListHelper.createList(resourceResolver, path, propertyMaps);

```
Note: when creating a list from an arbitrary Sling model, you use the model's public getters. They are expected to return values of types that are simply serialized (such as *String*, *long*, etc.). You can also use public fields.

In fact, the *ObjectMapper* from [Jackson](https://github.com/FasterXML/jackson) is used under the hood. Therefore, all the techniques relevant to serializing entities with *Jackson* are applicable here. For instance, you can skip a field or getter by adding `@JsonIgnore` to it. All other `@Json`-related annotations will also work.

Additionally, you can skip the getters or fields you don't want to be persisted in EToolbox List by:
- adding the `@Transient` annotation (from *java.beans*) to a method;
- adding the `transient` modifier to a public field.

Find more examples on creating EToolbox Lists code in [ListHelperTest](../../../core/src/test/java/com/exadel/aem/toolkit/core/lists/utils/ListHelperTest.java)


## Retrieving Lists' content programmatically

Apart from creating lists, [ListHelper](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/core/lists/utils/ListHelper.html) provides the ability to retrieve contents of any list by its path. See examples below:
```
   List<ItemModel> models = ListHelper.getList(resolver, "/content/myList", ItemModel.class);
   Map<String, String> mapping = ListHelper.getMap(resolver, "/content/myList");
```
You can find more examples in [ListHelperTest](../../../core/src/test/java/com/exadel/aem/toolkit/core/lists/utils/ListHelperTest.java)

### Populating dropdown widgets from a datasource
Exadel Toolbox Lists can be used as a data source for any widget consuming Granite datasources like in the following example:

```java
@AemComponent(
        path = "path/to/my/component",
        title = "My AEM Component"
)
@Dialog
public class MyComponent {
    @DialogField
    @Select(
        optionProvider = @OptionProvider(
            value = @OptionSource(
                value = "/content/path/to/etoolbox/list",
                textMember = "icon",
                valueMember = "code"
            ),
            sorted = true
        )
    )
    String optionList;
}
```

## Retrieving Lists' content via HTTP

Your website's authoring logic may require fetching the content of a list via HTTP — e.g., with an AJAX request. This can be achieved with the OptionSource / [OptionProvider](../dev-tools/option-provider.md) servlet. Make a GET request to `/apps/etoolbox-authoring-kit/datasources/option-provider.json?path=<path_to_EToolbox_list>`.

By default, the servlet returns a JSON array of objects with `text` and `value` properties matching the same-named attributes of list items. You will probably want to modify the mapping. To say, a typical EToolbox List makes use of `jcr:title` and `value` attributes. Add `&textMember=jcr:title` to expose _jcr:title_. Similarly, you can alter the `valueMember` parameter, add some `attributeMembers`, etc. Please remember that if an item doesn't have a non-blank mapping for both _text_ and _value_, it won't be displayed.
