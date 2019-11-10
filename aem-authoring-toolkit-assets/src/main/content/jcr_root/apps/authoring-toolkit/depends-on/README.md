## DependsOn Plugin client library

Author _Alexey Stsefanovich (ala'n)_

Version _1.2.0_
 
DependsOn Plugin is a clientlib that executes defined action on dependent fields.
 
DependsOn Plugin uses data attributes for fetching expected configuration. 
To define data attribute from JCR use _**granite:data**_ sub-node under the widget node.
AEM Authoring Toolkit provides a set of annotations to use DependsOn from Java code.

DependsOn workflow consists of the following steps:

**Reference**  ─┐

**Reference**  ─── **Query***  ─── **Action*** 

**Reference**  ─┘
 
Query and Action are always a part of DependsOn plugin workflow. 
 
Action defines what the plugin should do with the dependent field (show/hide, set value, etc).
 
Query always goes with the Action and defines an expression that should be used as Action's input.
 
References are external elements whose values can be used inside of Query.
 
#### Introduction  

"DependsOn" plugin is based on the following data attributes.

For dependent field:

* **dependsOn** (`data-dependson`) - to provide Query with condition or expression for the Action.
* **dependsOnAction** (`data-dependsonaction`) - (optional) to define Action that should be executed. 
* **dependsOnSkipInitial** (`data-dependsonskipinitial`) - (optional) marker to disable initial execution.

For referenced field:

* **dependsOnRef** (data-dependsonref) - to mark a field, that is referenced from the Query.
* **dependsOnRefType** (data-dependsonreftype) - (optional) to define expected type of reference value. 

#### DependsOn Usage

##### Actions

Built-in plugin actions are:
 * `visibility` - hide the element if the query result is 'falsy'
 * `tab-visibility` - hide the tab or element's parent tab if the query result is 'falsy'
 * `set` - set the query result as field's value
 * `set-if-blank` - set the query result as field's value only if the current value is blank
 * `required` - set the required marker of the field from the query result.
 * `validate` - set the validation state of the field from the query result.

If the action is not specified then `visibility` is used by default.

##### Action Registry

Custom action can be specified using `Granite.DependsOnPlugin.ActionRegistry`.

Action should have name and function to execute. 
For example build-in `set` action is defined as follows:
```
Granite.DependsOnPlugin.ActionRegistry.ActionRegistry.register('set', function setValue(value) {
     ns.ElementAccessors.setValue(this.$el, value);
});
```

##### Reference Types
Allowed reference types:
* `boolean` - cast to boolean (according to JS cast rules)
* `boolstring` - cast as a string value to boolean (true if string cast equals "true")
* `number` - cast to number value
* `string` - cast to string

If the type is not specified manually it will be chosen automatically based on element widget type 
(see _preferableType_ in ElementsAccessor definition).

##### ElementsAccessor Registry

Registry `Granite.DependsOnPlugin.ElementAccessors` - can be used to define custom accessors of element. 
Accessor provide the information how to get/set value, set a require/visibility state or returns `preferableType` for specific type of component.

For example default accessor descriptor is defined as follows:
```
Granite.DependsOnPlugin.ElementAccessors.registerAccessor({
    selector: '*', // Selector to filter element
    preferableType: 'string',
    get: function($el) {
        return $el.val() || '';
    },
    set: function($el, value) {
        $el.val(value);
    },
    required: function($el, val) {
        $el.attr('required', val ? 'true' : null);
        $el.attr('aria-required', val ? 'true' : null);
        ns.ElementAccessors.updateValidity($el);
    },
    visibility: function ($el, state) {
        $el.attr('hidden', state ? null : 'true');
        $el.closest('.coral-Form-fieldwrapper').attr('hidden', state ? null : 'true');
        if (!state) {
            ns.ElementAccessors.clearValidity($el);
        }
    }
});
```

##### Query Syntax

Query is a plain JavaScript condition or expression. 
Any global and native JavaScript object can be used inside of Query.
We can also use dynamic references to access other fields' values.
To define a reference we should specify referenced field name in dependsOnRef attribute on it.
Then it's accessible in the query by this name via @ symbol. 

##### Query Reference Syntax

Area to find referenced field can be narrowed down by providing the Scope. 
Scope is a CSS Selector of the closest container element. 
Scope is defined in parentheses after reference name.

Examples:
* `@enableCta (coral-panel)` - will reference the value of the field marked by `dependsOnRef=enableCta` in bounds of the closest parent Panel element.
* `@enableCta (.my-fieldset)` - will reference the value of the field marked by `dependsOnRef=enableCta` in bounds of the closest parent container element with "my-fieldset" class.

"Back-forward" CSS selectors are available in the Scope syntax, i.e. we can define CSS selector to determinate parent element and then provide selector to search the target element for scope in bounds of found parent. 
Back and forward selectors are separated by '|>' combination. 

For example:
* `@enableCta (section |> .fieldset-1)` - will reference the value of the field marked by `dependsOnRef=enableCta` in bounds of element with `fieldset-1` class placed in the closest parent section element.

##### Multiple Actions
 Multiple actions with queries could be defined.
 Single query/action should be separated by ';' and placed in the same order.
 The number of actions should match the number of queries.


#### Authoring Toolkit DependsOn annotations 

* `@DependsOn` - to define single DependsOn Action with the Query. Multiple annotations per element can be used.

* `@DependsOnRef` - to define referenced element name and type. Only a single annotation is allowed. 

* `@DependsOnTab` - to define DependsOn query with `tab-visibility` action for tab.


### Examples

#### 1. Simple bindings.

Field text is shown when `checkbox` is checked

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

Field text is shown when `checkbox` is unchecked

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

#### 2. Select value binding.

Field text is shown when `selectbox` value is "Show Text"

```
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

Field text is shown when `selectbox` value is "Show Text 1" or "Show Text 2"

```
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

#### 3. Multiple field binding.

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

#### 4. Tab binding.

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
    @PlaceOnTab("tab1")
    private boolean checkbox;
     
    @DialogField
    @TextField
    @PlaceOnTab("tab2")
    private String someField;
}
```

#### 5. Scoped binding

List of items (reused fragments or MultiField), each item should have `field1` if `conditionGlobal` (globally) and `conditionItem` in current item checked.

```
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

