[Main page](../../README.md)

## Managing structured data with EToolbox Lists

EToolbox Lists, or simply *Lists*, is an accessory of the ToolKit that represents a contribution to the Exadel Toolbox package

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

A List consists of a number of items, the structure and authoring dialog of every item is defined by an arbitrary AEM component (a.k.a. *Item Component*).
Each list is an AEM page, which means that it can be placed anywhere in the content structure. Lists can be created, edited, localized and published via Touch UI interface.

### Creating a new List

Lists can be created and managed either from the common Sites Console, or from the dedicated Lists Console (Tools -> EToolbox -> EToolbox Lists). Click Create -> List and specify the list's *Title*, *Name* and *Item Component*.

For a new installation, there will be the only Item Component in stock. It is the *Simple List Item* - the one that consists of *jcr:title* and *value* fields to cover the most basic use cases.

If you need a more developed list item, declare it in Java the same way you would do it for a regular AEM component; then add the `@ListItem` marker annotation.

There's no limitation to the complexity of List Item. Here is how an item for storing an HTTP status code and its metadata could look like:

```java
@AemComponent(
    path = "listItems/statusCode",
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
The List Items have a default view which enumerates all the available properties. However, this can be customized. You can add an Item preview section to a List Item. To do this, create a new `itemPreview.html` file in your Item's folder (in our case, `/apps/listItems/statusCode`). This preview will be displayed next to the properties.

### Editing a List

The Lists can be edited similarly to any other page. You can change the type of *Item Component* used in a list (even after the list has been populated with data) via the page properties.

### Retrieving Lists' content programmatically

[ListHelper](../../core/src/main/java/com/exadel/aem/toolkit/core/lists/utils/ListHelper.java) is a helper class that provides the ability to retrieve contents of any list by its path. See examples below:
```
   List<ItemModel> models = CustomListsHelper.getList(resolver, "/content/myList", ItemModel.class);
   Map<String, String> mapping = CustomListsHelper.getMap(resolver, "/content/myList");
```
You can find more examples in [ListHelperTest](../../core/src/test/java/com/exadel/aem/toolkit/core/lists/utils/ListHelperTest.java)

#### Populating dropdown widgets from a datasource.
EToolbox Lists can be used as a data source for any widget consuming Granite datasources like in the following example:

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

