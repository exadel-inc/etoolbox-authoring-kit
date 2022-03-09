<!--
layout: content
title: Widgets (A-Z)
order: 2
-->
## Widgets glossary

### Accordion

* [@Accordion](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/layouts/Accordion.html)
* Resource type: /libs/granite/ui/components/coral/foundation/accordion
* See spec: [Accordion](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/accordion/index.html)

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

The `@Accordion` annotation can be added to an arbitrary class member (field or method); its return type does not matter. Other widgets can refer to the columns with their `@Place` directives. See the "Placing widgets" section below for more detail.

Apart from this usage, `@Accordion` can be specified at the class level as the layout hint for the entire dialog. See [Laying out your dialog](../dialog-layout.md) for details.

### Alert

* [@Alert](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Alert.html)
* Resource type: /libs/granite/ui/components/coral/foundation/alert
* See spec: [Alert](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/alert/index.html)

Used to render a component responsible for showing conditional alerts to the users in Touch UI dialogs. Usage is similar to the following:

*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

```java
public class DialogWithAlert {
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

* [@AnchorButton](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/AnchorButton.html)
* Resource type: /libs/granite/ui/components/coral/foundation/anchorbutton
* See spec: [AnchorButton](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/anchorbutton/index.html)

This is a component to represent a standard HTML hyperlink (`<a>`), but to look like a button in Touch UI dialogs. Usage is as follows:

```java
public class DialogWithAnchorButton {
    @AnchorButton(
        href = "https://acme.com/en/content/page.html",
        hrefI18n = "https://acme.com/fr/content/page.html",
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

* [@Autocomplete](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/autocomplete/Autocomplete.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/autocomplete
* See spec: [Autocomplete](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/autocomplete/index.html)

Used to render the Autocomplete component in Touch UI dialogs. Options that appear as the user enters text depend on the value of the *namespaces* property of the `@AutocompleteDataSource`. If unset, all tags under the *\<content/cq:Tags>* JCR directory will be available. Otherwise, you specify one or more particular *\<cq:Tag>* nodes as in the snippet below:

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

* [@Button](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/button/Button.html)
* Resource type: /libs/granite/ui/components/coral/foundation/button
* See spec: [Button](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/button/index.html)

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

### ButtonGroup

* [@ButtonGroup](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/buttongroup/ButtonGroup.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/buttongroup
* See spec: [ButtonGroup](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/buttongroup/index.html)

Used to render button groups in Touch UI dialogs. A `@ButtonGroup` contains one or more manually specified `@ButtonGroupItem`s or declares an `@OptionProvider`. It has the *selectionMode* to define how many values will be stored at once (the widget does not store values to JCR unless you select "single" or "multiple"). You can also specify the *ignoreData* and *deleteHint* flags.

Each of a `@ButtonGroup`'s manual items is initialized with mandatory *text* and *value*. Use *checked* to define a button that is selected by default. You can specify several more optional parameters, such as *icon* or *size*. A visually textless button can be rendered with `hideText = true` while Adobe recommends that you never set the *text* property to an empty string.

Next is a code snippet for a `@ButtonGroup` usage:

```java
public class DialogWithButtonGroup {
    @ButtonGroup(items = {
        @ButtonGroupItem(text = "Empty", value = ""),
        @ButtonGroupItem(text = "One", value = "1", hideText = true, icon = "/content/some/icon"),
        @ButtonGroupItem(text = "Two", value = "2", checked = true)
    },
        selectionMode = SelectionMode.SINGLE,
        ignoreData = true,
        deleteHint = false)
    String options;
}
```

You can define an *optionProvider* that will produce options based on a variety of supported media such as a JCR node tree, a tag folder, etc. See the chapter on [OptionProvider](../../option-provider.md).

### Checkbox

* [@Checkbox](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Checkbox.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/checkbox
* See spec: [Checkbox](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/checkbox/index.html)

Used to produce either simple or complex (nested) checkbox inputs in Touch UI dialogs.

Simple checkbox usage is as follows:

```java
public class DialogWithCheckbox {
    @DialogField
    @Checkbox(
        text = "Is option enabled?",
        value = "{Boolean}true",            // These are the defaults. You may override them
        uncheckedValue = "{Boolean}false",  // to, e.g., swap the field's meaning to "disabled" without migrating content
        autosubmit = true,
        tooltipPosition = Position.RIGHT
    )
    private boolean enabled;
}
```

#### Checkbox nesting

Sometimes you’ll need to supply a list of sub-level checkboxes to a parent checkbox whose displayed state will be affected by the states of child inputs. You can achieve this by specifying a *sublist* property of `@Checkbox` with a reference to a nested class encapsulating all the sub-level options. This is actually a full-feature rendition of [Granite UI NestedCheckboxList](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/nestedcheckboxlist/index.html)
.

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

* [@ColorField](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/color/ColorField.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/colorfield
* See spec: [ColorField](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/colorfield/index.html)

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

* [@DatePicker](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/datepicker/DatePicker.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/datepicker
* See spec: [DatePicker](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/datepicker/index.html)

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

* [@FieldSet](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/FieldSet.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/fieldset
* See spec: [FieldSet](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fieldset/index.html)

Creates a fieldset (a group of fields that can be managed as one) in a Touch UI dialog. See the [dedicated section on the fieldsets](./configuring-fieldset.md).

### FileUpload

* [@FileUpload](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/FileUpload.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/fileupload
* See spec: [FileUpload](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fileupload/index.html)

Used to render the FileUpload components in Touch UI dialogs. You can specify acceptable MIME types of files and graphic styles of the created component. You’ll be required to provide the *uploadUrl* to an accessible JCR path (you may also specify a sub-node of an existing node that will be created as needed). Sling shortcut *${suffix.path}* for component-relative JCR path is also supported.

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

### FixedColumns

* [@FixedColumns](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/layouts/FixedColumns.html)
* Resource type: /libs/granite/ui/components/coral/foundation/fixedcolumns
* See spec: [FixedColumns](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/fixedcolumns/index.html)

Used to create a container which consists of one or more columns. See the following sample:

```java
public class DialogWithFixedColumnsWidget {
    @DialogField
    @TextField
    @Place("First column")
    private String field1;

    @FixedColumns(
        value = {
            @Column(title = "First column"),
            @Column(title = "Second column")
        },
        maximized = false, // optional
        margin = true // optional
    )
    private Object columnsHolder;

    @DialogField
    @TextField
    @Place("Second column")
    private String field2;

}
```

*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

The `@FixedColumns` annotation can be added to an arbitrary class member (field or method). Its return type does not matter. Titles of columns do not get rendered: they are used merely for "binding" other widgets to particular columns. Other widgets can refer to the columns with their `@Place` directives. See the "Placing widgets" section below for more detail.

Apart from this usage, `@FixedColumns` can be specified at the class level as the layout hint for the entire dialog. See [Laying out your dialog](../dialog-layout.md) for details. Take a note that additional `@FixedColumns` properties, such as *maximized*, are only meaningful for the in-dialog usage.

### Heading

* [@Heading](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Heading.html)
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

* [@Hidden](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Hidden.html)
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

* [@Hyperlink](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Hyperlink.html)
* Resource type: /libs/granite/ui/components/coral/foundation/hyperlink
* See spec: [Hyperlink](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/hyperlink/index.html)

Used to represent HTML hyperlinks`(<a>)`in Touch UI dialogs. See the usage sample below:

```java
public class DialogWithHyperlink {
    @Hyperlink(
        href = "https://acme.com/en/content/page.html",
        hrefI18n = "https://acme.com/fr/content/page.html",
        text = "Link Text",
        hideText = true,
        linkChecker = LinkCheckerVariant.SKIP
    )
    String field;
}
```

*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

### ImageUpload

* [@ImageUpload](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/imageupload/ImageUpload.html)
* Resource type: cq/gui/components/authoring/dialog/fileupload

Designed as a companion to @FileUpload, it mimics the features of the FileUpload component that was there [before Coral 3 was introduced](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/foundation/form/fileupload/index.html) and the built-in upload component situated at _cq/gui/components/authoring/dialog/fileupload_ in your AEM installation. Technically, this is just another rendition of the FileUpload logic aimed at mainly uploading images via drag-and-drop. Use it like you’ll see in the following code snippet:

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

* [@Include](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Include.html)
* Resource type: /libs/granite/ui/components/coral/foundation/include
* See spec: [Include](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/include/index.html)

Used to set up embedded resources in Touch UI dialogs. An inclusion can be any resource resolvable by a Sling `ResourceResolver`. See the usage sample below:

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

* [@MultiField](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/MultiField.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/multifield
* See spec: [Multifield](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html)

Creates a multifield (a group of one or more fields that will be reproduced multiple times) in a Touch UI dialog. See the [dedicated section on the multifields](./configuring-multifield.md).

### NumberField

* [@NumberField](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/NumberField.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/numberfield
* See spec: [NumberField](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/numberfield/index.html)

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

* [@Password](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Password.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/password
* See spec: [Password](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/password/index.html)

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

* [@PathField](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/PathField.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/pathfield
* See spec: [PathField](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/pathfield/index.html)

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

* [@RadioGroup](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/radio/RadioGroup.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/radiogroup
* See spec: [RadioGroup](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/radiogroup/index.html)

Renders groups of RadioButtons in Touch UI dialogs. The usage is as follows:

```java
public class DialogWithRadioGroup {
    @DialogField
    @RadioGroup(
        buttons = {
            @RadioButton(text = "Button 1", value = "1", checked = true),
            @RadioButton(text = "Button 2", value = "2"),
            @RadioButton(text = "Button 3", value = "3", disabled = true)
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

You can define an *optionProvider* that will produce options based on a variety of supported media such as a JCR node tree, a tag folder, etc. See the chapter on [OptionProvider](../../option-provider.md).

### RichTextEditor

* [@RichTextEditor](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/rte/RichTextEditor.html)
* Resource type: /libs/cq/gui/components/authoring/dialog/richtext
* See spec: [RichTextEditor](https://experienceleague.adobe.com/docs/experience-manager-65/administering/operations/rich-text-editor.html?lang=en)

Renders a full-featured editor for rich text. See the [dedicated section on the usage of RTE](./configuring-rte.md).

### Select

* [@Select](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/select/Select.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/select
* See spec: [Select](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html)

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

You can define an *optionProvider* that will produce options based on a variety of supported media such as a JCR node tree, a tag folder, etc. See the chapter on [OptionProvider](../../option-provider.md).

### Switch

* [@Switch](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Switch.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/switch
* See spec: [Switch](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/switch/index.html)

Used to render an on-off toggle switch in a Touch UI dialog.

```java
public class DialogWithSwitch {
    @DialogField
    @Switch(
        value = "{Boolean}true",            // These are the defaults. You may override them
        uncheckedValue = "{Boolean}false",  // to, e.g., swap the field's meaning to "disabled" without migrating content
        checked = true,
        onText = "TurnedOn",                // These values are optional
        offText = "TurnedOff"
    )
    private boolean enableOption;
}
```

### Tabs

* [@Tabs](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/layouts/Tabs.html)
* Resource type: /libs/granite/ui/components/coral/foundation/tabs
* See spec: [Tabs](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/tabs/index.html)

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
    private TabsFieldset tabsHolder;

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

The `@Tabs` annotation can be added to an arbitrary class member (field or method); its return type does not matter. Other widgets can refer to the columns with their `@Place` directives. See the "Placing widgets" section below for more detail.

Apart from this usage, `@Tabs` can be specified at the class level as the layout hint for the entire dialog (see [Laying out your dialog](../dialog-layout.md) for details). Take note that the `@Tabs` annotation contains properties for both usages, but not every property has meaning for either. Refer to the Javadoc on `@Tabs` to learn which properties should be used for tabs at class level and tabs as a widget, respectively.

### Text

* [@Text](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/Text.html)
* Resource type: /libs/granite/ui/components/coral/foundation/text
* See spec: [Text](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/text/index.html)

Used to produce a text component that is rendered as <span> in Touch UI dialog. That's how it may look like:

```java
public class DialogWithText {
    @Text("Just a string notice")
    String textHolder;
}
```

*Note:* this widget annotation does not need to be accompanied by a `@DialogField`.

### TextArea

* [@TextArea](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/textarea/TextArea.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/textarea
* See spec: [Textarea](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textarea/index.html)

Used to render a *textarea*-type HTML input in Touch UI dialogs.

```java
public class DialogWithTextArea {
    @DialogField(label = "Edit the TextArea here")
    @TextArea(
        value = "Default value",
        emptyText = "Empty text",
        autocomplete = "on",
        autofocus = true,
        rows = 10,
        cols = 50,
        resize = TextAreaResizeType.BOTH
    )
    String text;
}
```

### TextField

* [@TextField](https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/TextField.html)
* Resource type: /libs/granite/ui/components/coral/foundation/form/textfield
* See spec: [TextField](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textfield/index.html)

Used to produce text inputs in Touch UI dialogs. The usage is as follows:

```java
public class DialogWithTextField {
    @DialogField
    @TextField
    String text1;

    @DialogField
    @TextField(
        value = "Predefined value",
        emptyText = "Empty text",
        autocomplete = "on",
        autofocus = true,
        maxLength = 255
    )
    String text2;
}
```
