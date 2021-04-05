[Main page](../../README.md)

# Lists
AEM Authoring Toolkit Lists provide a flexible way to create, store and retrieve lists of structured items, e.g. a list of HTTP status codes and their descriptions:
```
+ list
  + item1
    - statusCode = "200"
    - description = "OK"
  + item2
    - statusCode = "404"
    - description = "Not Found"
```

A list consists of a number of items, the structure and authoring dialog of every item is defined by an arbitrary AEM component (a.k.a. Item Component).
Each list is an AEM page, which means that it can be placed anywhere in the content structure; lists can be created, edited, localized and published via TouchUI interface.


## Usage

#### Creating a new list
AAT Lists can be created and managed either from Sites Console, or from Lists Console (Tools -> AAT Lists -> AAT List (todo: change to agreed naming)). Click Create -> List and specify list's Title, Name and Item Component.
"Simple List Item" component is provided out-of-the-box and consists of "jcr:title" and "value" fields. In order to add a new Item Component to the dropdown, add `@ListItem` annotation to this component.

```java
@Dialog(
    name = "content/listItemComponent",
    title = "List Item"
)
@ListItem
public class ListItemComponent {
  ...
}
```
#### Editing lists
AAT Lists can be edited similarly to any other page. You may change the type of Item Component used in this list (even after the list has been populated with data) via page properties.

#### Retrieving lists' content programmatically
[ListsHelper](../../core/src/main/java/com/exadel/aem/toolkit/bundle/lists/util/ListsHelper.java) is a helper class that provides the ability to retrieve any list by its path. See examples below:
```java
   List<ItemModel> models = CustomListsHelper.getList(resolver, "/content/myList", ItemModel.class);
   Map<String, String> mapping = CustomListsHelper.getMap(resolver, "/content/myList");
```
You can find more examples in [ListsHelperTest](../../core/src/test/java/com/exadel/aem/toolkit/bundle/lists/util/ListsHelperTest.java)

#### Populating dropdown widgets from a datasource.
AAT Lists can be used as a data source for any widget consuming granite datasources.
(todo: add an example of @Datasource + Lists)
