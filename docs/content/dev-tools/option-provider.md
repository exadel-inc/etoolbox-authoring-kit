<!--
layout: content
title: Option Provider
seoTitle: Option Provider - Exadel Authoring Kit
order: 3
-->
## Feeding data to selection widgets with OptionProvider

Several Granite/Touch UI components, such as _RadioGroup_ or _Select_, facilitate selecting from a set of options. Traditionally, the options are either inlined (the ToolKit offers its `@RadioButton` and `@Option` annotations for that) or supplied via a datasource. Both ways have their limitations; the in-line options are not dynamic and potentially lead to a lot of copy-pasting across components, while the _datasource_ pattern requires creating a datasource servlet for every occasion.

The ToolKit is bundled with the _OptionProvider_ subsystem that aims at streamlining the usage of dynamic options without programming overhead.

The _OptionProvider_ is capable of delivering options in two modes; for a static Granite component it serves as a conventional _datasource_. Also it has a JSON-supplying servlet that allows for retrieving and updating options dynamically even after a Granite UI has already been rendered.

The options managed by _OptionProvider_ can originate from:

-   a dedicated structured data page, such as an EToolbox List or an ACS Commons List;
-   an arbitrary JCR node with children (each option will then be represented by a single child node);
-   a tag folder;
-   a Java class containing a set of constants, or an enum;
-   a static list of values (in a string array that can be made constant and shared across the project).

Moreover, a single _OptionProvider_ can join several data sources, merge and sort their options in a common sequence, enjoy having a "primary" and "fallback" option source, etc.

#### Static OptionProvider setup

For a static Granite UI component, OptionProvider is set up via a property of such annotations as `ButtonGroup`, `@RadioGroup`, or `@Select` (see the samples below):

```java
@AemComponent(
        path = "path/to/my/component",
        title = "My AEM Component"
)
@Dialog
public class MyComponent {
    @DialogField
    @RadioGroup(
        buttonProvider = @OptionProvider(@OptionSource(
            value = "/path/to/tag/folder",
            textMember = "jcr:title",
            valueMember = "name",
            attributeMembers = {"first", "second"},
            textTransform = "capitalize")
    ))
    String tag;
}

/* ...Elsewhere in the code */

@AemComponent(
        path = "path/to/my/other/component",
        title = "My Other AEM Component"
)
@Dialog
public class MyComponent {
    @DialogField
    @Select(
        optionProvider = @OptionProvider(
            value = {
                @OptionSource(
                    value = "/content/path/to/acs/list",
                    fallback = "https://acme.com/apis/sample.json/data/path"),
                @OptionSource(
                    value = "/content/path/to/etoolbox/list",
                    fallback = "/content/path/to/node",
                    textMember = "title",
                    valueMember = "descr"),
            },
            prepend = "None:none",
            selectedValue = "none",
            sorted = true
        )
    )
    String optionList;
}
```

`@OptionProvider` annotation has the following properties:

_value_ - contains one or more `@OptionSource` objects, each referring to a single data source (see below);

