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
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.buttongroup.ButtonGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.buttongroup.ButtonGroupItem;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.SelectionMode;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "ButtonGroup Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class ButtonGroupWidget {
    private static final String ACS_LIST_PATH = "/path/to/acs/list";

    @DialogField(label = "Manual options list")
    @ButtonGroup(items = {
        @ButtonGroupItem(text = "Empty", value = ""),
        @ButtonGroupItem(text = "Blank", value = " "),
        @ButtonGroupItem(
            text = "One",
            value = "1",
            checked = true,
            icon = "/content/some/icon",
            size = Size.LARGE),
        @ButtonGroupItem(
            text = "Two",
            value = "2",
            disabled = true,
            hideText = true),
        },
        selectionMode = SelectionMode.SINGLE,
        ignoreData = true,
        deleteHint = false)
    String options;

    @DialogField(label = "Provided options list")
    @ButtonGroup(
        itemProvider = @OptionProvider(
            prepend = "None:none",
            append = "All:all",
            selectedValue = "none",
            sorted = true
        )
    )
    String optionList;
}
