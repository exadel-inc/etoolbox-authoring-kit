[Main page](../../README.md)
## Defining dialog fields
The plugin makes use of `@DialogField`  annotation and the set of specific annotations, such as `@TextField`, `@Checkbox`, `@DatePicker`, etc., discussed further. The latter are referred as widget annotations.

### DialogField
* @DialogField
* Resource type: /libs/granite/ui/components/coral/foundation/form/field
* See spec: [Field](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/field/index.html)

Used for defining common properties of a field, such as the *name* attribute (specifies under which name the value will be persisted, equals to the class' field name if not specified), and also *label*, *description*, *required*, *disabled*, *wrapperClass*, *renderHidden*. In addition, `@DialogField` provides the possibility to order fields inside the dialog container by specifying *ranking* value.

Typically `@DialogField` is used in pair with one of widget annotations e.g. `@TextField`.

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
Please note that if `@DialogField` is specified but a widget annotation is not, such field will not be rendered (`@DialogField` exposes only most common information about a field and does not hint on which HTML component to use).

The other way around, you can indeed specify a widget annotation and omit `@DialogField`. Such field will be rendered (however without *label* and *description*, etc.), but its value will not be persisted.

In case when the dialog class extends another class that has some fields marked with widget annotations, relevant fields from both ancestral and child class are rendered. All fields from ancestral and child class (even those sharing same name) are considered different and rendered separately. Still namesake fields may interfere if rendered within same container (dialog or tab), so please avoid using same names. Still if you wish to engage some deliberate "field overriding", refer to the chapter on usage of `@Extends` below.

The fields are sorted in order of their *ranking*. If several fields have the same (or default) *ranking*, they are rendered in the order as they appear in the source code. Fields collected from ancestral classes have precedence over fields from child classes.

## Widgets (A-Z)

### Alert
* @Alert
* Resource type: /libs/granite/ui/components/coral/foundation/alert
* See spec: [Alert](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/alert/index.html?highlight=alert)

Used to render components responsible for showing conditional alerts to the users in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Alert](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/alert/index.html?highlight=alert). Usage is similar to the following:
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
Mind that alert variants available as of Coral 3 are enumerated in `StatusVariantConstants` class of the **Toolkit**'s API.

### Autocomplete
* @Autocomplete
* Resource type: /libs/granite/ui/components/coral/foundation/form/autocomplete
* See spec: [Autocomplete](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/autocomplete/index.html)

Used to render the component in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Autocomplete](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/autocomplete/index.html). Options becoming available as user enters text depend on the value of *namespaces* property of `@AutocompleteDataSource`. If unset, all tags under the *\<content/cq:Tags>* JCR directory will be available. Otherwise, you specify one or more particular *\<cq:Tag>* nodes as in the snippet below:
```java
public class AutocompleteDialog {
    @DialogField
    @Autocomplete(multiple = true, datasource = @AutocompleteDatasource(namespaces = {"workflow", "we-retail"}))
    String field;
}
```

### Button
* @Button
* Resource type: /libs/granite/ui/components/coral/foundation/button
* See spec: [Button](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/button/index.html)

Used to produce buttons in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Button](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/button/index.html).

*Note:* this widget annotation does not need to be accompanied with `@DialogWidget`

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

### Checkbox
* @Checkbox
* Resource type: /libs/granite/ui/components/coral/foundation/form/checkbox
* See spec: [Checkbox](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/checkbox/index.html)

Used to produce checkbox inputs in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Checkbox](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/checkbox/index.html).

