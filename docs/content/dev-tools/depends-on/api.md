<!--
layout: content
title: API
order: 3
-->

## Depends On API

### Actions

Built-in plug-in actions are:
* `visibility` - hide the element if the Query result is 'falsy'. <u>This is the default action that is applied when no action is specified.</u>
* `tab-visibility` - hide the tab or the element's parent tab if the Query result is 'falsy'
* `set` - set the Query result as the field's value (undefined Query result skipped)
* `set-if-blank` - set the Query result as the field's value only if the current value is blank (undefined Query result skipped)
* `readonly` - set the readonly marker of the field from the Query result.
* `required` - set the required marker of the field from the Query result.
* `validate` - set the validation state of the field from the Query result.
* `disabled` - set the field's disabled state from the Query result.

#### Async actions

Built-in plug-in async actions:
* `fetch` - an action to set the result of fetching an arbitrary resource.
  It uses Query result as a target path to a node or property.
  The path should end with the property name or '/' to retrieve the whole node.
  The path can be relative (e.g. 'node/property' or '../../property') or absolute ('whole/path/to/the/node/property').
  _Additional parameters:_
    * `map` (optional) - function `(result: any, name: string, path: string) => any` to process result. Can be used as mapping/keys-filtering or can provide more complicated actions.
    * `err` (optional, map to empty string and log error to console by default) - function `(error: Error, name: string, path: string) => any` to process error. Can be used to map or ignore error results.
      Note: If the mapping result is `undefined` then the action will not change the current value.
    * `postfix` (optional, `.json` by default) - a string to append to the path if it is not already presented

#### Widget-specific actions

* `update-options` - changes the option set of a Granite Select component based on the path from the Query result. The path can lead to any endpoint that is supported by the [Option Provider mechanism](../option-provider.md).

### Action Registry

Custom action can be specified using `Granite.DependsOnPlugin.ActionRegistry`.

An action should have a name and function to execute.
Action names support lower-case letters, numbers and '-'. All upper-case letters will be transformed to lower case.
For example built-in `set` action is defined as follows:
```javascript
Granite.DependsOnPlugin.ActionRegistry.register('set', function setValue(value) {
    if (value !== undefined) {
        Granite.DependsOnPlugin.ElementAccessors.setValue(this.$el, value);
    }
});
```

### Reference Types

Allowed reference types:
* `boolean` - cast to boolean (according to JS cast rules)
* `boolstring` - cast as a string value to boolean (true if string cast equals "true")
* `number` - cast to number value
* `string` - cast to string
* `json` - parse JSON string

If the type is not specified manually, it will be chosen automatically based on the type of element widget
(see _preferableType_ in the ElementsAccessor definition).

In any other case (e.g. if the type is `any`), no cast will be performed.

Note: If you use a Hidden field to save a temporary boolean result, use `boolstring` reference type in order to retrieve it.

### ElementsAccessor Registry

Registry `Granite.DependsOnPlugin.ElementAccessors` - can be used to define the custom accessors of an element.
The accessor provides information on how to get/set values, set a require/visibility/disabled state, or returns `preferableType` for the specific type of component.

For example, a default accessor descriptor is defined as follows:
```javascript
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
        Granite.DependsOnPlugin.ElementAccessors.updateValidity($el);
    },
    visibility: function ($el, state) {
        $el.attr('hidden', state ? null : 'true');
        $el.closest('.coral-Form-fieldwrapper').attr('hidden', state ? null : 'true');
        if (!state) {
            Granite.DependsOnPlugin.ElementAccessors.clearValidity($el);
        }
    },
    disabled: function ($el, state) {
        $el.attr('disabled', state ? 'true' : null);
        $el.closest('.coral-Form-fieldwrapper').attr('disabled', state ? 'true' : null);
        if (!state) {
            Granite.DependsOnPlugin.ElementAccessors.clearValidity($el);
        }
    }
});
```