_prepend_ - if specified, defines one or more extra options that will be inserted in the beginning of the option list independently of items acquired via the option source(s). This may be a kind of "none" or "default" option. Each extra option string must consist of a label and a value separated with a colon (`:`). A value may be an empty string, in which case the option ends with the `:` sign. If a label or a value itself must contain a colon, it can be escaped with `\`. If an option with a similar _value_ is already present, the extra option will not be added;

_append_ - same as _prepend_, but the extra item(s) are appended to the option list. A valid list may consist of only prepended and/or appended options without the "external" part;

_exclude_ - if specified, defines one or more options (coming from an external source) that should be skipped for the current component. A string passed must match either the option's value or text. The matching is case-insensitive. The wildcard symbol (*) can be used in matching strings;

_selectedValue_ - if set to a string that matches the value or the label of one of the datasource options, this option will be rendered as selected by default;

_sorted_ - if set to true, options will be sorted in their labels' alphabetical order regardless of the order they arrived from JCR. However, the _prepended_ and _appended_ options will appear in the order they were specified by the developer and will remain at the beginning and the end, respectively.

Every `@OptionSource` object can be specified with the following properties:

_value_ - defines the path to a List-like structure, a node tree, a tag folder, or else an URL of an HTTP server that outputs options in JSON format, or a fully qualified name of a Java class (see below).

Plain paths and _path references_ are supported. I.e. if a value is presented like `/typical/jcr/path`, this exact path will be looked for. However, if given in the `/some/node@attr` format, the _attr_ attribute will be retrieved from _/some/node_, and its value will be then assumed to be the "true" path.

_enumeration_ - stores a reference of a Java class containing constants or an _Enum_. This property has a lower priority than _value_, therefore, if both are set, only _value_ will be taken into account.

_textMember_ - if specified, defines the attribute of a JCR node or a public method/field name of an Enum  to be rendered as RadioButton's or Select's _label_.
<br>Defaults are the _"jcr:title"_ attribute for a JCR node and the _.name()_ method of an Enum class;

_valueMember_ - if specified, defines the attribute of a JCR node or a public method name of an Enum class to be rendered as RadioButton's or Select's _value_.
<br>If the reserved token `@name` is specified, the node name will be used for value.
<br>If `@id` is specified, the _tag node name with tag namespace_ (for a _cq:Tag_ node) or a node name (otherwise) will be used as the value. This is particularly useful for tag listings.
<br>Default is the _"value"_ attribute for ordinary JCR nodes, _.toString()_ method for an Enum class and the node name for tags;

_attributeMembers_ - if specified, defines one or more attributes of a node or public method names of an Enum class to be rendered as HTML attributes of the corresponding Granite UI entities. For example, `attributeMembers = "some-jcr-attribute"` will be rendered as `<coral-select-item data-some-jcr-attribute="literal_value_of_this_attribute">` in HTML;

_attributes_ - if specified, defines one or more static values to be rendered as HTML attributes of the corresponding Granite UI entities. For example, `attributes = "some-attribute:some-value"` argument will be rendered as `<coral-select-item data-some-attribute="some-value">` in HTML.
<br>Each attribute must consist of a key and a value separated with colon (`:`). If a key or a value itself must contain a colon, it can be escaped with `\`.

_textTransform_ - if specified, defines the way the <u>label</u> will be transformed before rendering;

_valueTransform_ - if specified, defines the way the <u>value</u> will be transformed before rendering;

_isFallback_ - determines that the current `@OptionSource` is only used if other option source entries yielded no results. It is also used if it is the only option source. This option is useful, e.g., when you have a component with an authorable path to an option source. As the component is just created, the path will probably be empty. But still, there will be a possibility to display some options retrieved via the fallback source.

#### Working with HTTP endpoints

Apart from a JCR path, `@OptionSource` allows specifying a common network URL (note: must be a complete URL string parseable with `new URL("...")`. The content reached via the URL is expected to be a JSON entity. A JSON array becomes the list of options. A JSON node that has children produces the list of options from the enumeration of child nodes as well much the same way as a JCR resource with children.

If the JSON structure is such that the required array is nested deeper than the "root" node, you can add a "path" within the url like `http://acme.com/apis/sample.json/internal/path` The "path" is defined similar to a Sling suffix: it is the trailing part of the URL after the _.json/_ extension.

There is the possibility to add authentication info to the URL like in the following example: `http://admin:admin@localhost:4502/my/service.json`. The authentication info is converted into a Basic auth request header and sent with the request. This feature is mainly for the testing and debugging purposes. You should not use it when calling a 3rd-party API.

#### Working with Enums

`@OptionSource` supports usage of Java enums as well as ordinary Java classes that contain a collection of constants. Take a look at the example below:

```java

@AemComponent(
    path = "path/to/my/component",
    title = "My AEM Component"
)
@Dialog
public class MyComponent {
    @DialogField
    @RadioGroup(
        buttonProvider = @OptionProvider(
                @OptionSource(
                        enumeration = MyEnum.class,
                        valueMember="getInteger",
                        attributeMembers="toString")
        )
    )
    String radio;
}

public enum MyEnum {
    FIRST, SECOND, THIRD;
    public int getInteger() {
        //...
    }
}
```
By default, `@OptionProvider` uses the return value of the `.name()` method as the text source, and the value of the `.toString()` as the source for values. No Granite attributes are added by default.

