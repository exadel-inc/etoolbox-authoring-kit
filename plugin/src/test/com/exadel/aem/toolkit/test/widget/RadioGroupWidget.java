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
package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "RadioGroup Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class RadioGroupWidget {
    @DialogField
    @RadioGroup(buttons = {
            @RadioButton(text = "Empty", value = ""),
            @RadioButton(text = "Blank", value = " "),
            @RadioButton(text = "Button 1", value = "1", checked = true),
            @RadioButton(text = "Button 2", value = "2"),
            @RadioButton(text = "Button 3", value = "3", disabled = true)
    })
    String group1;

    @DialogField
    @RadioGroup(
        datasource = @DataSource(path = "new/path", resourceType = "my/res/type")
    )
    String group2;


    @DialogField
    @RadioGroup(buttonProvider = @OptionProvider(
        value = @OptionSource(
            value = "/path/to/tags",
            textMember = "jcr:title",
            valueMember = "name",
            attributeMembers = {"first", "second"},
            textTransform = StringTransformation.CAPITALIZE,
            valueTransform = StringTransformation.LOWERCASE)
    ))
    String group3;
}
