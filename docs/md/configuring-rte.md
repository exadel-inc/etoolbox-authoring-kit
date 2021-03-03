[Main page](../../README.md)
## Implementing RichTextEditor
[RichTextEditor (RTE)](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/rich-text-editor.html) is somehow special yet vastly demanded Coral dialog component that provides possibility of editing strings and texts with WYSIWYG experience. the functionality of the component is based upon set of plugins, either built-in or custom. Most plugins expose sets of "features" reflected by UI elements (buttons, or dropdown lists, or button panels, or floating panels - so called "popovers").

Traditionally, to add a feature to RichTextEditor a user needs to include a string representing a button in `toolbar` attributes of one or more XML nodes, include another node representing a plugin to a corresponding plugin tree and/or populate `features` attribute of that node and then possibly set plugin's custom features, each in one's own specific format. It the feature is to sit in a floating panel, the `<popovers>` node and its sub-nodes must be additionally taken care of.

 The **Toolkit** streamlines that process a lot.
### RTE features and popovers
Using AEM Authoring Toolkit, to initialize a RichTextEditor component with certain plugins/features, a user needs to apply `@RichTextEditor` annotation to a class field and then set the annotation's features property. This property accepts an array of strings in plugin#feature format. To specify a popover, one adds to the array a square-bracketed string like *[plugin#feature1, plugin#feature2,...plugin#featureN] or [plugin:feature1:feature2:...featureN]* depending on plugin's specification.

The built-in plugin#feature pairs are stored as constants of RteFeatures class for convenience. Feature sets of various built-in plugins grouped by plugin (so that to show them in separate popovers) are stored within RteFeatures.Popovers class. Definitions of specific panels are in RteFeatures.Panels class (for now only one specific panel, "table", is supported).

Thus the nearly maximal set of built-in features for a RichTextEditor component can be exposed in the following manner:
```java
@RichTextEditor(
    features = {
        RteFeatures.Popovers.CONTROL_ALL,
        RteFeatures.UNDO_UNDO,
        RteFeatures.UNDO_REDO,
        RteFeatures.SEPARATOR,
        RteFeatures.Popovers.EDIT_ALL,
        RteFeatures.Popovers.FINDREPLACE_ALL,
        RteFeatures.SEPARATOR,
        RteFeatures.Popovers.FORMAT_ALL,
        RteFeatures.Popovers.SUBSUPERSCRIPT_ALL,
        RteFeatures.Popovers.STYLES,
        RteFeatures.Popovers.PARAFORMAT,
        RteFeatures.Popovers.JUSTIFY_ALL,
        RteFeatures.Popovers.LISTS_ALL,
        RteFeatures.Popovers.LINKS_MODIFY_DELETE,
        RteFeatures.SEPARATOR,
        RteFeatures.Panels.TABLE,
        RteFeatures.SPELLCHECK_CHECKTEXT,
        RteFeatures.Popovers.MISCTOOLS_ALL,
        RteFeatures.FULLSCREEN_TOGGLE,
    }
)
private String text;
```
Apart from built-in features, you can append any features provided by a custom RTE plugin using the same string format. Technically, the plugin searches for plugin#feature strings and converts each into a toolbar button. Then it searches for `[plugin#feature1,plugin#feature2]` patterns and converts each into a popover. First plugin#feature entry becomes the button to bring on the popover. This one and all the rest entries are shown as the popover content.

 Thus, you alter any of the predefined popovers or compose a different popover (from either built-in, or custom features, or both). See the following snippet that indicates appending a custom feature, then two custom popovers to a feature set:
```java
@RichTextEditor ( /* ... */
    features = {
        "some#feature",
        RteFeatures.BEGIN_POPOVER +
            "myPlugin#feature1" + RteFeatures.FEATURE_SEPARATOR +
            "myPlugin#feature2" +
        RteFeatures.END_POPOVER,
        RteFeatures.BEGIN_POPOVER +
            RteFeatures.FORMAT_BOLD + RteFeatures.FEATURE_SEPARATOR +
            "myAnotherPlugin#feature3" + RteFeatures.FEATURE_SEPARATOR +
            RteFeatures.LINKS_ANCHOR +
        RteFeatures.END_POPOVER
    }
)
```
### RTE view modes
RichTextEditor configuration allows specifying features for three different editor modes. These are:

- *inline* (for an ordinary dialog window),
- *dialogFullScreen* (for a "maximized" dialog window), and
- *fullscreen* (for a dialog window which shows after "ToggleFullscreen" button pressed and for a "maximized" in-place editor).

