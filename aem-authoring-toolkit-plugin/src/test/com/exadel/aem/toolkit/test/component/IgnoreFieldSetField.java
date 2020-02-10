package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.IgnoreFields;

@Dialog(
        name = "test-component",
        title = "Component with external classes",
        description = "Component with external classes",
        componentGroup = "componentsGroup",
        layout = DialogLayout.TABS,
        resourceSuperType = "/resource/super/type",
        tabs = {
                @Tab(title = "Main tab"),
                @Tab(title = ComponentWithRichTextAndExternalClasses.TAB_LINKS),
                @Tab(title = ComponentWithRichTextAndExternalClasses.TAB_FEATURED_CARD_1),
        },
        disableTargeting = true
)
@EditConfig(
        dropTargets = {
                @DropTargetConfig(
                        nodeName = "featuredimage1",
                        accept = {"image/.*"},
                        groups = {"media"},
                        propertyName = ComponentWithRichTextAndExternalClasses.PREFIX_FIRST + "Image1" + "reference1"
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

@IgnoreFields(ignoreFields = {
        @ClassField(value = ComponentWithRichTextAndExternalClasses.class, field = "secondCard"),
        @ClassField(value = ComponentWithRichTextAndExternalClasses.class, field = "extendedLinks")
})
public class IgnoreFieldSetField extends ComponentWithRichTextAndExternalClasses {

}
