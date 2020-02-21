/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.Ignore;

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

@Ignore(fields = {
        @ClassField(value = ComponentWithRichTextAndExternalClasses.class, field = "secondCard"),
        @ClassField(value = ComponentWithRichTextAndExternalClasses.class, field = "extendedLinks")
})
public class IgnoreFieldSetField extends ComponentWithRichTextAndExternalClasses {

}
