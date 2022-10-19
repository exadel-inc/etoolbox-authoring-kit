<!--
layout: content
title: DependsOn Examples
navTitle: Examples
seoTitle: DependsOn Examples - Exadel Authoring Kit
order: 4
-->

## Simple bindings

Field text is shown when the `checkbox` is checked

```java
public class Component {
    @DependsOnRef(name = "checkbox")
    @DialogField
    @Checkbox
    private boolean checkbox;


    @DependsOn(query = "@checkbox")
    @DialogField
    @TextField
    private String text;
}
```

Field text is shown when the `checkbox` is unchecked

```java
public class Component {
    @DependsOnRef(name = "checkbox")
    @DialogField
    @Checkbox
    private boolean checkbox;


    @DependsOn(query = "!@checkbox")
    @DialogField
    @TextField
    private String text;
}
```

## Select value binding

Field text is shown when the `selectbox` value is "Show Text"

```java
public class Component {
    @DependsOnRef(name = "selectbox")
    @DialogField
    @Select(
        options = {
            @Option(text = "Hide Text", value = "hideval"),
            @Option(text = "Show Text", value = "showval")
        }
    )
    private String select;

    @DependsOn(query = "@selectbox === 'showval'")

    @DialogField
    @TextField
    private String text;
}
```

Field text is shown when the `selectbox` value is "Show Text 1" or "Show Text 2"

```java
public class Component {
    @DependsOnRef(name = "selectbox")
    @DialogField
    @Select(
        options = {
            @Option(text = "Hide Text", value = "hideval"),
            @Option(text = "Show Text 1", value = "showval1"),
            @Option(text = "Show Text 2", value = "showval2")
        }
    )
    private String select;

    @DependsOn(query = "@selectbox === 'showval1' || @selectbox === 'showval2'")
    // or @DependsOn(query = "['showval1', 'showval2'].indexOf(@selectbox) !== -1")
    //or any other way to define condition with JS
    @DialogField
    @TextField
    private String text;
}
```

## Multiple field binding

Field `text3` is shown when `text1` is equal to `text2`

```java
public class Component {
    @DependsOnRef(name = "text1")
    @DialogField
    @TextField
    private String text1;


    @DependsOnRef(name = "text2")
    @DialogField
    @TextField
    private String text2;


    @DependsOn(query = "@text1.toLowerCase() === @text2.toLowerCase()")
    @DialogField
    @TextField
    private String text3;
}
```

## Tab binding

`tab2` should be shown when `checkbox1` is checked

```java
@Dialog(
    name = "exampleComponent",
    title = "Example",
    tabs = {
        @Tab(title = "tab1"),
        @Tab(title = "tab2")
    }
)
@DependsOnTab(tabTitle = "tab2", query = "@checkbox")
public class Component {
    @DialogField
    @DependsOnRef(name = "checkbox")
    @Checkbox
    @Place("tab1")
    private boolean checkbox;

    @DialogField
    @TextField
    @Place("tab2")
    private String someField;
}
```

## Scoped binding

List of items (reused fragments or a MultiField). Each item should show `field1` if the `conditionGlobal` (globally) and `conditionItem` in the current item are checked.

```java
public class Component {
    @DialogField
    @DependsOnRef(name = "conditionGlobal")
    @Checkbox
    private boolean conditionGlobal;


    @MultiField(field = Component.Item.class)
    @FieldSet
    private List<Item> items;

    public static class Item {
        @DialogField
        @DependsOnRef(name = "conditionItem")
        @Checkbox
        private boolean conditionItem;

        @DialogField
        // We should define the scope for @conditionItem reference as each MultiField item contains a reference called 'conditionItem'
        @DependsOn(query = "@conditionItem(coral-multifield-item) && @conditionGlobal")
        @TextField
        private String field1;
    }
}
```

## Temporary result

An unnamed hidden field can be used to save the temporary result of an expression.

