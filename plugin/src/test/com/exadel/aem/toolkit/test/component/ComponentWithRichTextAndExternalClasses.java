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

import java.util.List;

import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = "Component with external classes",
        description = "Component with external classes",
        componentGroup = TestConstants.DEFAULT_COMPONENT_GROUP,
        resourceSuperType = "/resource/super/type",
        disableTargeting = true
)
@Dialog
@EditConfig(
        dropTargets = {
                @DropTargetConfig(
                        nodeName = "featuredimage1",
                        accept = {"image/.*"},
                        groups = {"media"},
                        propertyName = ComponentWithRichTextAndExternalClasses.PREFIX_FIRST + "Image1" + "reference1"
                ),
                @DropTargetConfig(
                        nodeName = "featuredimage2",
                        accept = {"image/.*"},
                        groups = {"media"},
                        propertyName = ComponentWithRichTextAndExternalClasses.PREFIX_SECOND + "Image2" + "reference2"
                )
        },
        inplaceEditing = {
                @InplaceEditingConfig(
                        title = "Label Header",
                        propertyName = "header",
                        type = "Type of inplace editing",
                        editElementQuery = ".cl-editable-header"
                ),
                @InplaceEditingConfig(
                        title = "Label description",
                        propertyName = "description",
                        type = EditorType.TEXT,
                        editElementQuery = ".cl-editable-description",
                        richText = @Extends(value = ComponentWithRichTextAndExternalClasses.class, field = "description")
                )
        }
)
@Tabs({
    @Tab(title = "Main tab"),
    @Tab(title = ComponentWithRichTextAndExternalClasses.TAB_LINKS),
    @Tab(title = ComponentWithRichTextAndExternalClasses.TAB_FEATURED_CARD_1),
    @Tab(title = ComponentWithRichTextAndExternalClasses.TAB_FEATURED_CARD_2)
})
@SuppressWarnings("unused")
public class ComponentWithRichTextAndExternalClasses {

    static final String TAB_LINKS = "Links";
    static final String TAB_FEATURED_CARD_1 = "Featured Card 1";
    static final String TAB_FEATURED_CARD_2 = "Featured Card 2";

    static final String PREFIX_FIRST = "first";
    static final String PREFIX_SECOND = "second_";

    private static final String FIELD_LINKS_LAYOUT = "linksLayout";
    private static final String DESCRIPTION_LINKS_LAYOUT = "Select layout for the Link List.";

    private static final String LABEL_LAYOUT_RIGHT = "Header to the right";
    private static final String LABEL_LAYOUT_LEFT = "Header to the left";
    private static final String VALUE_LAYOUT_RIGHT = "right";
    private static final String VALUE_LAYOUT_LEFT = "left";

    private static final String VALUE_BACKGROUND_WHITE = "white";
    private static final String VALUE_BACKGROUND_GREY = "grey";

    private static final String LABEL_LINKS_LAYOUT_SIMPLE = "Simple Link";
    private static final String LABEL_LINKS_LAYOUT_ICON = "Link With Icon";
    private static final String LABEL_LINKS_LAYOUT_NUMBERED = "Numbered Link";
    private static final String VALUE_LINKS_LAYOUT_SIMPLE = "simple";
    private static final String VALUE_LINKS_LAYOUT_ICON = "asset";
    private static final String VALUE_LINKS_LAYOUT_NUMBERED = "numbered";

    private static final String FIELD_LINKS = "links";
    private static final String LABEL_LINKS = "Links";
    private static final String FIELD_EXTENDED_LINKS = "extendedLinks";
    private static final String LABEL_EXTENDED_LINKS = "Extended Links";

    private static final String FIELD_ENABLE_SECOND_CARD = "enableSecondCard";
    private static final String LABEL_ENABLE_SECOND_CARD = "Enable Second Card?";
    private static final String DESCRIPTION_ENABLE_SECOND_CARD = "Check to enable second Featured Card.";

    @DialogField(
            name = "layout",
            label = "Layout",
            description = "Layout description",
            required = true
    )
    @Select(options = {
            @Option(text = LABEL_LAYOUT_RIGHT, value = VALUE_LAYOUT_RIGHT),
            @Option(text = LABEL_LAYOUT_LEFT, value = VALUE_LAYOUT_LEFT, selected = true),
    })
    private String layout;

    @DialogField(
            name = "Background Color",
            label = "Background color",
            description = "Background color description",
            required = true
    )
    @Select(options = {
            @Option(text = "White", value = VALUE_BACKGROUND_WHITE),
            @Option(text = "Grey", value = VALUE_BACKGROUND_GREY)
    })
    private String backgroundColor;

    @DialogField(
            name = "Header_name",
            label = "Header label",
            description = "Header description"
    )
    @TextField
    private String header;

    @DialogField(
            name = "Description Of Component",
            label = "Description's label",
            description = "description of component"
    )
    @RichTextEditor(
            features = {
                    RteFeatures.LINKS_MODIFYLINK,
                    RteFeatures.LINKS_UNLINK,
                    RteFeatures.SUBSUPERSCRIPT_SUBSCRIPT,
                    RteFeatures.SUBSUPERSCRIPT_SUPERSCRIPT
            }
    )
    private String description;

    @DialogField(
            name = FIELD_LINKS_LAYOUT,
            label = "Links Layout",
            description = DESCRIPTION_LINKS_LAYOUT,
            required = true)
    @RadioGroup(
            buttons = {
                    @RadioButton(text = LABEL_LINKS_LAYOUT_SIMPLE, value = VALUE_LINKS_LAYOUT_SIMPLE, checked = true),
                    @RadioButton(text = LABEL_LINKS_LAYOUT_ICON, value = VALUE_LINKS_LAYOUT_ICON),
                    @RadioButton(text = LABEL_LINKS_LAYOUT_NUMBERED, value = VALUE_LINKS_LAYOUT_NUMBERED)
            }
    )
    @Place(TAB_LINKS)
    private String linksLayout;

    @DialogField(
            name = FIELD_LINKS,
            label = LABEL_LINKS
    )
    @MultiField(value = SampleMultifieldBase.class)
    @Place(TAB_LINKS)
    private List<SampleMultifieldBase> links;

    @FieldSet(namePrefix = PREFIX_FIRST)
    @Place(TAB_FEATURED_CARD_1)
    @Attribute(className = "first-card", data = {@Data(name = "data-name", value = "data-value")})
    private SampleFieldsetBase1 firstCard;

    @DialogField(
            name = FIELD_ENABLE_SECOND_CARD,
            label = LABEL_ENABLE_SECOND_CARD,
            description = DESCRIPTION_ENABLE_SECOND_CARD
    )
    @Switch
    @Place(TAB_FEATURED_CARD_1)
    private boolean enableSecondCard;

    @FieldSet(namePrefix = PREFIX_SECOND)
    @Attribute(data = {@Data(name = "second-data-name", value = "second-data-value")})
    @Place(TAB_FEATURED_CARD_2)
    private SampleFieldsetBase1 secondCard;

    @DialogField(
            name = FIELD_EXTENDED_LINKS,
            label = LABEL_EXTENDED_LINKS
    )
    @MultiField(field = SampleMultifieldExtension.class)
    @Place(TAB_LINKS)
    private List<SampleMultifieldBase> extendedLinks;
}
