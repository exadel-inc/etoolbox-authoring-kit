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
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
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

@Dialog(
        name = "test-component",
        title = "Component",
        description = "test component",
        componentGroup = "test_component_group",
        resourceSuperType = "resource/super/type",
        disableTargeting = true,
        isContainer = true,
        width = 800,
        tabs = {
                @Tab(title = "Tab_1"),
                @Tab(title = "Tab_2"),
                @Tab(title = "Tab_3"),
                @Tab(title = "Tab_4"),
                @Tab(title = "Tab_5"),
                @Tab(title = "Tab_6"),
                @Tab(title = "Tab_7")
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
                        richText = @Extends(value = ComplexComponent.class, field = "secondField")
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 1 Title",
                        propertyName = "primary1_topicTitle",
                        type = "Primary_type",
                        editElementQuery = ".editable-prtopic1title"
                ),
                @InplaceEditingConfig(
                        title = "Primary Topic 1 Description",
                        propertyName = "primary1_topicDescription",
                        type = EditorType.TEXT,
                        editElementQuery = ".editable-prtopic1description",
                        richText = @Extends(value = SampleDialog.class, field = "description")
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
                        richText = @Extends(value = SampleDialog.class, field = "description")
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
                        richText = @Extends(value = SampleDialog.class, field = "description")
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
                        richText = @Extends(value = SampleDialog.class, field = "description")
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
                        richText = @Extends(value = SampleDialog.class, field = "description")
                ),
        }
)
@SuppressWarnings("unused")
public class ComplexComponent {
    private static final String TAB_TOPIC_1 = "Topic 1";
    private static final String TAB_TOPIC_2 = "Topic 2";
    private static final String TAB_TOPIC_3 = "Topic 3";
    private static final String TAB_ADDITIONAL_TOPICS = "Additional Topics";

    private static final String PREFIX_FIRST_PRIMARY_DIALOG = "primary1";
    private static final String PREFIX_SECOND_PRIMARY_DIALOG = "primary2";
    private static final String PREFIX_THIRD_PRIMARY_DIALOG = "primary3";
    private static final String PREFIX_FIRST_EXAMPLE_DIALOG = "secondary1";
    private static final String PREFIX_SECOND_SECONDARY_DIALOG = "secondary2";

    private static final String FIELDSET_TITLE_FIRST_EXAMPLE_DIALOG = "Additional Topic #1";
    private static final String TITLE_SECOND_EXAMPLE_DIALOG = "Additional Topic #2";

    private static final String FIELD_FIRST_SECONDARY_ENABLED = "firstSecondaryEnabled";

    private static final String SECOND_EXAMPLE_DIALOG_ENABLED = "secondSecondaryEnabled";

    private static final String LABEL_SECONDARY_ENABLED = "Enable Additional Topic?";

    @DialogField(
            name = "first_field",
            label = "First field label",
            description = "First field description",
            required = true
    )
    @TextField
    @PlaceOnTab("Tab_1")
    private String firstField;

    @DialogField(
            name = "second_field",
            label = "Second field label",
            description = "Second field description"
    )
    @RichTextEditor(
            features = {
                    RteFeatures.LINKS_MODIFYLINK,
                    RteFeatures.LINKS_UNLINK,
                    RteFeatures.SUBSUPERSCRIPT_SUBSCRIPT,
                    RteFeatures.SUBSUPERSCRIPT_SUPERSCRIPT
            }
    )
    @PlaceOnTab("Tab_1")
    private String secondField;

    @DialogField(
            name = "third_Field",
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
    @PlaceOnTab("Tab_1")
    private String thirdField;

    @DialogField
    @FieldSet(namePrefix = PREFIX_FIRST_PRIMARY_DIALOG)
    @PlaceOnTab(TAB_TOPIC_1)
    private SamplePrimaryDialog firstPrimaryDialog;

    @DialogField
    @FieldSet(namePrefix = PREFIX_SECOND_PRIMARY_DIALOG)
    @PlaceOnTab(TAB_TOPIC_2)
    private SamplePrimaryDialog secondPrimaryDialog;

    @DialogField
    @FieldSet(namePrefix = PREFIX_THIRD_PRIMARY_DIALOG)
    @PlaceOnTab(TAB_TOPIC_3)
    private SamplePrimaryDialog thirdPrimaryDialog;

    @DialogField(
            name = FIELD_FIRST_SECONDARY_ENABLED,
            label = LABEL_SECONDARY_ENABLED
    )
    @Switch
    @DependsOnRef(name = "first")
    @PlaceOnTab(TAB_ADDITIONAL_TOPICS)
    private boolean firstDialogEnabled;

    @DialogField
    @FieldSet(
            title = FIELDSET_TITLE_FIRST_EXAMPLE_DIALOG,
            namePrefix = PREFIX_FIRST_EXAMPLE_DIALOG
    )
    @DependsOn(query = "@first")
    @PlaceOnTab(TAB_ADDITIONAL_TOPICS)
    private SampleDialog firstExampleDialog;

    @DialogField(
            name = SECOND_EXAMPLE_DIALOG_ENABLED,
            label = LABEL_SECONDARY_ENABLED
    )
    @Switch
    @PlaceOnTab(TAB_ADDITIONAL_TOPICS)
    private boolean secondExampleDialogEnabled;

    @DialogField
    @FieldSet(
            title = TITLE_SECOND_EXAMPLE_DIALOG,
            namePrefix = PREFIX_SECOND_SECONDARY_DIALOG
    )
    @PlaceOnTab(TAB_ADDITIONAL_TOPICS)
    private SampleDialog secondExampleDialogDialog;

    @DialogField(
            name = "first_number_field",
            label = "First number field label",
            description = "First number field description"
    )
    @TextField
    @NumberField(min = 0)
    @PlaceOnTab("Tab_6")
    private Integer exampleFirstNumberField;

    @DialogField(
            name = "second_number_field",
            label = "Second number field label",
            description = "Second number field description"
    )
    @TextField
    @NumberField(min = 0)
    @PlaceOnTab("Tab_6")
    private Integer exampleSecondNumberField;

    @DialogField
    @FieldSet
    @PlaceOnTab("Tab_7")
    private SampleFieldSet exampleFieldSet;
}