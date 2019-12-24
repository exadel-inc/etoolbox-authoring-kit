# AEM Authoring Toolkit Samples

**AEM Authoring Toolkit Samples** is the example of using AEM Authoring Toolkit functional in the form of components set. Here you can find examples of using all AAT annotations, example of custom annotation and dependsOn actions.

## Table of contents
1. [Annotations map](#annotations-map)
    - [@Dialog, @DialogField, @Tab, @PlaceOnTab](#dialog-dialogfield-tab-placeontab)
    - [@Autocomplete](#autocomplete)
    - [@Attribute](#attribute)
    - [@Checkbox](#checkbox)
    - [@ColorField](#colorfield)
    - [@DatePicker](#datepicker)
    - [@Extends](#extends)
    - [@ImageUpload (@FileUpload)](#imageupload-fileupload)
    - [@Hidden](#hidden)
    - [@NumberField](#numberfield)
    - [@Password](#password)
    - [@PathField](#pathfield)
    - [@Properties, @Property](#properties-property)
    - [@RadioGroup](#radiogroup)
    - [@Select, @Option](#select-option)
    - [@Switch](#switch)
    - [@TextArea](#textarea)
    - [@TextField](#textfield)
    - [@FieldSet](#fieldset)
    - [@MultiField](#multifield)
    - [@RichTextEditor, @IconMapping, @HtmlPasteRules, @HtmlLinkRules, @Characters, @ParagraphFormat, @Style](#richtexteditor-iconmapping-htmlpasterules-htmllinkrules-characters--paragraphformat-style)
    - [@EditConfig, @InplaceEditingConfig](#editconfig-inplaceeditingconfig)
2. [Custom annotations and handlers](#custom-annotations-and-handlers)
3. [DependsOn](#dependson)
    
***

## Annotations map

Sling models with the examples are placed in the module `toolkit-samples-bundle` package `com.exadel.aem.toolkit.samples.models`.
    
#### `@Dialog`, `@DialogField`, `@Tab`, `@PlaceOnTab`

These annotations are used in the every model (just there is no `@PlaceOnTab` in the HomelandComponent).

  * Tab in form of nested class is used in the **HomelandComponent** model.
  
  ```
    @Dialog(
          ...
          layout = DialogLayout.TABS
    )
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public class HomelandComponent {

        @Tab(title = "Homeland")
        @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
        public static class HomelandTab { ... }
    }
  ```
    
  * Tabs in form of array of `@Tab` within `@Dialog` are used in the **every** model _instead_ HomelandComponent.
    
  ```
    @Dialog(
        ...
        tabs= {
            @Tab(title = "Main info"),
            @Tab(title = "Tastes"),
            @Tab(title = "Fruits"),
            @Tab(title = "Films")
        }
    )
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public class WarriorDescriptionComponent {
        ...
        @PlaceOnTab("Tastes")
        @DialogField( label = "Does your warrior like fruits?")
        @Checkbox
        @ValueMapValue
        private boolean isLikeFruits;
        ...
    }
  ```

***

#### `@Autocomplete`

`@Autocomplete` annotation is used in the **WarriorDescriptionComponent**.

***

#### `@Attribute`

`@Attribute` annotation is used in the **AttributeTestComponent**.

***

#### `@Checkbox`

`@Checkbox` annotation is used in the **WarriorDescriptionComponent**.

***

#### `@ColorField`

`@ColorField` annotation is used in the **ArmorColorComponent**.

***

#### `@DatePicker`

`@DatePicker` annotation is used in the **WarriorDescriptionComponent**.

***

#### `@Extends`

`@Extends` annotation is used in the **ExtendsAndPropertyTestComponent**.

***

#### `@ImageUpload`, `@FileUpload`

`@ImageUpload` annotation is used in the **HomelandComponent** and is special case of the `@FileUpload`.

`@FileUpload` annotation is used similarly:

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

***

#### `@Hidden`

`@Hidden` annotation is used in the **WarriorDescriptionComponent**.

***

#### `@NumberField`

`@NumberField` annotation is used in the **AbilitiesComponent**.

***

#### `@Password`

`@Password` annotation is used in the **SecretInfoComponent**.

***

#### `@PathField`

`@PathField` annotation is used in the **WarriorComponent**.

***

#### `@Properties`, `@Property`

`@Properties` and `@Property` annotation is used in the **ExtendsAndPropertyTestComponent** to override children of extended `@Select`.

***

#### `@RadioGroup`

`@RadioGroup` annotation is used in the **WarriorDescriptionComponent**.

***

#### `@Select`, `@Option`

`@Select` and `@Option` annotations are used in the **AbilitiesComponent**.

***

#### `@Switch`

`@Switch` annotation is used in the **WarriorComponent**.

***

#### `@TextArea`

`@TextArea` annotation is used in the **SecretInfoComponent**.

***

#### `@TextField`

`@TextField` annotation is used in the **WarriorComponent** and **AbilitiesComponent**.

***

#### `@FieldSet`

`@FieldSet` annotation is used in the **ArmorColorComponent**.

***

#### `@MultiField`

`@MultiField` annotation is used in the **AbilitiesComponent**.

***

#### `@RichTextEditor`, `@IconMapping`, `@HtmlPasteRules`, `@HtmlLinkRules`, `@Characters`, `@ParagraphFormat`, `@Style`

These annotations are used in the **WarriorDescriptionComponent**.

***

#### `@EditConfig`, `@InplaceEditingConfig`

`@EditConfig` and `@InplaceEditingConfig` annotations are used in the **WarriorComponent**.

***

## Custom annotations and handlers

Let's look custom annotation as an example of a postfix for `@FieldSet`. 
The example is placed in the the module `toolkit-camples-bundle` package `com.exadel.aem.toolkit.samples.annotations`.
Annotation handler is placed in the package `com.exadel.aem.toolkit.samples.annotations.handlers`

## DependsOn

`@DependsOn` and `@DependsOnRef` annotations are used in the **AbilitiesComponent** (`@DependsOnRef`, `@DependsOn`), **WarriorDescriptionComponent** (`@DependsOnRef`, `@DependsOn` `@DependsOnTab`) and **AttributeTestComponent** (`@DependsOnRef`, `@DependsOn`, group reference and selectors).

Also there are some examples of custom dependsOn actions creating. They are placed in the module `toolkit-samples-package`.
In the package `apps.authoring-toolkit.samples.components.content` are placed components with their logic and styles. Custom logic of the components is placed in `component_name/clientlib/authoring`.
There is custom dependsOn actions in the `abilities-component ` and `warrior-description-component`.