```java
public class Component {
    @DependsOnRef(name = "field1", type = DependsOnRefTypes.NUMBER)
    @DialogField
    @NumberField
    private int field1;

    @DependsOnRef(name = "field2", type = DependsOnRefTypes.NUMBER)
    @DialogField
    @NumberField
    private int field2;

    // @DialogField is not needed, the name should not be defined, field type in the model not important
    @DependsOn(
        // Absolutely no need to calculate simple actions like sum separately, light operations can be used as it is, heavy thing can be declared like here
        query = "@field1 + @field2",
        // Simple set action is used
        action = DependsOnActions.SET
    )
    @DependsOnRef(
        // Here we add a name to our expression
        name = "sum",
        // As usual reference
        type = DependsOnRefTypes.NUMBER
    )
    @Hidden
    private int conditionGlobal;

    @DialogField
    @DependsOn(query = "@sum > 0")
    @TextField
    private String field3;
}
```

## Query function usage

Global functions are available in the Queries (note: it is recommended to use 'pure' functions because the query is recalculated on each reference change and side effects may produce unexpected results).

```java
    public class Component {
    @DialogField
    @DependsOn(query = "ProjectJSUtils.checkSomething(@refField1, @refField2)")
    @TextField
    private String field1;

    @DialogField
    @DependsOn(query = "Math.max(@refField1, @refField2) < 10")
    @TextField
    private String field2;
}
```

## Async conditions

If you use a custom action that provides the result asynchronously, it should be stored in a separate hidden field and then used in other Queries.

```java
public class Component {
    @DependsOn(query = "", action = "customAsyncAction")
    @DependsOnRef(name = "temporaryResult", type = DependsOnRefTypes.BOOLSTRING)
    @Hidden
    private String tmp;

    @DialogField
    @DependsOn(query = "@temporaryResult")
    @TextField
    private String field;
}
```
```javascript
(function (Granite, $, DependsOn) {
    'use strict';
    Granite.DependsOnPlugin.ActionRegistry.register('customAsyncAction', function () {
        setTimeout(() => DependsOn.ElementAccessors.setValue(this.$el, 'async value'));
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);
```

## Multiple actions

`text` should be shown when `checkbox1` is checked and should be required when `checkbox2` is checked.

```java
public class Component {
    @DependsOnRef(name = "checkbox1")
    @DialogField
    @Checkbox
    private boolean checkbox1;

    @DependsOnRef(name = "checkbox2")
    @DialogField
    @Checkbox
    private boolean checkbox2;

    @DependsOn(query = "@checkbox1") // action 'visibility' is default
    @DependsOn(query = "@checkbox2", action = DependsOnActions.REQUIRED)
    @DialogField
    @TextField
    private String text;
}
```

## Custom validation
DependsOn allows you to simply validate field values.
Here is an example of character count validation:
```java
public class Component {
    @DependsOn(query = "@this.length > 5", action = DependsOnActions.VALIDATE, params = {
       @DependsOnParam(name = "msg", value = "Limit exceeded")
    })
    @DialogField
    @TextField
    private String text;
}
```

## Group references

This example allows you to select `active` for only one item in multifield.
```java
public class MultifieldItem {
    @DependsOnRef(name = "active")
    @DependsOn(
            action = DependsOnActions.DISABLED,
            // We disable checkbox if it is not selected but some of checkboxes with reference name 'active' are selected
            query = "!@this && @@active.some((val) => val)"
    )
    @DialogField
    @Checkbox
    private boolean active;

    // ...
    // other fields
}
```

One of the ways to validate min and max multifield items count (by 2 min and 5 max in the current example):
```java
public class Component {

    @DependsOn(action = DependsOnActions.VALIDATE, query = "@@item(this).length >= 2 && @@item(this).length <= 5")
    @MultiField(field = Component.Item.class)
    @FieldSet
    private List<Item> items;

    public static class Item {
        @DialogField
        @DependsOnRef(name = "item")
        @Checkbox
        private boolean firstItem;

        // ...
    }
}
```

