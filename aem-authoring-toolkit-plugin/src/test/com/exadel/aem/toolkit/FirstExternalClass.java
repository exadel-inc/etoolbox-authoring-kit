package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;

@SuppressWarnings("unused")
class FirstExternalClass {

    private static final String FIELD_CAPTION = "caption";
    private static final String LABEL_CAPTION = "Caption";
    private static final String DESCRIPTION_CAPTION = "Provide the text to be shown as the Caption.";

    @DialogField(
            name = "Title",
            label = "Label title's",
            description = "Title's description"
    )
    @TextField
    @PlaceOnTab("Main tab")
    private String title;

    @DialogField(
            name = "Description",
            label = "Description's label",
            description = "description"
    )
    @RichTextEditor(
            features = {
                    RteFeatures.LINKS_MODIFYLINK,
                    RteFeatures.LINKS_UNLINK
            }
    )
    @PlaceOnTab("Main tab")
    private String description;

    @DialogField(
            name = FIELD_CAPTION,
            label = LABEL_CAPTION,
            description = DESCRIPTION_CAPTION
    )
    @TextField
    @PlaceOnTab("Main tab")
    private String caption;

    @DialogField(
            name = "Label's name",
            label = "Label",
            description = "description",
            required = true
    )
    @TextField
    @PlaceOnTab("Labels's tab")
    private String label;

    @DialogField(
            name = "FieldName",
            label = "Field label",
            description = "Field's description",
            required = true
    )
    @PathField(
            rootPath = "root/path"
    )
    @PlaceOnTab("URL's tab")
    private String url;

    @DialogField(
            name = "Image text",
            label = "Text on image",
            description = "Description for text on image"
    )
    @TextField
    @PlaceOnTab("Main tab")
    private String imageText;
}