#### Checkbox nesting
Sometimes there is a need to supply a list of sub-level checkboxes to a parent checkbox whose displayed state will be affected by the states of child inputs. You can achieve this by specifying *sublist* property of `@Checkbox` with a reference to a nested class encapsulating all the sub-level options. This is actually a full-feature rendition of [Granite UI NestedCheckboxList](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/nestedcheckboxlist/index.html).
```java
@Dialog
public class NestedCheckboxListDialog {
    @Checkbox(text = "Level 1 Checkbox", sublist = Sublist.class)
    private boolean option1L1;

    private static class Sublist {
        @Checkbox(text = "Level 2 Checkbox 1")
        boolean option2L1;

        @Checkbox(text = "Level 2 Checkbox 2", sublist = Sublist2.class)
        boolean option2L2;
    }

    private static class Sublist2 {
        @Checkbox(text = "Level 3 Checkbox 1")
        boolean option3L1;

        @Checkbox(text = "Level 3 Checkbox 2")
        boolean option3L2;
    }
}
```

### ColorField
* @ColorField
* Resource type: /libs/granite/ui/components/coral/foundation/form/colorfield
* See spec: [ColorField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/colorfield/index.html)

Used to render inputs for storing color values in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on ColorField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/colorfield/index.html).

### DatePicker
* @DatePicker
* Resource type: /libs/granite/ui/components/coral/foundation/form/datepicker
* See spec: [DatePicker](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/datepicker/index.html)

Used to render date/time pickers in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on DatePicker](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/datepicker/index.html). You can set the type of DatePicker (whether it stores only date, only time, or both). Also you can display format (see [Java documentation](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) on possible formats),
minimal and maximal date/time to select (may also specify timezone). To make formatter effective, set `typeHint = TypeHint.STRING` to store date/time to JCR as merely string and not a numeric value.
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
### FileUpload
* @FileUpload
* Resource type: /libs/granite/ui/components/coral/foundation/form/fileupload
* See spec: [FileUpload](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fileupload/index.html)

Used to render the FileUpload components in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on FileUpload](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fileupload/index.html). You can specify MIME types of files acceptable, graphic styles of the created component. It is required to specify *uploadUrl* to an actual and accessible JCR path (may also specify a sub-node of an existing node that will be created as needed). Sling shortcut  *${suffix.path}* for component-relative JCR path is also supported.
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

### ImageUpload
* @ImageUpload
* Resource type: cq/gui/components/authoring/dialog/fileupload

Designed as a companion to @FileUpload,  mimics the features of FileUpload component that was there [before Coral 3 was introduced](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/foundation/form/fileupload/index.html), and the build-in upload component situated at _cq/gui/components/authoring/dialog/fileupload_ in your AEM installation. Technically, this is but another rendition of FileUpload logic aimed at mainly uploading images via drag-and-drop
```java
public class ImageFieldDialog {
    @DialogField
    @ImageUpload(
        mimeTypes="image",
        title="Upload Image Asset",
        sizeLimit = 100000
    )
    String file;
}
```

### Heading
* @Heading
* Resource type: /libs/granite/ui/components/coral/foundation/heading
* See spec: [Heading](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/heading/index.html)

Used to render heading element in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Heading](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/heading/index.html).

*Note:* this widget annotation does not need to be accompanied with `@DialogWidget`
```java
public class DialogWithHeading {
    @Heading(text = "Heading text", level = 2)
    String heading;
}
```

### Hidden
* @Hidden
* Resource type: /libs/granite/ui/components/coral/foundation/form/hidden
* See spec: [Hidden](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/hidden/index.html)

Used to render hidden inputs in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Hidden](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/hidden/index.html).

### Hyperlink
* @Hyperlink
* Resource type: /libs/granite/ui/components/coral/foundation/hyperlink
* See spec: [Hyperlink](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/hyperlink/index.html)

Used to represent a HTML hyperlinks`(<a>)`in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Hyperlink](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/hyperlink/index.html).

### AnchorButton
* @AnchorButton
* Resource type: /libs/granite/ui/components/coral/foundation/anchorbutton
* See spec: [AnchorButton](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/anchorbutton/index.html)

AnchorButton is a component to represent a standard HTML hyperlink`(<a>)`, but to look like a button in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on AnchorButton](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/anchorbutton/index.html).

### NumberField
* @NumberField
* Resource type: /libs/granite/ui/components/coral/foundation/form/numberfield
* See spec: [NumberField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/numberfield/index.html)