## Multifield reference

Multifield reference has two properties:
- length - count of multifield items
- isEmpty - _true_ if there are no items

Another way to validate min and max multifield items count:
```java
public class Component {

    @DependsOn(action = DependsOnActions.VALIDATE, query = "@this.length >= 2 && @this.length <= 5")
    @MultiField(field = Item.class)
    @DialogField
    @ValueMapValue
    private String[] items;

    public static class Item {
        @TextField
        @DialogField
        public String item;
    }
}
```

Show `multifield2` if `multifield1` is not empty and vice versa using multifield reference's property isEmpty:
```java
public class Component {

    @DependsOnRef(name = "multifield1")
    @MultiField(field = Item.class)
    @DialogField
    @ValueMapValue
    private String[] multifield1;

    @DependsOn(query = "!@multifield1.isEmpty")
    @MultiField(field = Item.class)
    @DialogField
    @ValueMapValue
    private String[] multifield2;

    public static class Item {
        @TextField
        @DialogField
        public String item;
    }
}
```

## Fetch action
The `fetch` action provides easy access to parent nodes' properties.

The example below shows how to set an 'opaque' option only if the 'bg' option of the parent component is not blank.
```java
public class Component {
        @Hidden
        @DependsOn(action = DependsOnActions.FETCH, query = "'../../bg'")
        @DependsOnRef(name = "parentBg")
        private String parentBg;

        @DialogField
        @TextField
        @DependsOn(query = "!!@parentBg")
        private String opaque;
}
```

The `fetch` action has short-term caching, so multiple properties will be requested once without any performance loss.
 ```java
 public class Component {
         @Hidden
         @DependsOn(action = DependsOnActions.FETCH, query = "'../../field1'")
         @DependsOnRef(name = "parentProperty1")
         private String parentProperty1;

         @Hidden
         @DependsOn(action = DependsOnActions.FETCH, query = "'../../field2'")
         @DependsOnRef(name = "parentProperty2")
         private String parentProperty2;
 }
 ```

`map` acton param can be used to process results.
The example below retrieves the parent component's title and type in a special format.
 ```java
 public class Component {
         @Hidden
         @DependsOn(action = DependsOnActions.FETCH, query = "'../../'", params = {
            @DependsOnParam(name = "map", value = "(resource) => resource.name + ' (' + resource.type + ')'")
         })
         @DependsOnRef(name = "parentHeading")
         private String parentHeading;

         @Heading
         @DependsOn(action = "set", query = "@parentHeading")
         private String parentComponentHeading;
 }
 ```

## Alert accessors

DependsOn provides the ability to conditionally change any property of the Alert widget:
- text;
- title;
- size;
- variant.

Setting Alert's text is done the same way as setting the value of other widgets.
If you want to set multiple properties at once, use a JSON object (see the example below).

You can also reference alert widgets. Alert reference is an object that provides the alert's title and text.
```java
public class Component {

    @DialogField(label = "Set alert text")
    @TextField
    @DependsOnRef
    private String textSetter;

    @DialogField(label = "Set alert size")
    @Select(options = {
        @Option(text = "Small", value = "S"),
        @Option(text = "Large", value = "L")
    })
    @DependsOnRef
    private String sizeSetter;

    @DialogField
    @Alert(text = "2", variant = StatusVariantConstants.WARNING)
    @DependsOnRef
    @DependsOn(query = "\\{'text': @textSetter, 'size': @sizeSetter\\}", action = DependsOnActions.SET)
    // @DependsOn(query = "@textSetter", action = DependsOnActions.SET) //works as well
    private String alert;

    @DialogField(label = "Get alert text")
    @TextField
    @DependsOn(query = "@alert.text", action = DependsOnActions.SET)
    private String alertGetter;
}
```

<hr/>
<h2 id="see-also" class="h3">See also</h2>

- [Feeding data to selection widgets with OptionProvider](../option-provider.md)
