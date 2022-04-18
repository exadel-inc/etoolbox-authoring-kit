<!--
layout: content
title: Usage
seoTitle: Usages - Exadel Authoring Kit
order: 2
-->
## Using Depends On

### Data Attributes

The DependsOn plug-in is based on the following data attributes:

For dependent fields:

* `data-dependson` - to provide Query with a condition or expression for the Action
* `data-dependsonaction` - (optional) to define the Action that should be executed
* `data-dependsonskipinitial` - (optional) a marker to disable initial execution

For referenced fields:

* `data-dependsonref` - to mark a field that is referenced in the Query.
* `data-dependsonreftype` - (optional) to define the expected type of reference value.
* `data-dependsonreflazy` - (marker) an attribute to mark a reference as lazy. In this case, DependsOn will not observe rapid events like `input`.

### EToolbox Authoring Kit Annotations

* `@DependsOn` - to define a single DependsOn Action with the Query. Multiple annotations per element can be used.
* `@DependsOnRef` - to define a referenced element name and type. Only a single annotation is allowed.
* `@DependsOnTab` - to define a DependsOn Query with tab-visibility Action for a tab.


### Debug Info

DependsOn produces three types of debug notifications:

- Critical errors: DependsOn will throw an Error on a configuration mismatch (like unknown action name, illegal custom accessor registration, etc.)
- Error messages: not blocking runtime messages (Query evaluation errors, unreachable references, etc.)
- Warn messages: potentially unexpected results warning or deprecated functionality

The following expression can be evaluated in the browser console to see
current DependsOn debug information (references, actions): `Granite.DependsOnPlugin.debug()`
