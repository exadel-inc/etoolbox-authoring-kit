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
package com.exadel.aem.toolkit.it.cases.ignorefreshness;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.it.cases.Constants;

@AemComponent(
    path = Constants.JCR_COMPONENTS_ROOT + "/ignoreFreshness/ignoring",
    title = "IgnoreFreshness Ignoring Component",
    componentGroup = Constants.GROUP_COMPONENTS,
    disableTargeting = true
)
@Dialog(forceIgnoreFreshness = true)
public class IgnoreFreshnessComponent {

    @DialogField(label = "Selection 1")
    @RadioGroup(buttons = {
        @RadioButton(text = "Option 1", value = "1"),
        @RadioButton(text = "Option 2", value = "2", checked = true)
    })
    private String selection1;

    @DialogField(label = "Selection 2")
    @Select(options = {
        @Option(text = "Option 1", value = "1"),
        @Option(text = "Option 2", value = "2", selected = true)
    })
    private String selection2;

}
