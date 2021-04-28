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

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;
import com.exadel.aem.toolkit.test.custom.annotation.CustomEditConfigAnnotation;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_1;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_2;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_3;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_4;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_5;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_6;

@AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        description = "test component",
        componentGroup = TestConstants.DEFAULT_COMPONENT_GROUP,
        resourceSuperType = "resource/super/type",
        disableTargeting = true,
        isContainer = true
)
@Dialog(
    width = 800,
    extraClientlibs = {
        "cq.common.wcm",
        "core.wcm.page.properties",
        "cq.wcm.msm.properties"
    }
)
@EditConfig(
        actions = {
                ActionConstants.EDIT,
                ActionConstants.COPYMOVE,
                ActionConstants.INSERT,
                ActionConstants.DELETE
        },
        dropTargets = {
                @DropTargetConfig(
                        nodeName = "target1",
                        accept = {"targets/.*"},
                        groups = {"TargetsGroup"},
                        propertyName = "drop_target1/reference"
                ),
                @DropTargetConfig(
                        nodeName = "target2",
                        accept = {"targets/.*"},
                        groups = {"TargetsGroup"},
                        propertyName = "drop_target2/reference"
                ),
                @DropTargetConfig(
                        nodeName = "target3",
                        accept = {"targets/.*"},
                        groups = {"TargetsGroup"},
                        propertyName = "drop_target3/reference"
                ),
        },
        inplaceEditing = {
                @InplaceEditingConfig(
                        title = "Title",
                        propertyName = "title",
                        type = EditorType.TITLE,
                        editElementQuery = ".editable-title"
                ),
                @InplaceEditingConfig(
                        title = "Description",
                        propertyName = "description",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-description",
                        richText = @Extends(value = ComplexComponent1.class, field = "secondField")
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 1 Title",
                        propertyName = "../primary1_topicTitle",
                        type = "Primary_type",
                        editElementQuery = ".editable-prtopic1title"
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 1 Description",
                        propertyName = "primary1_topicDescription",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-prtopic1description",
                        richText = @Extends(value = SampleFieldsetAncestor.class, field = "description")
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 2 Title",
                        propertyName = "primary2_topicTitle",
                        type = "Primary_type",
                        editElementQuery = ".editable-prtopic2title"
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 2 Description",
                        propertyName = "primary2_topicDescription",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-prtopic2description",
                        richText = @Extends(value = SampleFieldsetAncestor.class, field = "description")
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 3 Title",
                        propertyName = "primary3_topicTitle",
                        type = "Primary_type",
                        editElementQuery = ".editable-prtopic3title"
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 3 Description",
                        propertyName = "primary3_topicDescription",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-prtopic3description",
                        richText = @Extends(value = SampleFieldsetAncestor.class, field = "description")
                ),
                @InplaceEditingConfig(
                        title = "Secondary Topic 1 Title",
                        propertyName = "secondary1_topicTitle",
                        type = "Secondary_type",
                        editElementQuery = ".editable-sectopic1title"
                ),
                @InplaceEditingConfig(
                        title = "Secondary Topic 1 Description",
                        propertyName = "secondary1_topicDescription",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-sectopic1description",
                        richText = @Extends(value = SampleFieldsetAncestor.class, field = "description")
                ),
                @InplaceEditingConfig(
                        title = "Secondary Topic 2 Title",
                        propertyName = "secondary2_topicTitle",
                        type = "Secondary_type",
                        editElementQuery = ".editable-sectopic2title"
                ),
                @InplaceEditingConfig(
                        title = "Secondary Topic 2 Description",
                        propertyName = "secondary2_topicDescription",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-sectopic2description",
                        richText = @Extends(value = SampleFieldsetAncestor.class, field = "description")
                ),
        }
)
@CustomEditConfigAnnotation
@Tabs({
    @Tab(title = LABEL_TAB_1),
    @Tab(title = LABEL_TAB_2),
    @Tab(title = LABEL_TAB_3),
    @Tab(title = LABEL_TAB_4),
    @Tab(title = LABEL_TAB_5),
    @Tab(title = LABEL_TAB_6)
})
@Accordion(@AccordionPanel(title = "Panel 1")) // Will be ignored because tab layout is detected
@SuppressWarnings("unused")
public class ComplexComponent1 extends ComplexComponent1Parent {
    private static final String PREFIX_FIRST_PRIMARY_DIALOG = "primary1";
    private static final String PREFIX_SECOND_PRIMARY_DIALOG = "primary2";
    private static final String PREFIX_THIRD_PRIMARY_DIALOG = "primary3";

    private static final String PREFIX_FIRST_SECONDARY_DIALOG = "secondary1";
    private static final String PREFIX_SECOND_SECONDARY_DIALOG = "secondary2";

