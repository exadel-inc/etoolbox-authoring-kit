[Main page](../../README.md)

## Defining dialog fields

Dialog fields are the Granite UI / Touch UI entities usually built around an HTTP web form element (`<input>`). Granite UI provides a vast scope of dialog elements, such as text fields, RTEs, date pickers, path pickers, etc.

<small>All of these are generally referred to as "widgets" in the document below, despite the fact that the Granite UI documentation would most of the time name them "components". Our special naming is introduced not to mix up the components as "building bricks" of a dialog with "true" AEM components that represent a Java backend _plus_ the package folder.</small>

The ToolKit makes use of `@DialogField` annotation and the set of specific annotations, such as `@TextField`, `@Checkbox`, `@DatePicker`, etc., as discussed further below. They can be applied to either a class field or a method (methods of both class and interface are supported).

### DialogField

* `@DialogField`
* Resource type: /libs/granite/ui/components/coral/foundation/form/field
* See spec: [Field](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/field/index.html)

Used for defining common properties of a dialog field, such as *name* attribute (specifies under which name the value will be persisted, equals to the class' field name if not specified), *label*, *description*, *required*, *disabled*, *wrapperClass*, and *renderHidden*. In addition, `@DialogField` makes it possible to order fields inside a container by specifying a *ranking* value.

Typically, `@DialogField` is used in a pair with a widget annotation (e.g. `@TextField`).

```java
@Dialog
public class Dialog {
    @DialogField(
        label = "Field 1",
        description = "This is the first field",
        wrapperClass = "my-class",
        renderHidden = true,
        ranking = 5,
        validation = "foundation.jcr.name" // may as well accept array of strings
    )
    @TextField
    String field1;
}
```
Please note that if `@DialogField` is specified but a widget annotation is not, the field will not be rendered. This is because `@DialogField` exposes only the most common information about a field and does not specify which HTML component to use.

The other way around, you can specify a widget annotation and omit the `@DialogField`. A field like this will be rendered (without *label* and *description*, etc.), but its value will not be persisted. This usage may be handy if you need a merely "temporary" or "service" field.

In cases when the dialog class extends another class that has some fields marked with widget annotations, relevant fields from both the superclass and child class are rendered. All fields from the superclass and child class (even those sharing the same name) are considered different and rendered separately.

Still, namesake fields may interfere if rendered within the same container (dialog or tab). Therefore, avoid “field name collisions” between a superclass and a child class where possible. Even so, if you wish to do some deliberate "field overriding", refer to the [chapter](reusing-code.md) speaking about the use of `@Extends`, `@Replace` and `@Ignore`.

Unless manually aligned with `@Place` annotation, the fields are sorted in order of their *ranking*. If several fields have the same (or default) *ranking*, they are rendered in the order in which they appear in the source code. Class fields appear before class methods. Fields collected from ancestral classes have precedence over fields from child classes.

There are specific recommendations concerning fields' and methods' ordering. See the [Ordering widgets](#ordering-widgets) section below.

## Widgets (A-Z)

### Accordion

* `@Accordion`
* Resource type: /libs/granite/ui/components/coral/foundation/accordion
* See spec: [Accordion](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/accordion/index.html)

Used to create an accordion widget containing other widgets organized in panels. See the following sample:

```java
public class DialogWithAccordion {
    @DialogField
    @TextField
    private String field1;

    @Accordion(
        value = {
            @AccordionPanel(title = "Main panel", disabled = true),
            @AccordionPanel(title = "Description panel")
        },
        variant = AccordionVariant.LARGE,
        margin = true
    )
    AccordionFieldSet accordion;

    private static class AccordionFieldSet { // Used as the source of the nested accordion contents
        @DialogField
        @TextField
        @Place("Main panel")
        String name;

        @DialogField
        @TextArea
        @Place("Description panel")
        String description;
    }
}
```

*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

Apart from this usage, `@Accordion` can be specified at class level as the layout hint for the entire dialog. See [Laying out your dialog](dialog-layout.md) for details.

### Alert

* `@Alert`
* Resource type: /libs/granite/ui/components/coral/foundation/alert
* See spec: [Alert](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/alert/index.html?highlight=alert)

Used to render a component responsible for showing conditional alerts to the users in Touch UI dialogs. Usage is similar to the following:

*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

```java
public class DialogWithAlert{
    @Alert(
            size = AlertSize.LARGE,
            text = "Alert content",
            title = "Alert title",
            variant = StatusVariantConstants.SUCCESS
    )
    String alertField;
}
```

### AnchorButton

* `@AnchorButton`
* Resource type: /libs/granite/ui/components/coral/foundation/anchorbutton
* See spec: [AnchorButton](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/anchorbutton/index.html)

This is a component to represent a standard HTML hyperlink (`<a>`), but to look like a button in Touch UI dialogs. Usage is as follows:

```java
public class DialogWithAnchorButton {
    @AnchorButton(
        href = "http://acme.com/en/content/page.html",
        hrefI18n = "http://acme.com/fr/content/page.html",
        text = "Button Text",
        linkChecker = LinkCheckerVariant.SKIP,
        icon = "search",
        actionConfigName = "actionConfig"
    )
    String field;
}
```

*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

### Autocomplete

* `@Autocomplete`
* Resource type: /libs/granite/ui/components/coral/foundation/form/autocomplete
* See spec: [Autocomplete](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/autocomplete/index.html)

Used to render the Autocomplete component in Touch UI dialogs. Available options as the user enters text depend on the value of the *namespaces* property of the `@AutocompleteDataSource`. If unset, all tags under the *\<content/cq:Tags>* JCR directory will be available. Otherwise, you specify one or more particular *\<cq:Tag>* nodes as in the snippet below:

```java
public class AutocompleteDialog {
    @DialogField
    @Autocomplete(
        multiple = true,
        datasource = @AutocompleteDatasource(namespaces = {"workflow", "we-retail"})
    )
    String field;
}
```

### Button

* `@Button`
* Resource type: /libs/granite/ui/components/coral/foundation/button
* See spec: [Button](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/button/index.html)

Helps to create buttons in Touch UI dialogs. Usage is as follows:

```java
public class DialogWithButton {
    @Button(
        type = ButtonType.SUBMIT,
        text = "save",
        icon = "edit",
        command = "shift+s",
        variant = ElementVariantConstants.PRIMARY,
        block = true
    )
    String field;
}
```
*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

### Checkbox

* `@Checkbox`
* Resource type: /libs/granite/ui/components/coral/foundation/form/checkbox
* See spec: [Checkbox](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/checkbox/index.html)

Used to produce either simple or complex (nested) checkbox inputs in Touch UI dialogs.

Simple checkbox usage is as follows:
```java
public class DialogWithCheckbox {
    @DialogField
    @Checkbox(
        text = "Is option enabled?",
        value = "{Boolean}true",            // These are the defaults. You may override them
        uncheckedValue = "{Boolean}false",  // to e.g. swap the field's meaning to "disabled" without migrating content
        autosubmit = true,
        tooltipPosition = Position.RIGHT
    )
    private boolean enabled;
}
```

#### Checkbox nesting

Sometimes you’ll need to supply a list of sub-level checkboxes to a parent checkbox whose displayed state will be affected by the states of child inputs. You can achieve this by specifying a *sublist* property of `@Checkbox` with a reference to a nested class encapsulating all the sub-level options. This is actually a full-feature rendition of [Granite UI NestedCheckboxList](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/nestedcheckboxlist/index.html).

```java
@Dialog
public class NestedCheckboxListDialog {
    @DialogField
    @Checkbox(text = "Level 1 Checkbox", sublist = Sublist.class)
    private boolean option1L1;

    private static class Sublist {
        @DialogField
        @Checkbox(text = "Level 2 Checkbox 1")
        boolean option2L1;

        @DialogField
        @Checkbox(text = "Level 2 Checkbox 2", sublist = Sublist2.class)
        boolean option2L2;
    }

    private static class Sublist2 {
        @DialogField
        @Checkbox(text = "Level 3 Checkbox 1")
        boolean option3L1;

        @DialogField
        @Checkbox(text = "Level 3 Checkbox 2")
        boolean option3L2;
    }
}
```

### ColorField

* `@ColorField`
* Resource type: /libs/granite/ui/components/coral/foundation/form/colorfield
* See spec: [ColorField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/colorfield/index.html)

Used to render inputs for storing color values in Touch UI dialogs.
```java
public class DialogWithColorField {
    @DialogField
    @ColorField(
        value = "#4488CC",
        emptyText = "test-string",
        variant = ColorVariant.SWATCH,
        autogenerateColors = GenerateColorsState.SHADES,
        showSwatches = false,
        showDefaultColors = false,
        showProperties = false,
        customColors = {"#FF0000", "#00FF00", "#0000FF"}
    )
    String color;
}
```

### DatePicker

* `@DatePicker`
* Resource type: /libs/granite/ui/components/coral/foundation/form/datepicker
* See spec: [DatePicker](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/datepicker/index.html)

Used to render date/time pickers in Touch UI dialogs. You can set the type of DatePicker (whether it stores only the date, only the time, or both). You can also display format (see [Java documentation](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) on possible formats) and minimal and maximal date/time to select (may also specify a timezone). To make the formatter effective, set `typeHint = TypeHint.STRING` to store date/time to JCR as only a string and not a numeric value.

```java
public class DatePickerDialog {
    @DialogField
    @DatePicker(
        type = DatePickerType.DATETIME,
        displayedFormat = "DD.MM.YYYY HH:mm",
        valueFormat = "DD.MM.YYYY HH:mm",
        minDate = @DateTimeValue(day = 1, month = 1, year = 2019),
        maxDate = @DateTimeValue(day = 30, month = 4, year = 2020, hour = 12, minute = 10, timezone = "UTC+3"),
        typeHint = TypeHint.STRING
    )
    String currentDate;
}
```

### FieldSet

* `@FieldSet`
* Resource type: /libs/granite/ui/components/coral/foundation/form/fieldset
* See spec: [FieldSet](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fieldset/index.html)

Creates a fieldset (a group of fields that can be managed as one) in a Touch UI dialog. See the [dedicated section on the fieldsets](configuring-fieldset.md).

### FileUpload

* `@FileUpload`
* Resource type: /libs/granite/ui/components/coral/foundation/form/fileupload
* See spec: [FileUpload](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fileupload/index.html)

Used to render the FileUpload components in Touch UI dialogs. You can specify MIME types of files acceptable and graphic styles of the created component. You’ll be required to specify the *uploadUrl* to an actual and accessible JCR path (you may also specify a sub-node of an existing node that will be created as needed). Sling shortcut *${suffix.path}* for component-relative JCR path is also supported.

```java
public class FileUploadDialog {
    @DialogField
    @FileUpload(
        uploadUrl = "/content/dam/my-project",
        autoStart = true,
        async = true,
        mimeTypes = {
            "image/png",
            "image/jpg"
        },
        buttonSize = ButtonSize.LARGE,
        buttonVariant = ButtonVariant.ACTION_BAR,
        icon = "dataUpload",
        iconSize = IconSize.SMALL
    )
    String currentDate;
}
```
### Heading

* `@Heading`
* Resource type: /libs/granite/ui/components/coral/foundation/heading
* See spec: [Heading](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/heading/index.html)

Renders a heading element in Touch UI dialogs. See the following usage example:

```java
public class DialogWithHeading {
    @Heading(text = "Heading text", level = 2)
    String heading;
}
```
*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

### Hidden

* `@Hidden`
* Resource type: /libs/granite/ui/components/coral/foundation/form/hidden
* See spec: [Hidden](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/hidden/index.html)

Used to render hidden inputs in Touch UI dialogs.

```java
public class DialogWithHiddenFields {
    @DialogField
    @Hidden("bundled-value")
    String field;

    @DialogField(name = "field@Delete") // Can be used as a Sling POST servlet hint
    @Hidden
    String fieldRemover;
}
```

### Hyperlink

* `@Hyperlink`
* Resource type: /libs/granite/ui/components/coral/foundation/hyperlink
* See spec: [Hyperlink](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/hyperlink/index.html)

Used to represent HTML hyperlinks`(<a>)`in Touch UI dialogs. See the usage sample below:

```java
public class DialogWithHyperlink {
    @Hyperlink(
        href = "http://acme.com/en/content/page.html",
        hrefI18n = "http://acme.com/fr/content/page.html",
        text = "Link Text",
        hideText = true,
        linkChecker = LinkCheckerVariant.SKIP
    )
    String field;
}
```
*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.


### ImageUpload

* `@ImageUpload`
* Resource type: cq/gui/components/authoring/dialog/fileupload

Designed as a companion to @FileUpload, it mimics the features of the FileUpload component that was there [before Coral 3 was introduced](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/foundation/form/fileupload/index.html) and the built-in upload component situated at _cq/gui/components/authoring/dialog/fileupload_ in your AEM installation. Technically, this is just another rendition of the FileUpload logic aimed at mainly uploading images via drag-and-drop. Use it like you’ll see in the following code snippet:

```java
public class ImageFieldDialog {
    @DialogField
    @ImageUpload(
        mimeTypes = {
            ImageUploadConstants.MIME_TYPE_PNG,
            ImageUploadConstants.MIME_TYPE_JPG
        },
        sizeLimit = 100000,
        fileNameParameter = "./image/fileName",
        fileReferenceParameter = "image/fileRef",
        allowUpload = true,
        icon = "search"
    )
    String file;
}
```

### Include

* `@Include`
* Resource type: /libs/granite/ui/components/coral/foundation/include
* See spec: [Include](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/include/index.html)

Used to set up embedded resources in Touch UI dialogs. An inclusion can be any resource resolvable by a Sling `ResourceResolver`.
See the usage sample below:

```java
public class DialogWithInclude {
    @Include(
        path = "/content/path/to/resource",
        resourceType = "component/resource/type"
    )
    String field;
}
```
*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

### MultiField

* `@MultiField`
* Resource type: /libs/granite/ui/components/coral/foundation/form/multifield
* See spec: [Multifield](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html)

Creates a multifield (a group of one or more fields that be reproduced multiple times) in a Touch UI dialog. See the [dedicated section on the multifields](multiplying-fields.md).

### NumberField

* `@NumberField`
* Resource type: /libs/granite/ui/components/coral/foundation/form/numberfield
* See spec: [NumberField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/numberfield/index.html)

Used to render inputs for storing numbers in Touch UI dialogs. Use it as follows:

```java
public class DialogWithNumberField {
    @DialogField
    @NumberField(
        value = "", // Or else a particular value can be specified
        min = -10,
        max = 10,
        step = 2
    )
    String number;
}
```

### Password

* `@Password`
* Resource type: /libs/granite/ui/components/coral/foundation/form/password
* See spec: [Password](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/password/index.html)

Used to render password inputs in Touch UI dialogs. If you wish to engage a "confirm the password" box in your dialog's layout, create two `@Password`-annotated fields in your Java class, then feed the name of the second field to the *retype* property for the first one. If the values of the two fields do not match, you’ll see a validation error.

```java
public class DialogWithPasswordField {
    @DialogField
    @Password(retype = "confirmPass")
    String pass;

    @DialogField
    @Password
    String confirmPass;
}
```

### PathField

* `@PathField`
* Resource type: /libs/granite/ui/components/coral/foundation/form/pathfield
* See spec: [PathField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/pathfield/index.html)

Used to produce path selectors in Touch UI dialogs. You can implement it as in the following example:

```java
public class DialogWithPathField {
    @DialogField
    @PathField(
        rootPath = "/content/dam",
        emptyText = "Enter a path here"
    )
    String path;
}
```

### RadioGroup

* `@RadioGroup`
* Resource type: /libs/granite/ui/components/coral/foundation/form/radiogroup
* See spec: [RadioGroup](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/radiogroup/index.html)

Renders groups of RadioButtons in Touch UI dialogs. The usage is as follows:

```java
public class DialogWithRadioGroup {
    @DialogField
    @RadioGroup(
        buttons = {
            @RadioButton(text = "Button 1", value = "1", checked=true),
            @RadioButton(text = "Button 2", value = "2"),
            @RadioButton(text = "Button 3", value = "3", disabled=true)
        }
    )
    String field;
}
```
Note that you can set this up to use a *datasource* instead of a list of buttons. This way your `@RadioGroup` would look like this:

```java
public class DialogWithRadioGroup {
    @DialogField
    @RadioGroup(datasource = @DataSource(path = "my/path", resourceType = "my/res/type"))
    String field;
}
```

Just as for a `@RadioGroup`, you can define an *optionProvider* that will produce options based on a wide variety of supported media such as a JCR node tree, a tag folder, etc. See the chapter on [OptionProvider](option-provider.md).

### RichTextEditor

* `@RichTextEditor`
* Resource type: /libs/cq/gui/components/authoring/dialog/richtext
* See spec: [RichTextEditor](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/rich-text-editor.html)

Renders a full-featured editor for rich text. See the [dedicated section on the usage of RTE](configuring-rte.md).

### Select

* `@Select`
* Resource type: /libs/granite/ui/components/coral/foundation/form/select
* See spec: [Select](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html)

Used to render select inputs in Touch UI dialogs. `@Select` consists of a set of `@Option` items. Each of them must be initialized with mandatory *value* and several optional parameters, such as *text* (represents an option label), boolean flags *selected* and *disabled*, and String values responsible for visual presentation of an option: *icon*, *statusIcon*, *statusText* and *statusVariant*.

Here is a code snippet for a typical `@Select` usage:

```java
public class DialogWithDropdown {
    @DialogField(label = "Rating")
    @Select(
        options = {
            @Option(
                    text = "1 star",
                    value = "1",
                    selected = true,
                    statusIcon = "/content/dam/samples/icons/1-star-rating.png",
                    statusText = "This is to set 1-star rating",
                    statusVariant = StatusVariantConstants.SUCCESS
            ),
            @Option(text = "2 stars", value = "2"),
            @Option(text = "3 stars", value = "3"),
            @Option(text = "4 stars", value = "4", disabled = true),
            @Option(text = "5 stars", value = "5", disabled = true)
        },
        deleteHint = false,
        emptyOption = false,
        emptyText = "Select rating",
        forceIgnoreFreshness = false,
        multiple = false,
        ordered = false,
        translateOptions = false,
        variant = SelectVariant.DEFAULT
    )
    String dropdown;
}
```

Note that you can set this up to use a *datasource* instead of a list of options. This way your `@Select` would look like this:

```java
public class DialogWithDropdown {
    @DialogField(label = "Rating")
    @Select(
        datasource = @DataSource(path = "my/path", resourceType = "my/res/type"),
        emptyText = "Select rating"
    )
    String dropdown;
}
```

Just as for a `@RadioGroup`, you can define an *optionProvider* that will produce options based on a wide variety of supported media such as a JCR node tree, a tag folder, etc. See the chapter on [OptionProvider](option-provider.md).

### Switch

* `@Switch`
* Resource type: /libs/granite/ui/components/coral/foundation/form/switch
* See spec: [Switch](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/switch/index.html)

Used to render an on-off toggle switch in a Touch UI dialog.

```java
public class DialogWithSwitch {
    @DialogField
    @Switch(
        value = "{Boolean}true",            // These are the defaults. You may override them
        uncheckedValue = "{Boolean}false",  // to e.g. swap the field's meaning to "disabled" without migrating content
        checked = true,
        onText = "TurnedOn",                // These values are optional
        offText = "TurnedOff"
    )
    private boolean enableOption;
}
```

### Tabs

* `@Tabs`
* Resource type: /libs/granite/ui/components/coral/foundation/tabs
* See spec: [Tabs](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/tabs/index.html)

Used to create a nested tabs container possessing other widgets organized in tabs. See the following sample:

```java
public class DialogWithTabs {
    @Tabs(
        value = {
            @Tab(
                title = "First Inner",
                trackingElement = "first"
            ),
            @Tab(
                title = "Second Inner",
                trackingElement = "second",
                active = true,
                icon = "some/icon",
                padding = true
            )
        },
        maximized = true,
        trackingFeature = "feature1",
        trackingWidgetName = "widget1"
    )

    private static class TabsFieldset { // Used as the source of widgets for the nested tabs structure
        @DialogField(label = "Field 1 in the inner Tab")
        @TextField
        @Place("First Inner")
        String field1;

        @DialogField(label = "Field 2 in the inner Tab")
        @TextField
        @Place("Second Inner")
        String field2;
    }
}
```
*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

Apart from this usage, `@Tabs` can be specified at class level as the layout hint for the entire dialog (see [Laying out your dialog](dialog-layout.md) for details). Take note that the `@Tabs` annotation contains properties for both usages, but not every property is meaningful for either. Refer to the Javadoc on `@Tabs` to learn which properties should be used for tabs at class level and tabs as a widget respectively.

### Text

* `@Text`
* Resource type: /libs/granite/ui/components/coral/foundation/text
* See spec: [Text](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/text/index.html)

Used to produce a text component that is rendered as <span> in Touch UI dialog. That's how it may look like:

```java
public class DialogWithText {
    @Text("Just a string notice")
    String textHolder;
}
```
*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

### TextArea

* `@TextArea`
* Resource type: /libs/granite/ui/components/coral/foundation/form/textarea
* See spec: [Textarea](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textarea/index.html)

Used to render a *textarea*-type HTML input in Touch UI dialogs.

```java
public class DialogWithTextArea {
    @DialogField(label = "Edit the TextArea here")
    @TextArea(
        value = "Default value",
        emptyText = "Empty text",
        autofocus = true,
        rows = 10,
        cols = 50,
        resize = TextAreaResizeType.BOTH
    )
    String text;
}
```

### TextField

* `@TextField`
* Resource type: /libs/granite/ui/components/coral/foundation/form/textfield
* See spec: [TextField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textfield/index.html)

Used to produce text inputs in Touch UI dialogs. The usage is as follows:

```java
public class DialogWithTextField {
    @DialogField
    @TextField
    String text1;

    @DialogField
    @TextField(
        value = "Predefined value",
        emptyText = "Empty text"
    )
    String text2;
}
```

## Setting widgets' common attributes

Components of Touch UI dialogs honor the concept of [global HTML attributes](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/docs/server/commonattrs.html) added to rendered HTML tags. To set them via the ToolKit, you use the @Attribute annotation.

Also, you can assign additional properties of Granite UI components or re-write existing ones, even change, for instance, the *sling:resourceType* of a widget, with use of `@Property` annotation.

Read more on this in the [Additional properties](additional-properties.md) chapter.


## Ordering widgets

Widgets created upon class fields are placed in dialogs in the order they appear in the source class. When a class extends another class that also contains widgets, the ancestral ones are placed before the child ones.

If there are widgets build upon both class fields and methods, the field-based ones come first.

This behavior can be altered in two ways. First is the usage or *ranking* property of `@DialogField`. Ranking can equal to an integer value, no matter negative or positive. Fields with smaller ranking come first. Rankings persist across the superclass - child class relation and can be used in "insert" fields from a subclass in between fields of a superclass.

In some respects using *rankings* is not quite convenient. That is why there is another mechanism, You can attach `@Place(before = @ClassMember("anotherFieldName"))` or `@Place(after = @ClassMember(source = Another.class, value "anotherFieldName"))` to the field or method you want to be precisely placed.

The *before* and *after* parameters accept a `@ClassMember` argument. In its turn, the `@ClassMember` can contain the name of a relative field/method and optionally a reference to a class this fields originates from. When no class reference specified, the current class is assumed.

In the following sample you see the way to move a method from a subclass before field from a superclass and then the field from the same subclass before the former:

```java
public class MyComponentAncestor {
    @DialogField
    @TextArea
    private String description;
}

public class MyComponent extends MyComponentAncestor {
    @DialogField
    @TextField
    @Place(before = @ClassMember(source = MyComponentAncestor.class, value = "text"))
    private String getName() {/* ... */};

    @DialogField
    @TextField
    @Place(before = @ClassMember("getName")) // Class reference is not specified, therefore, the current class
    private Strning namePrefix;
}
```
The resulting widget sequence will be: the text field for *namePrefix*, the text field for *name* (as in "getName()"), and then the text area for *description*.

<u>Important notice</u>: it is recommended that you always use `@Place(before/after)` or *ranking* with
<u>method-based widgets</u> because it is not guaranteed that the JVM reports methods in the same order (unlike class fields).

***
#### See also

- [Configuring RichTextEditor](configuring-rte.md)
- [Laying out your dialogs](dialog-layout.md)
- [Grouping fields with FieldSet](configuring-fieldset.md)
- [Multiplying fields](multiplying-fields.md)
- [Additional properties of components, dialogs and fields](additional-properties.md)
- [Programming dynamic dialog behavior: DependsOn plugin client library](depends-on.md)
- [Feeding data to selection widgets with OptionProvider](option-provider.md)