#### 6. Temporary result

To save and use temporary result of some expression hidden unnamed field is used.

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
     
    // @DialogField is not needed, name should not be defined, field type in model not important
    @DependsOn(
        // Absolutely no need to calculate simple actions like sum separately, light operations can be used as it is, heavy thing can be declared like here
        query = "@field1 + @field2",
        // Simple set action is used
        action = "DependsOnActions.SET"
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

#### 7. Function usages

Global functions are available in the queries. (Note: only pure functions are supported, as query recalculates only on reference change)

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

#### 8. Async conditions

Async result should be stored as a temporary result with a custom action to provide the result.

```java
public class Component {
    @DialogField
    @DependsOnRef(name = "path")
    @TextField
    private String path;
     
    @DependsOn(query = "@path", action = "customAsyncAction")
    @DependsOnRef(name = "temporaryResult", type = DependsOnRefTypes.BOOLSTRING)
    @Hidden
    private int conditionGlobal;
     
    @DialogField
    @DependsOn(query = "@temporaryResult")
    @TextField
    private String field;
}
```
```
(function (Granite, $, DependsOn) {
    'use strict';
    Granite.DependsOnPlugin.ActionRegistry.ActionRegistry.register('customAsyncAction', function (path) {
         const $el = this.$el;
         $.get(Granite.HTTP.externalize(path + '/jcr:content.json')).then(
             function (data) { return data && data.result; },
             function () { return false; }
         ).then(function (res) {
             $el.attr('value', res);
             $el.trigger('change');
         });
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);
```

#### 9. Multiple actions

`text` should be shown when `checkbox1` is checked and should be required when `checkbox2` is checked

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