    private static final String TITLE_FIRST_SECONDARY_DIALOG = "Additional Topic #1";
    private static final String TITLE_SECOND_SECONDARY_DIALOG = "Additional Topic #2";

    private static final String FIELD_FIRST_SECONDARY_DIALOG_ENABLED = "firstSecondaryDialogEnabled";

    private static final String FIELD_SECOND_SECONDARY_DIALOG_ENABLED = "secondSecondaryDialogEnabled";

    private static final String LABEL_SECONDARY_DIALOG_ENABLED = "Enable Additional Topic?";

    @DialogField(
            name = "field_with_colon_at_the_end:",
            label = "First field label",
            description = "First field description",
            validation = "foundation.jcr.name",
            required = true
    )
    @TextField
    @Place(LABEL_TAB_1)
    private String firstField;

    @DialogField(
            name = "field_with_slash_at_the_end/",
            label = "Second field label",
            description = "Second field description",
            validation = {"foundation.jcr.name1", "foundation.jcr.name2"}
    )
    @RichTextEditor(
            features = {
                    RteFeatures.LINKS_MODIFYLINK,
                    RteFeatures.LINKS_UNLINK,
                    RteFeatures.SUBSUPERSCRIPT_SUBSCRIPT,
                    RteFeatures.SUBSUPERSCRIPT_SUPERSCRIPT
            }
    )
    @Place(LABEL_TAB_1)
    private String secondField;

    @DialogField(
            name = "field_with:namespace",
            label = "Third field label",
            description = "Third field description",
            required = true
    )
    @Select(
            options = {
                    @Option(text = "First", value = "1"),
                    @Option(text = "Second", value = "2", selected = true),
                    @Option(text = "Third", value = "3"),
                    @Option(text = "Fourth", value = "4")
            })
    @Place(LABEL_TAB_1)
    private String thirdField;

    @DialogField
    @FieldSet(namePrefix = PREFIX_FIRST_PRIMARY_DIALOG, namePostfix = "postfix")
    @Place(LABEL_TAB_2)
    private SampleFieldsetBase2 firstPrimaryDialog;

    @DialogField
    @FieldSet(namePrefix = PREFIX_SECOND_PRIMARY_DIALOG)
    @Place(LABEL_TAB_2)
    private SampleFieldsetBase2 secondPrimaryDialog;

    @DialogField
    @FieldSet(namePrefix = PREFIX_THIRD_PRIMARY_DIALOG)
    @Place(LABEL_TAB_2)
    private SampleFieldsetBase2 thirdPrimaryDialog;

    @DialogField(
            name = FIELD_FIRST_SECONDARY_DIALOG_ENABLED,
            label = LABEL_SECONDARY_DIALOG_ENABLED
    )
    @Switch
    @DependsOnRef(name = "first")
    @Place(LABEL_TAB_3)
    private boolean firstSecondaryDialogEnabled;

    @DialogField
    @FieldSet(
            title = TITLE_FIRST_SECONDARY_DIALOG,
            namePrefix = PREFIX_FIRST_SECONDARY_DIALOG
    )
    @DependsOn(query = "@first")
    @Place(LABEL_TAB_3)
    private SampleFieldsetAncestor firstSecondaryDialog;

    @DialogField(
            name = FIELD_SECOND_SECONDARY_DIALOG_ENABLED,
            label = LABEL_SECONDARY_DIALOG_ENABLED
    )
    @Switch
    @Place(LABEL_TAB_3)
    private boolean secondSecondaryDialogEnabled;

    @DialogField
    @FieldSet(
            title = TITLE_SECOND_SECONDARY_DIALOG,
            namePrefix = PREFIX_SECOND_SECONDARY_DIALOG
    )
    @Place(LABEL_TAB_3)
    private SampleFieldsetAncestor secondSecondaryDialog;

    @DialogField(
            name = "first_number_field",
            label = "First number field label",
            description = "First number field description"
    )
    @TextField
    @NumberField(min = 0)
    @Place(LABEL_TAB_5)
    private Integer sampleFirstNumberField;

    @DialogField(
            name = "second_number_field",
            label = "Second number field label",
            description = "Second number field description"
    )
    @TextField
    @NumberField(min = 0)
    @Place(LABEL_TAB_5)
    private Integer sampleSecondNumberField;

    @DialogField
    @FieldSet(namePostfix = "/fieldset /on /tab#6")
    @Place(LABEL_TAB_6)
    private SampleFieldsetBase3 sampleFieldSet;
}

@AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME + "-parent",
        title = TestConstants.DEFAULT_COMPONENT_TITLE
)
@Dialog
@Tabs({
    @Tab(title = LABEL_TAB_3),
    @Tab(title = LABEL_TAB_2)
})
abstract class ComplexComponent1Parent {
}