You can separately specify set of features for *inline* and *dialogFullScreen*/*fullscreen* modes by populating "features" and *fullscreenFeatures* properties, accordingly. These two properties accept values in the same format. Or you can use one set of features for either, by populating only "features".

Generally it is recommended that a narrower set of features be used for the *inline*, e.g. "windowed" mode, and popover elements avoided in this mode due to unwanted visual effects, and a wider set of features with popovers unrestricted be used for any of the "fullscreen" modes.

If neither *features* nor *fullscreenFeatures* are populated, a default set of buttons will be generated for each of the three editor modes.

### Toolbar icons
A user can override existing or add new icon definitions for toolbar buttons via *icons* property. Several icon definitions may be missing from Coral installation. To provide complete user experience with the mentioned full feature set, you may use the following snippet:
```java
@RichTextEditor ( /* ... */
    icons = {
        @IconMapping(command = "#edit", icon = "copy"),
        @IconMapping(command = "#findreplace", icon = "search"),
        @IconMapping(command = "#links", icon = "link"),
        @IconMapping(command = "#table", icon = "table"),
        @IconMapping(command = "#subsuperscript", icon = "textSuperscript"),
        @IconMapping(command = "#control", icon = "check"),
        @IconMapping(command = "#misctools", icon = "fileCode"),
    }
)
```
### Settings for pasting text
One substantial concern for a RichTextEditor component maintainer is the rules for processing the input from clipboard. A user may specify *defaultPasteMode* and *htmlPasteRules* for dealing with non-plaintext clipboard content as in the below snippet:
```java
@RichTextEditor ( /* ... */
    defaultPasteMode = PasteMode.WORDHTML,
    htmlPasteRules = @HtmlPasteRules(
        allowBold = false,
        allowItalic = true,
        allowImages = false,
        allowLists = AllowElement.ALLOW,
        allowTables = AllowElement.REPLACE_WITH_PARAGRAPHS,
        allowedBlockTags = {"p", "sub"},
        fallbackBlockTag = "p"
    )
)
```
Setting the [htmlLinkRules](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/configure-rich-text-editor-plug-ins.html#linkstyles) property allows to additionally control the way internal and external links in pasted text are processed. See the following snippet:
```java
@RichTextEditor ( /* ... */
    htmlLinkRules = @HtmlLinkRules(
        cssInternal = "my-internal-link-style",
        cssExternal = "my-external-link-style",
        targetInternal = LinkTarget.MANUAL,
        targetExternal = LinkTarget.BLANK,
        protocols = {Protocol.HTTP, Protocol.HTTPS},
        defaultProtocol = Protocol.HTTPS,
    )
)
```
### Inserting special characters
Among the commonly user RTE assets is the `misctools#specialchars` feature that represents an "Insert symbol"-like dialog. The set of Unicode characters to offer may be defined in specialCharacters property. This is an array that stores either a single HTML entity definition or a Unicode range (decimal values) as in the following snippet:
```java
@RichTextEditor ( /* ... */
    specialCharacters = {
        @Characters(name = "Copyright", entity = "&copy"),
        @Characters(name = "Euro sign", entity = "&#x20AC"),
        @Characters(name = "Registered", entity = "&#x00AE"),
        @Characters(name = "Trademark", entity = "&#x2122"),
        @Characters(rangeStart = 512, rangeEnd = 514),
        @Characters(rangeStart = 998, rangeEnd = 1020)
    }
)
```
### Paragraph tagging and text styles
Set of formatting tags for a "paraformat" button may be defined in [formats](https://experienceleague.adobe.com/docs/experience-manager-65/administering/operations/configure-rich-text-editor-plug-ins.html?lang=en#operations) property as in the snippet:
```java
@RichTextEditor ( /* ... */
    formats = {
        @ParagraphFormat(tag = "h1", description = "My custom header"),
        @ParagraphFormat(tag = "h2", description = "My custom subheader")
    }
)
```
RichTextEditor allows to define text visual features by [CSS rules](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/configure-rich-text-editor-plug-ins.html#textstyles). Property *externalStyleSheets* is for specifying array of strings representing paths to JCR-stored CSS files that will be applied to the RTE content. After *externalStyleSheets* are set, one may populate the *styles* property with the CSS classes that will be offered to a use in styles dropdown, as in the below snippet:
```java
@RichTextEditor ( /* ... */
    externalStyleSheets = {
        "/etc/clientlibs/myLib/style1.css",
        "/etc/clientlibs/myLib/style2.css"
    },
    styles = {
        @Style(cssName = "my-style", text = "My custom style 1"),
        @Style(cssName = "my-another-style", text = "My custom style 2")
    }
)
```
(Unlike *formats* above, these are not HTML tag definitions but rather *\<span style='...'>...\</span>* blocks that will be added to selected text.)
### Miscellaneous tweaks
Additionally, a user can specify amount of edit operations stored for undo/redo logic (via [maxUndoSteps](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/configure-rich-text-editor-plug-ins.html#undohistory) property), the width of tabulation (in spaces, via [tabSize](https://helpx.adobe.com/experience-manager/6-3/sites/administering/using/configure-rich-text-editor-plug-ins.html#tabsize) property) and the indentation margin of lists (in spaces, via [indentMargin](https://helpx.adobe.com/experience-manager/6-3/sites/administering/using/configure-rich-text-editor-plug-ins.html#indentmargin) property).
