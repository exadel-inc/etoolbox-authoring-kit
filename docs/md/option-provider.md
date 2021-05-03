[Main page](../../README.md)

## Feeding data to selection widgets with OptionProvider

Several Granite/Touch UI components, such as *RadioGroup* or *Select*, facilitate selecting from a set of options. Traditionally, the options are either inlined (the ToolKit offers its `@RadioButton` and `@Option` annotations for that) or supplied via a datasource. Both ways have their limitations; the in-line options are not dynamic and potentially lead to a lot of copy-pasting across components, while the *datasource* pattern requires creating a datasource servlet for every occasion.

The ToolKit is bundled with the *OptionProvider* subsystem that aims at streamlining the usage of dynamic options without programming overhead.

The *OptionProvider* is capable of  delivering options in two modes; for a static Granite component it serves as a conventional *datasource* and above it has a JSON-supplying servlet that allows for retrieving and updating options dynamically even after a Granite UI has already been rendered.

The options managed by *OptionProvider* can originate from:
- a dedicated structured data page, such as an EToolbox List or an ACS Commons List;
- an arbitrary JCR node with children (each option will then be represented by a single child node);
- a tag folder;
- a static list of values (in a string array that can be made constant and shared across the project).
  Moreover, a single *OptionProvider* can join several data sources, merge and sort their options in a common sequence, enjoy having a "primary" and "fallback" option source, etc.

#### Static OptionProvider setup

For a static Granite UI component, OptionProvider is set up via a property of `@RadioGroup` or `@Select` (see the samples below):

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
                @OptionSource(value = "/content/path/to/acs/list"),
                @OptionSource(
                        value = "/content/path/to/etoolbox/list",
                        fallbackPath = "/content/path/to/node", textMember = "title", valueMember = "descr"),
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

*value* - contains one or more `@OptionSource` objects each referring to a single data source (see below);

*prepend* - if specified, defines one or more extra options that will be inserted in the beginning of the option list independently of items acquired via the option source(s). This may be a kind of "none" or "default" option. Each extra option string must consist of a label and a value separated with a colon (`:`). A value may be an empty string, in which case the option ends with the `:` sign. If a label or a value itself must contain a colon, it can be escaped with `\`. If an option with a similar *value* is already present, the extra option will not be added;

*append* - same as *prepend*, but the extra item(s) are appended to the option list. A valid list may consist of only prepended and/or appended options without the "external" part;

*exclude* - if specified, defines one or more options (coming from an external source) that should be skipped for the current component. A string passed must match either the option's value, or text. The matching is case-insensitive. The wildcard symbol (*) can be used in matching strings;

*selectedValue* - if set to a string that matches the value or the label of one of the datasource options, this option will be rendered as selected by default;

*sorted* - if set to true, options will be sorted in their labels' alphabetical order regardless of the order they arrived from JCR. However, the *prepended* and *appended* options will appear in the order they were specified by the developer and will remain at the beginning and the end, respectively.

Every `@OptionSource` object can be specified with the following properties:

*value* - defines the path to a List-like structure, a node tree, or a tag folder. Plain paths and *path references* are supported (i.e., if a value is presented like `/typical/jcr/path`, this exact path will be looked for, but if given in the `/some/node@attr` format, the *attr* attribute will be retrieved from */some/node*, and its value will be then assumed to be the "true" path).

*fallback* defines a reserve path value for situations in which *value*-specified address is not reachable. This may be the case when *value* comes from an authored parameter of a component and the component has just been created. Then *fallback* may present a constant alternative;

*textMember* - if specified, defines the attribute of a JCR node to be rendered as RadioButton's or Select's *label*.
<br>Default is the *"jcr:title"* attribute;

*valueMember* - if specified, defines the attribute of a JCR node to be rendered as RadioButton's or Select's *value*.
<br>If the reserved token `@name` is specified, the node name will be used for value.
<br>If `@id` is specified, the *tag node name with tag namespace* (for a cq:Tag node) or a node name (otherwise) will be used as the value. This is particularly useful for tag listings.
<br>Default is the *"value"* attribute for ordinary JCR nodes and *"@name"* for tags;

*attributeMembers* - if specified, define one or more attributes of a node to be rendered as HTML attributes of the corresponding Granite UI entities. For example, `attributeMembers = "some-jcr-attribute"` will be rendered as `<coral-select-item data-some-jcr-attribute="literal_value_of_this_attribute">` in HTML;

*attributes* - if specified, define one or more static values to be rendered as HTML attributes of the corresponding Granite UI entities. For example, `attributes = "some-attribute:some-value"` argument will be rendered as `<coral-select-item data-some-attribute="some-value">` in HTML.
<br>Each attribute must consist of a key and a value separated with colon (`:`). If a key or a value itself must contain a colon, it can be escaped with `\`.

*textTransform* - if specified, defines the way the <u>label</u> will be transformed before rendering;

*valueTransform* - if specified, defines the way the <u>value</u> will be transformed before rendering.

#### Dynamic option change

Because an `@OptionProvider` supports *path references* apart from regular paths, the setting that says "where to look for the path" can be stored in a dialog field other than the one that actually deals with paths.

Therefore, it ought to be possible to dynamically respond to a *path reference* change. In the real world, it may look like the following. Imagine there is a dialog field (say, a path picker) that allows you to select a data source (say, a EToolbox List). Below is a select dropdown with options coming from the EToolbox List selected in the above path picker.

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
        action = "update-datasource",
        params = {
            @DependsOnParam(name = "sorted", value = "true"),
            @DependsOnParam(name = "append", value = "None:none")
        }
    )
    private String selectedOption;
}
```

The facility that makes it possible to dynamically update selectable options is the *DependsOn* action *"update-options"* (see more on DependsOn actions [here](depends-on.md)). It accepts any of the conventional *OptionProvider* params described above in its `params` collection.

***
#### See also

[Programming dynamic dialog behavior: DependsOn plugin client library](depends-on.md)