Used to render inputs for storing numbers in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on NumberField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/numberfield/index.html).

### Password
* @Password
* Resource type: /libs/granite/ui/components/coral/foundation/form/password
* See spec: [Password](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/password/index.html)

Used to render password inputs in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Password](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/password/index.html). If you wish to engage "confirm password" box in your dialog's layout, create two `@Password`-annotated fields in your Java class, then feed the name of the second field to the *retype* property for the first one. If the values of the two fields do not match, validation error is produced.
```java
public class PasswordDialog {
    @DialogField
    @Password(retype = "confirmPass")
    String pass;
    @DialogField
    @Password
    String confirmPass;
}
```

### PathField
* @PathField
* Resource type: /libs/granite/ui/components/coral/foundation/form/pathfield
* See spec: [PathField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/pathfield/index.html)

Used to produce path selectors in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on PathField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/pathfield/index.html).

### RadioGroup
* @RadioGroup
* Resource type: /libs/granite/ui/components/coral/foundation/form/radiogroup
* See spec: [RadioGroup](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/alert/index.html?highlight=alert)

Used to render groups of RadioButtons in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on RadioGroup](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/radiogroup/index.html). The usage is as follows:
```java
public class RadioGroupDialog {
    @DialogField
    @RadioGroup(
        buttons = {
            @RadioButton(text = "Button 1", value = "1", checked=true),
            @RadioButton(text = "Button 2", value = "2"),
            @RadioButton(text = "Button 3", value = "3", disabled=true)
        }
    )
    String field8;
}
```
Mind you can set up to use a *datasource* instead of list of buttons. This way your `@RadioGroup` would look as follows:
```java
public class RadioGroupDialog {
    @DialogField
    @RadioGroup(datasource = @DataSource(path = "my/path", resourceType = "my/res/type"))
    String field8;
}
```
### Select
* @Select
* Resource type: /libs/granite/ui/components/coral/foundation/form/select
* See spec: [Select](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html)

Used to render select inputs in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Select](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html). `@Select` comprises set of `@Option` items. Each of them must be initialized with mandatory *value* and several optional parameters, such as *text* (represents option label), boolean flags *selected* and *disabled*, and also String values responsible for visual presentation of an option: *icon*, *statusIcon*, *statusText* and *statusVariant*.

Here is a code snippet for a typical `@Select` usage:
```java

public class MyDialogWithDropdown {
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
        emptyText = "Select rating",
        multiple = false,
        translateOptions = false,
        ordered = false,
        emptyOption = false,
        variant = SelectVariant.DEFAULT,
        deleteHint = false,
        forceIgnoreFreshness = false
    )
    String dropdown;
}
```
Mind you can set up to use a *datasource* instead of list of options. This way your `@Select` would look as follows:
```java
public class MyDialogWithDropdown {
    @DialogField(label = "Rating")
    @Select(
        datasource = @DataSource(path = "my/path", resourceType = "my/res/type"),
        emptyText = "Select rating"
    )
    String dropdown;
}
```

### Switch
* @Switch
* Resource type: /libs/granite/ui/components/coral/foundation/form/switch
* See spec: [Switch](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/switch/index.html)

Used to render on-off toggle switches in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Switch](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/switch/index.html).

### Text
* @Text
* Resource type: /libs/granite/ui/components/coral/foundation/text
* See spec: [Text](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/text/index.html)

Used to render text component that is rendered as <span> in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on Text](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/text/index.html).

### TextArea
* @TextArea
* Resource type: /libs/granite/ui/components/coral/foundation/form/textarea
* See spec: [Textarea](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textarea/index.html)

Used to render textarea HTML inputs in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on TextArea](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textarea/index.html).

### TextField
* @TextField
* Resource type: /libs/granite/ui/components/coral/foundation/form/textfield
* See spec: [TextField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textfield/index.html)

Used to produce text inputs in TouchUI dialogs. Exposes properties as described in [Adobe's Granite UI manual on TextField](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textfield/index.html).
