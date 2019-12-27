# AEM Authoring Toolkit Samples

**AEM Authoring Toolkit Samples** is an example of using AEM Authoring Toolkit functionality in the form of components set. Here you can find examples of using all AAT annotations, examples of custom annotation and dependsOn actions.

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

These annotations are used in every model (just there is no `@PlaceOnTab` in the HomelandComponent).

  * Tab in the form of a nested class is used in the [**HomelandComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/HomelandComponent.java).
  
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
    
  * Tabs in the form of an array of `@Tab` within `@Dialog` are used in the **every** model _except_ HomelandComponent.
    
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

`@Autocomplete` annotation is used in the [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

***

#### `@Attribute`

`@Attribute` annotation is used in the [**AttributeTestComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AttributeTestComponent.java).

***

#### `@Checkbox`

`@Checkbox` annotation is used in the [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

***

#### `@ColorField`

`@ColorField` annotation is used in the [**ArmorColorComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/ArmorColorComponent.java).

***

#### `@DatePicker`

`@DatePicker` annotation is used in the [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

***

#### `@Extends`

`@Extends` annotation is used in the [**ExtendsAndPropertyTestComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/ExtendsAndPropertyTestComponent.java).

***

#### `@ImageUpload`, `@FileUpload`

`@ImageUpload` annotation is used in the [**HomelandComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/HomelandComponent.java) and is a special case of the `@FileUpload`.

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

`@Hidden` annotation is used in the [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

***

#### `@NumberField`

`@NumberField` annotation is used in the [**AbilitiesComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java).

***

#### `@Password`

`@Password` annotation is used in the [**SecretInfoComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/SecretInfoComponent.java).

***

#### `@PathField`

`@PathField` annotation is used in the [**WarriorComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java).

***

#### `@Properties`, `@Property`

`@Properties` and `@Property` annotation is used in the [**ExtendsAndPropertyTestComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/ExtendsAndPropertyTestComponent.java) to override children of extended `@Select`.

***

#### `@RadioGroup`

`@RadioGroup` annotation is used in the [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

***

#### `@Select`, `@Option`

`@Select` and `@Option` annotations are used in the [**AbilitiesComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java).

***

#### `@Switch`

`@Switch` annotation is used in the [**WarriorComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java).

***

#### `@TextArea`

`@TextArea` annotation is used in the [**SecretInfoComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/SecretInfoComponent.java).

***

#### `@TextField`

`@TextField` annotation is used in the [**WarriorComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java) and [**AbilitiesComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java).

***

#### `@FieldSet`

`@FieldSet` annotation is used in the [**ArmorColorComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/ArmorColorComponent.java).

***

#### `@MultiField`

`@MultiField` annotation is used in the [**AbilitiesComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java).

***

#### `@RichTextEditor`, `@IconMapping`, `@HtmlPasteRules`, `@HtmlLinkRules`, `@Characters`, `@ParagraphFormat`, `@Style`

These annotations are used in the [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

***

#### `@EditConfig`, `@InplaceEditingConfig`

`@EditConfig` and `@InplaceEditingConfig` annotations are used in the [**WarriorComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java).

***

## Custom annotations and handlers

Let's look at custom annotation as an example of a postfix for `@FieldSet`. 
The example is placed in the the module `toolkit-camples-bundle` package `com.exadel.aem.toolkit.samples.annotations`.
Annotation handler is placed in the package `com.exadel.aem.toolkit.samples.annotations.handlers`

## DependsOn

#### `@DependsOn`

`@DependsOn` annotation is used in the:
 - [**AbilitiesComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java) to switch visibility (default action) of a multifield with the simple boolean query:
   ```
   @DependsOn(query = "@ability === 'magic'")
   ```
 - [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java) 
   - to call custom action with a parent component's path argument, that gets color theme of a parent:
   ```
   @DependsOn(query = "@parentPath", action = "getParentColorTheme")
   ```
   - to call custom action, that changes tags scope of `@Autocomplete` depending on a color theme:
   ```
   @DependsOn(query = "@isDarkColorTheme", action = "namespaceFilter") 
   ```
 - [**AttributeTestComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AttributeTestComponent.java) to switch visibility and disabling of a textfield with the group boolean query (using back-forward class selector):
   ```
   @DependsOn(query = "@@checkbox(coral-panel |> .toggle-fieldSet).every(item => item)")
   @DependsOn(query = "@@checkbox.every(item => item)", action = DependsOnActions.DISABLED)
   ``` 

***

#### `@DependsOnRef`

`@DependsOnRef` annotation is used in the [**AbilitiesComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java), [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java) and [**AttributeTestComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/AttributeTestComponent.java)

***

#### `@DependsOnTab`

`@DependsOnTab` annotation is used in the [**WarriorDescriptionComponent**](./toolkit-samples-bundle/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java) to switch visibility of tabs with the simple boolean queries:

    ```
    @DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_FRUITS, query = "@isLikeFruits")
    @DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_FILMS, query = "@isLikeFilms")
    ```
Custom dependsOn actions placed in the [`abilities-component`](./toolkit-samples-package/src/main/content/jcr_root/apps/authoring-toolkit/samples/components/content/abilities-component/clientlib/authoring/altVisibility.js) and [`warrior-description-component`](./toolkit-samples-package/src/main/content/jcr_root/apps/authoring-toolkit/samples/components/content/warrior-description-component/clientlib/authoring).

