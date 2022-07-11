<!--
layout: content
title: Allowed Components
order: 5
-->

## Managing allowed components on editable and static templates with AllowedChildren

Defining allowed components has always been a tiring task for developers. The XML configurations are hard to scale and manage. If you store the designs/policies in the codebase, you can use the capabilities of Exadel Toolbox Authoring Kit to define the allowed components in a uniform and flexible way.

### Defining allowed children

In order to specify a list of allowed children for a container component, use `@AllowedChildren` annotation.  There can be one or more `@AlowedChildren` annotations per component. Every `@AllowedChildren` entry describes a set of allowed components/groups and, optionally, the conditions under which it should be applied.

You can either specify a single rule that will apply everywhere the component can be inserted, or have several rules with different conditions. The rules are evaluated in the order they were specified. The first suitable rule takes effect, so you would want to specify the rules from the more specific ones to more generic ones.

A detailed description of all properties of `@AllowedChildren` annotation can be found in the [API documentation](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/policies/AllowedChildren.html). Here is a brief overview of the properties that can help you to narrow down the conditions for a rule:
- _templates_ - specify applicable templates;
- _pageResourceTypes_ - specify applicable page resource types;
- _parents_ - specify applicable parent component resource types or groups. You can also specify several "generations" of parents separating them with a space (see Example 3 below);
- _pagePaths_ - specify applicable page paths;
- _resourceNames_ - specify applicable resource subnode names.

For _templates_, _pageResourceTypes_, _parents_, _pagePaths_ and the _value_ itself, you can use wildcard symbol (`*`) to omit the beginning or the end of the path to a component or template: `/apps/acme/components/content/new_design/*`, `*/new_design/*`, `*/text`. However, such policies are hard to support and may lead to unwanted components being allowed, therefore they should be used with caution.

The final setting that can be applied to a rule is the _mode_. Mode defines whether the current rule replaces (or "overrides") the rules defined through the conventional policies for editable templates / designs, or just merges with them.

The default behavior is "override". It helps to keep the policies and the `@AllowedChildren` logic apart.

Use the _merge_ mode with caution. It allows using the policies of editable templates and the code-defined rules at the same time, and this can be a quick way to alter things. But it complicates debugging policy issues: it will be harder to guess for what exact reason a component is in or out of the "allowed" list.

### Allowed children for a top-level container

In almost every AEM page, there are containers that a developer hasn't created with one's own hands. Such are built-in `parsys`-es, `responsivegrid`-s, etc. Usually, they are situated above the user-defined components' nodes in the page's node tree. Therefore we name them "top-level containers".

There would unlikely be a dedicated component for a top-level container in the code base (unless you, e.g., introduce a descendant for `parsys` of your own). However, there is a way to define policies for top-level containers as well.

You can create (or reuse) an  `@AemComponent`-annotated class that manifests a page. This technique is described in the section on [Page properties](component-management/component-structure.md#page-properties-dialogs). Please mind that you don't necessarily need a `@Dialog`, etc. for such a class if you're not going to modify the page properties UI.
Just `@AemComponent` and then `@AllowedChildren` would be enough.

In `@AllowedChildren` you specify the node name of the appropriate top-level container. To restrict the rule to s specific page or set of pages, you can use _templates_ and _pagePaths_. Note that either _parents_ or _pageResourceTypes_ are not welcome in this case.


### Examples
Here are some examples that showcase the typical use cases.

1. Allow Text component under Columns component anywhere the Columns component is supported:
```java
@AemComponent(title = "Columns Component", path = "columns")
@AllowedChildren("/apps/acme/components/content/text")
public class ColumnsComponent {
}
```

2. Allow "General" group of components under Columns component if it is placed on the Articles template, and only Text component in all other cases:
```java
@AemComponent(title = "Columns Component", path = "columns")
@AllowedChildren(
    value = "group:General",
    templates = "/conf/acme/settings/wcm/templates/article"
)
@AllowedChildren("/apps/acme/components/content/text")
public class ColumnsComponent {
}
```

3. Allow Text component under Columns component only if the Columns component is placed inside the specified "chain" of parents:
``` text
└── /apps/acme/components/content/rootColumn (a member of "Root Components" group)
    └── /apps/acme/components/content/grandparent-component
       └── /apps/acme/components/content/parent-component
          └── /apps/acme/components/content/columns
```
```java
@AemComponent(title = "Columns Component", path = "columns")
@AllowedChildren(
    value = "/apps/acme/components/content/text",
    parents = "Group:'Root Components' */grandparent-component */parent-component"
)
public class ColumnsComponent {
}
```

4. Disallow all components under Column component
```java
@AemComponent(title = "Columns Component", path = "columns")
@AllowedChildren(value = "")
public class ColumnsComponent {
}
```

5. Allow Text component under CustomParsys component, which extends OOTB Parsys. Let this rule be added to the conventional policy for the current component
```java
@AemComponent(
        title = "Columns Component",
        path = "columns",
        resourceSuperType = "wcm/foundation/components/parsys")
@AllowedChildren(
        value = "/apps/acme/components/content/text",
        targetContainer = PolicyTarget.CURRENT,
        mode = PolicyMergeMode.MERGE
)
public class CustomParsys {
}
```

6. Allow the Text and Image components in the top-level container (OOTB Parsys or a similar species) that stores nested components in a node named "body-zone" in the Generic Page template.
```java
@AemComponent(
        title = "Generic Page",
        path = "/apps/acme/components/pages/generic-page",
        description = "Standard page for various content",
        resourceSuperType = "acme/components/pages/base")

@AllowedChildren(
        resourceNames = "body-zone",
        value = {
                "/apps/acme/components/content/text",
                "/apps/acme/components/content/image"
        })
public class GenericPage {
}

```