### Query Syntax

A Query is a plain JavaScript condition or expression.
Any global and native JavaScript object can be used inside a Query.
You can also use dynamic references to access other fields' values.
In order to define a reference, the referenced field's name should be specified in a `dependsOnRef` attribute.
Then the reference will be accessible in the Query using the `@` or `@@` symbol and reference name.

#### Using Semicolons in DependsOn Queries

DependsOn queries are always treated as a single JavaScript expression and never as multiple statements in one line.
Semicolon symbols (`;`) within a DependsOn Query must be escaped.

If you add a Query via a Java annotation, semicolons will be escaped automatically:
```java
class MyComponent {
    @DependsOn(query = "@field === ';'")
    private String field1;
}
```
But, if you write directly to XML or HTML, you should escape them by hand:
```xml
<granite:data dependson="@field === '\\;'"/>
```

If you need to actually execute several JavaScript statements within a DependsOn,
you can do this by wrapping them in a function call:
```java
class MyComponent {
    @DependsOn(query = "function(){ var a = @field1 + @field2; return a * a < 4; }()")
    private String field;
}
```

Be aware that it is still better to move complex structures to a standalone client library
```javascript
  // project-clientlib.js
  window.MyUtils = window.MyUtils || {};
  window.MyUtils.fieldAccepted = function (field1, field2) {
      var a = field1 + field2;
      return a * a < 4;
  };
```
```java
class MyComponent {
    @DependsOn(query = "MyUtils.fieldAccepted(@field1, @field2)")
    private String field;
}
```

#### Query Reference Syntax

There are two versions of references available in the Queries:
- 'Single' reference: `@reference`. 'Single' reference starts from the `@` symbol in the Query, it allows you to access a defined field value.
  'Single' reference should reference an existing field and will not be reattached on dynamic DOM change.
- 'Multiple' reference: `@@reference`. Starts from double `@` symbols. Allows you to access a group of field values marked by the same reference name.
  'Multiple' reference always returns an array in the Query.

Note: 'multiple' reference triggers Query update on any group update: changing some of group’s fields value or adding/removing a referenced field.

'this' is a reserved word for reference names. Using `@this` you can retrieve the value of the current element. In this case there is no need to specify a reference name unless you want to use it outside the current element.

The area to find a referenced field can be narrowed down by providing the Scope.
The Scope is a CSS Selector of the closest container element.
The Scope is defined in parentheses after the reference name.

Examples:
* `@enableCta (coral-panel)` - will reference the value of the field marked by `dependsOnRef=enableCta` in bounds of the closest parent Panel element.
* `@enableCta (.my-fieldset)` - will reference the value of the field marked by `dependsOnRef=enableCta` in bounds of the closest parent container element with "my-fieldset" class.
* `@@enableCta (coral-multifield)` - will reference all values of the fields marked by `dependsOnRef=enableCta` in bounds of the closest multifield.

"Back-forward" CSS selectors are available in the Scope syntax, i.e. we can define the CSS selector to determine the parent element and then provide a selector to search the target element for scope in bounds of the found parent.
Back and forward selectors are separated by '|>'.

For example:
* `@enableCta (section |> .fieldset-1)` - will reference the value of the field marked by `dependsOnRef=enableCta` in bounds of element with `fieldset-1` class placed in the closest parent section element.

### Multiple Actions
Multiple actions with Queries can be defined.
Queries/Actions should be separated by ';' and placed in the same order.
The number of Actions should match the number of Queries.

Static action’s params can be passed through data attributes with the following syntax:
- for a single and first action `data-dependson-{action}-{paramName}` can be easily accessed and used from action,
  e.g. `data-dependson-validate-msg` will be used by `validate` action as invalid state message
- for multiple actions of the same type additional actions params should end with `-{index}` (1 for the second action, 2 for the third).
  e.g. `data-dependson-validate-msg-1` will be used by second `validate` action as invalid state message