You can redefine this via the _textMember_, _valueMember_, and _attributeMembers_ properties. E.g. if the enum you want to use has the _.getInteger()_ method, you may specify it like `@OptionSource(enumeration = MyEnum.class, valueMember="getInteger")`. Besides, you can, for example, ensure that the value of _.toString()_ is rendered as an HTML attribute by specifying `attributeMembers="toString"`.

#### Working with Java classes holding constants

Apart from an enum, you can make an "ordinary" Java class work as the source of options if it contains `public static final` fields. Names of such fields will become option titles, and the stringified values (`String.valueOf(MY_CONSTANT)`) will become option values. You are able to select only some of the available fields with `@OptionSource(exclude=...)`.

More interestingly, there is a way to "merge" constants into pairs in the way that one constant will manifest an option title and another - the value. Indeed, many AEM constants classes follow this pattern:
```java
public class ColorConstants {
    public static final String LABEL_RED = "Red";
    public static final String VALUE_RED = "#ff0000";
    public static final String LABEL_GREEN = "Green";
    public static final String VALUE_GREEN = "#00ff00";
    public static final String BACKGROUND_NAME_BLACK = "Black";
    public static final String BACKGROUND_VALUE_BLACK = "#000";
}
```
To handle that pattern, you may reuse the _textMember_ and _valueMember_ in a bit unusual way. Think of them as not just literals but masks. E.g., the following code:
```
@DialogField
@Select(
    optionProvider = @OptionProvider(
            @OptionSource(
                    enumeration = ColorConstants.class,
                    textMember="LABEL_*",
                    valueMember="VALUE_*")
    )
)
String colors;
```
This code will make `@OptionProvider` look for all the constants whose names match the _"LABEL_xxxx"_ pattern and then for those that follow _"VALUE_xxxx"_. The subsets of constants are merged: "red" goes to "red", etc. In this particular case we receive a list of two option: `Red:#ff0000` and `Green:#00ff00`.

Note that the "backgrounds" are not included in the list, as they do not correspond to the provided mask. You can introduce a separate _Select_, this time bor backgrounds, and populate it with `@OptionSource(enumeration = ColorConstants.class, textMember="BACKGROUND_NAME_*", valueMember="BACKGROUND_VALUE_*")`.

#### Dynamic option change

Because an `@OptionProvider` supports _path references_ apart from regular paths, the setting that says "where to look for the path" can be stored in a dialog field other than the one that actually deals with paths.

Therefore, it must be possible to dynamically respond to a _path reference_ change. In the real world, it may look like the following. Imagine there is a dialog field (say, a path picker) that allows you to select a data source (say, an EToolbox List). Below is a select dropdown with options coming from the Exadel Toolbox List selected in the above path picker.

Here's how it may look in Java code:

```java
@AemComponent(
        path = "path/to/my/component",
        title = "My AEM Component"
)
@Dialog
public class MyComponent {
    private static final String PATH_REFERENCE = "@path";

    @DialogField(label = "Select option list")
    @PathField(rootPath = "/etc/tags")
    @DependsOnRef
    private String path;

    @DialogField(label = "Select option")
    @Select(optionProvider = @OptionProvider(@OptionSource(PATH_REFERENCE))) // @path refers to the attribute named "path" in the same JCR node
    @DependsOn(
        query = PATH_REFERENCE,
        action = DependsOnActions.UPDATE_OPTIONS,
        params = {
            @DependsOnParam(name = "sorted", value = "true"),
            @DependsOnParam(name = "append", value = "None:none")
        }
    )
    private String selectedOption;
}
```

The facility that makes it possible to dynamically update selectable options is the _DependsOn_ action _"update-options"_ (see more on DependsOn actions [here](./depends-on/api.md)). It accepts any of the conventional _OptionProvider_ params described above in its `params` collection.

### See also

[Programming dynamic dialog behavior: DependsOn plugin client library](./depends-on/introduction.md)
