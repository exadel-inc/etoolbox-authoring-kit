/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.ListenerConstants;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.Autocomplete;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.AutocompleteDatasource;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;

@Dialog(name = "test-component",
        title = "First dialog title",
        description = "First dialog description",
        componentGroup = "TestComponentGroup",
        layout = DialogLayout.TABS,
        disableTargeting = true,
        resourceSuperType = "TestComponentSuperType",
        extraClientlibs = "cq.common.wcm",
        tabs = {
                @Tab(title = "First tab"),
                @Tab(title = "Second tab"),
                @Tab(title = "Third tab"),
                @Tab(title = "Fourth tab"),
                @Tab(title = "Fifth tab"),
                @Tab(title = "Sixth tab"),
                @Tab(title = "Seventh tab")
        })
@EditConfig(
        listeners = {
                @Listener(event = ListenerConstants.EVENT_AFTER_INSERT, action = ListenerConstants.ACTION_REFRESH_PAGE),
                @Listener(event = ListenerConstants.EVENT_AFTER_DELETE, action = ListenerConstants.ACTION_REFRESH_PAGE)
        },
        dropTargets = {
                @DropTargetConfig(
                        nodeName = "image",
                        accept = {"image/.*"},
                        groups = {"media"},
                        propertyName = "file_image" + "file-reference"
                ),
                @DropTargetConfig(
                        accept = {"image/.*"},
                        groups = {"media"},
                        propertyName = "file_image" + "file",
                        nodeName = "big_image"
                )
        },
        inplaceEditing = {
                @InplaceEditingConfig(
                        title = "label_header",
                        propertyName = "header",
                        type = "in_place_text_editor",
                        editElementQuery = ".header"
                ),
                @InplaceEditingConfig(
                        title = "label_description",
                        propertyName = "description",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-description"
                )
        }
)
@SuppressWarnings("unused")
public class ComplexComponent2 {
    private static final String TAB_TITLE_FEED = "Feed";

    private static final String PRIMARY_ELEMENT_LABEL = "Primary Element";
    private static final String SECONDARY_ELEMENT_LABEL = "Secondary Element";
    private static final String VIDEO_ELEMENT_LABEL = "Video Element";

    private static final String PRIMARY = "primary";
    private static final String SECONDARY = "secondary";

    private static final String FEED_1 = "feedFirst";
    private static final String FEED_2 = "feedSecond";

    private static final String LEFT_LAYOUT = "left";
    private static final String RIGHT_LAYOUT = "right";
    private static final String STACKED_LAYOUT = "stacked";

    private static final String LIGHT_TEXT_THEME = "light";
    private static final String DARK_TEXT_THEME = "dark";

    private static final String FIELD_LAYOUT = "field_layout";
    private static final String LABEL_LAYOUT = "label_layout";
    private static final String DESCRIPTION_LAYOUT = "description_layout";

    private static final String FIELD_DESCRIPTION = "field description";
    private static final String LABEL_DESCRIPTION = "label description";
    private static final String DESCRIPTION_DESCRIPTION = "description";


    @DialogField(
            name = FIELD_LAYOUT,
            label = LABEL_LAYOUT,
            description = DESCRIPTION_LAYOUT,
            required = true
    )
    @Select(
            options = {
                    @Option(text = "First option", value = LEFT_LAYOUT, selected = true),
                    @Option(text = "Second option", value = RIGHT_LAYOUT),
                    @Option(text = "Third option", value = STACKED_LAYOUT),
                    @Option(text = "Forth option", value = FIELD_LAYOUT)
            })
    private String layout;

    @DialogField(
            name = "field-text",
            label = "label-text",
            description = "description text",
            required = true
    )
    @RadioGroup(
            buttons = {
                    @RadioButton(text = "Dark", value = DARK_TEXT_THEME, checked = true),
                    @RadioButton(text = "Light", value = LIGHT_TEXT_THEME)
            }
    )
    @PlaceOnTab("Tab title general")
    private String text;

    @DialogField(
            name = "field header",
            label = "label header",
            description = "description header",
            required = true
    )
    @TextField
    @PlaceOnTab("Tab title general")
    private String header;

    @DialogField(
            name = FIELD_DESCRIPTION,
            label = LABEL_DESCRIPTION,
            description = DESCRIPTION_DESCRIPTION
    )
    @RichTextEditor(
            features = {
                    RteFeatures.LINKS_MODIFYLINK,
                    RteFeatures.LINKS_UNLINK,
                    RteFeatures.SUBSUPERSCRIPT_SUBSCRIPT,
                    RteFeatures.SUBSUPERSCRIPT_SUPERSCRIPT
            }
    )
    @PlaceOnTab("Tab title general")
    private String description;

    @DialogField(
            name = "field enable gradient",
            label = "label enable gradient",
            description = "description enable gradient"
    )
    @Checkbox(checked = true)
    private Boolean gradient;

    @DialogField(
            name = "field_media_text",
            label = "label media text",
            description = "description media text",
            required = true
    )
    @TextField
    @PlaceOnTab("Tab title general")
    private String mediaText;

    @DialogField(
            name = "field_text_content",
            label = "label_text_content",
            description = "description_text_content",
            required = true
    )
    @TextField
    @PlaceOnTab("Tab title general")
    private String textContent;

    //2
    @DialogField
    @Attribute(data = {
            @Data(name = "feed-dialog", value = "1")
    })
    @FieldSet(namePrefix = FEED_1, title = "Feed first")
    @PlaceOnTab(TAB_TITLE_FEED)
    private FeedFieldSet feedFirst;

