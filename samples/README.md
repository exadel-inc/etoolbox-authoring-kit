# Exadel Authoring Kit for AEM Samples

This project contains examples of using *Exadel Toolbox Authoring Kit* (*ToolKit*) functionality via several AEM components. Here you can find many built-in and custom *ToolKit* annotations, as well as *DependsOn* annotations and actions.

## Table of contents
1. [Project structure and installation](#project-structure-and-installation)
2. [Annotations reference](#annotations-reference)
    - [@Dialog, @DialogField, @Tab, @PlaceOnTab](#dialog-dialogfield-tab-placeontab)
    - [@Autocomplete](#autocomplete)
    - [@Attribute](#attribute)
    - [@Checkbox](#checkbox)
    - [@ColorField](#colorfield)
    - [@DatePicker](#datepicker)
    - [@EditConfig, @InplaceEditingConfig](#editconfig-inplaceeditingconfig)
    - [@Extends](#extends)
    - [@FieldSet](#fieldset)
    - [@Hidden](#hidden)
    - [@ImageUpload (@FileUpload)](#imageupload-fileupload)
    - [@Include](#include)
    - [@MultiField](#multifield)
    - [@NumberField](#numberfield)
    - [@Password](#password)
    - [@PathField](#pathfield)
    - [@Properties, @Property](#properties-property)
    - [@RadioGroup](#radiogroup)
    - [@RichTextEditor, @IconMapping, @HtmlPasteRules, @HtmlLinkRules, @Characters, @ParagraphFormat, @Style](#richtexteditor-iconmapping-htmlpasterules-htmllinkrules-characters-paragraphformat-style)
    - [@Select, @Option](#select-option)
    - [@Switch](#switch)
    - [@TextArea](#textarea)
    - [@TextField](#textfield)
3. [Custom annotations and handlers](#custom-annotations-and-handlers)
4. [DependsOn](#dependson-plugin)
    - [@DependsOn](#dependson)
    - [@DependsOnRef](#dependsonref)
    - [@DependsOnTab](#dependsontab)
    - [@DependsOnParam](#dependsonparam)


## Project structure and installation
The project consists of three modules: the **core** module containing *ToolKit*-annotated Sling models; the **apps** module that contains matching AEM components; and the **content** module used to deploy sandboxing pages and supplementary AEM entities.

To compile and install the project's artifacts to the local Maven repository use `mvn clean install` command. To deploy the installation to an arbitrary AEM instance, run `mvn clean install -Pinstall-samples`.

The content is deployed to *http://localhost:4502* by default; however you can control the target with the additional command line switches like `-Dhost=some_host`, `-Dport=XXXX`.

You can also control the version of *ToolKit* API and Maven plugin called by the project. To do this, use the command line switch `-Dtoolkit.version=<arbitrary release or snapshot>`.


***

## Annotations reference

Sling models with the examples are placed in the **core** module.

#### `@Dialog`, `@DialogField`, `@Tab`, `@PlaceOnTab`

These annotations are used in nearly every model.

  * Tab in the form of a nested class is used in the [HomelandComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/HomelandComponent.java).

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

  * Tabs in the form of an array of `@Tab` within `@Dialog` are used in all models except for HomelandComponent.

  ```
    @Dialog(
        ...
        tabs= {
            @Tab(title = "Main info"),
            @Tab(title = "Tastes"),
            @Tab(title = "Fruits"),
            @Tab(title = "Movies")
        }
    )
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public class WarriorDescriptionComponent {
        ...
        @PlaceOnTab("Tastes")
        @DialogField( label = "Does your warrior like fruits?")
        @Checkbox
        @ValueMapValue
        private boolean likesFruits;
        ...
    }
  ```

#### `@Autocomplete`

This annotation is used in the [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

#### `@Attribute`

This annotation is used in the [ShoppingListComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/ShoppingListComponent.java).

#### `@Checkbox`

This annotation is used in the [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

#### `@ColorField`

This annotation is used in the [ArmorColorComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/ArmorColorComponent.java).

#### `@DatePicker`

This annotation is used in the [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

#### `@EditConfig`, `@InplaceEditingConfig`

`@EditConfig` and `@InplaceEditingConfig` are used in the [WarriorComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java).

#### `@Extends`

`@Extends` is used in the [DungeonsComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/DungeonsComponent.java).

#### `@FieldSet`

This annotation is used in the [ArmorColorComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/ArmorColorComponent.java) and [ShoppingListComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/ShoppingListComponent.java).

#### `@Hidden`

This annotation is used in the [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

#### `@ImageUpload`, `@FileUpload`

`@ImageUpload` is used in the [HomelandComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/HomelandComponent.java).

`@FileUpload` is generally used similarly:

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
    String file;
}
```
#### `@Include`

This annotation is used in the [DungeonsComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/DungeonsComponent.java).

#### `@MultiField`

`@MultiField` is used in the [AbilitiesComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java).

#### `@NumberField`

This annotation is used in the [AbilitiesComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java).

#### `@Password`

This annotation is used in the [SecretInfoComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/SecretInfoComponent.java).

#### `@PathField`

This annotation is used in the [WarriorComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java).

#### `@Properties`, `@Property`

`@Properties` and `@Property` are used in the [DungeonsComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/DungeonsComponent.java) to override children of extended `@Select`.


#### `@RadioGroup`

This annotation is used in the [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java).

#### `@RichTextEditor`, `@IconMapping`, `@HtmlPasteRules`, `@HtmlLinkRules`, `@Characters`, `@ParagraphFormat`, `@Style`

These annotations are used in the [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java) and [DungeonsComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/DungeonsComponent.java).

#### `@Select`, `@Option`

`@Select` and `@Option` are used in the [AbilitiesComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java) and [**DungeonsComponent**](./core/src/main/java/com/exadel/aem/toolkit/samples/models/DungeonsComponent.java).

#### `@Switch`

This annotation is used in the [WarriorComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java).


#### `@TextArea`

This annotation is used in the [SecretInfoComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/SecretInfoComponent.java).

#### `@TextField`

This annotation is used in the [WarriorComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorComponent.java), [AbilitiesComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java) and [ShoppingListComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/ShoppingListComponent.java).

***

## Custom annotations and handlers

An instance of a custom annotation is used to implement the *field name postfix" feature for the `@FieldSet`.
The example is placed in the **core** module (package `com.exadel.aem.toolkit.samples.annotations`).
Annotation handler is placed in the package `com.exadel.aem.toolkit.samples.annotations.handlers`.

***

## DependsOn Plugin

#### `@DependsOn`

This annotation is used in:
 - [AbilitiesComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java)
    - to switch visibility (default action) of a multifield with the simple boolean query:
   ```
   @DependsOn(query = "@ability === 'magic'")
   ```
   - to validate max count of multifield's items with the special reference `@this` for a multifield:
   ```
   @DependsOn(
       query = "@this.length <= 3",
       action = DependsOnActions.VALIDATE,
       params = { @DependsOnParam(name = "msg", value = "Too powerful!")}
   )
   ```
 - [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java)
   - to get *colorTheme* property from the following path:
   ```
   @DependsOn(
       query = "'../../colorTheme'",
       action = DependsOnActions.FETCH
   )
   ```
   - to call custom action that changes tags scope of `@Autocomplete` depending on a color theme:
   ```
   @DependsOn(query = "@isDarkColorTheme", action = "namespaceFilter")
   ```
 - [ShoppingListComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/ShoppingListComponent.java) to switch visibility and disable a textfield with the group boolean query (using back-forward class selector);
 and to set default text for a textfield depending on its state:
   ```
   @DependsOn(query = "@@checkbox(coral-panel |> .products-fieldSet).every(item => item)")
   @DependsOn(query = "@@checkbox.every(item => item)", action = DependsOnActions.DISABLED)
   @DependsOn(query = "ToolKitSamples.getShoppingDefaultText(@@checkbox(coral-panel |> .weapon-fieldSet), @this)", action = DependsOnActions.SET)
   ```

#### `@DependsOnRef`

This annotation is used in [AbilitiesComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java), [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java), and [ShoppingListComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/ShoppingListComponent.java)

#### `@DependsOnTab`

This annotation is used in the [WarriorDescriptionComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/WarriorDescriptionComponent.java) to switch visibility of tabs with simple boolean queries:

    @DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_FRUITS, query = "@likesFruit")
    @DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_MOVIES, query = "@likesMovies")

#### `@DependsOnParam`

This annotation is used in the [AbilitiesComponent](./core/src/main/java/com/exadel/aem/toolkit/samples/models/AbilitiesComponent.java) to show custom validation message:
   ```
   @DependsOn(
       query = "@this.length <= 3",
       action = DependsOnActions.VALIDATE,
       params = { @DependsOnParam(name = "msg", value = "Too powerful!")}
   )
   ```
Custom *DependsOn* actions are placed in the [`abilities-component`](./apps/src/main/content/jcr_root/apps/etoolbox-authoring-kit/samples/components/content/abilities-component/clientlib/authoring/altVisibility.js) and [`warrior-description-component`](./apps/src/main/content/jcr_root/apps/etoolbox-authoring-kit/samples/components/content/warrior-description-component/clientlib/authoring) folders.