    @DialogField
    @FieldSet(namePrefix = FEED_1, title = "Feed first element")
    @PlaceOnTab(TAB_TITLE_FEED)
    private ElementFieldSet feedFirstElement;

    @DialogField(
            name = "field_enable_fee1_1",
            label = "label enable feed 1",
            description = "description enable feed 1"
    )
    @Switch
    @PlaceOnTab(TAB_TITLE_FEED)
    private boolean enableFeedFirst;

    @DialogField()
    @Attribute(data = {@Data(name = "feed-dialog", value = "2")})
    @FieldSet(namePrefix = FEED_2, title = "Feed")
    @PlaceOnTab(TAB_TITLE_FEED)
    private FeedFieldSet feed;

    @DialogField
    @FieldSet(namePrefix = FEED_2, title = "Feed Second Element")
    @PlaceOnTab(TAB_TITLE_FEED)
    private ElementFieldSet feedSecondElement;

    //3
    @DialogField(
            name = "field enable primary element",
            label = "label enable primary element",
            description = "description enable primary element"
    )
    @Switch
    @PlaceOnTab("Tab title element")
    private boolean primaryElementEnable;

    @DialogField
    @FieldSet(namePrefix = PRIMARY, title = PRIMARY_ELEMENT_LABEL)
    @PlaceOnTab("Tab title element")
    private ElementIconFieldSet primaryElement;

    @DialogField(
            name = "field enable secondary element",
            label = "label enable secondary element",
            description = "description enable secondary element"
    )
    @Switch
    @PlaceOnTab("Tab title element")
    private boolean secondaryElementEnable;

    @DialogField()
    @FieldSet(namePrefix = SECONDARY, title = SECONDARY_ELEMENT_LABEL)
    @PlaceOnTab("Tab title element")
    private ElementIconFieldSet secondaryElement;

    @DialogField(
            name = "fieldEnableVideoElement",
            label = "label enable video element",
            description = "description enable video element"
    )
    @Switch
    @PlaceOnTab("Tab title element")
    private boolean videoElementEnable;

    @DialogField
    @FieldSet(title = VIDEO_ELEMENT_LABEL)
    @PlaceOnTab("Tab title element")
    private VideoElementFieldSet videoElement;

    //4
    @DialogField(
            name = "fieldImage",
            label = "label image",
            required = true
    )
    @ImageUpload(sizeLimit = 100L)
    @PlaceOnTab("Tab title image")
    private String imagePath;

    //5
    @DialogField(
            name = "fieldProductImage",
            label = "label product image",
            required = true
    )
    @ImageUpload(sizeLimit = 0L)
    @Extends(value = ComplexComponent2.class, field = "imagePath")
    @PlaceOnTab("Tab title product image")
    private String productImagePath;

    //6
    @DialogField(
            name = "field_widget",
            label = "label widget",
            description = "description widget"
    )
    @PathField
    @PlaceOnTab("tab widget")
    private String widget;

    @DialogField(
            name = "field enable widget",
            label = "labelEnableWidget",
            description = "descriptionwidget"
    )
    @Checkbox
    @PlaceOnTab("tab widget")
    private boolean enableWidget;


    private static class ElementFieldSet {
        @DialogField(
                name = "field_element_text",
                label = "label element text",
                description = "description element text",
                ranking = 1,
                required = true
        )
        @TextField
        private String elementText;

        @DialogField(
                name = "fieldElementPath",
                label = "label element path",
                description = "description element path",
                ranking = 2,
                required = true
        )
        @PathField(rootPath = "/base/path")
        private String elementPath;

        @DialogField(
                name = "fieldElementCheckbox",
                label = "label element checkbox",
                description = "description element checkbox",
                ranking = 4
        )
        @Checkbox
        private boolean elementCheckbox;

    }

    private static class ElementIconFieldSet extends ElementFieldSet {

        @DialogField(
                name = "field_element_icon",
                label = "label element icon",
                description = "description element icon",
                ranking = 3
        )
        @PathField(rootPath = "SHARED_ICONS_FOLDER_PATH")
        @Properties(@Property(name = "shared-icon-property", value = "shared icon value"))
        private String iconPath;

    }

    private static class VideoElementFieldSet {

        @DialogField(
                name = "fieldVideoResource",
                label = "label video resource",
                description = "description video resource",
                required = true
        )
        @PathField(rootPath = "/root/path")
        @Properties(@Property(name = "property", value = "video_resource_prop"))
        private String videoResource;
    }

    private static class FeedFieldSet {

        @DialogField(
                name = "field header",
                label = "label header",
                description = "description header",
                required = true
        )
        @TextField
        private String header;

        @DialogField(
                name = "field header_XS",
                label = "label header_XS",
                description = "description header_XS"
        )
        @TextField
        private String headerXS;

        @DialogField(
                name = "field_feed_channel",
                label = "label feed chanel",
                description = "description feed chanel",
                required = true
        )
        @PathField(rootPath = "/root/path")
        @Properties({
                @Property(name = "filter", value = "custom_filter")
        })
        private String feedChannel;

        @DialogField(
                name = "fieldFeedFilter",
                label = "label feed filter",
                description = "description feed filter"
        )
        @Autocomplete(
                multiple = true,
                datasource = @AutocompleteDatasource(namespaces = "test_name_space")
        )
        private String[] feedFilter;

        @DialogField(
                name = "checkbox_field",
                label = "label checkbox",
                description = "description checkbox"
        )
        @Checkbox
        private boolean checkbox;

        @DialogField(
                name = "switch_field",
                label = "switch label name"
        )
        @Switch(checked = true)
        private boolean switchField;
    }
}
