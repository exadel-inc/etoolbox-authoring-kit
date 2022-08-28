
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
import com.exadel.aem.toolkit.api.annotations.widgets.autocompletecoral3.Autocomplete;
import com.exadel.aem.toolkit.api.annotations.widgets.autocompletecoral3.AutocompleteOption;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "Autocomplete Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class AutocompleteWidget {

    private static final String ACS_LIST_PATH = "/path/to/acs/list";

    @DialogField(label = "Autocomplete")
    @Autocomplete(
        options = {
            @AutocompleteOption(text = "Text1", value = "value1"),
            @AutocompleteOption(text = "Text2", value = "value2")
        },
        placeholder = "placeholder",
        matchStartsWith = true,
        icon = "icon",
        multiple = true
    )
    String autocomplete;

    @DialogField(label = "Provided autocomplete")
    @Autocomplete(
        optionProvider = @OptionProvider(
            value = {
                @OptionSource(value = ACS_LIST_PATH),
                @OptionSource(value = ACS_LIST_PATH + "2", fallback = ACS_LIST_PATH + "3", textMember = "pageTitle"),
            },
            prepend = "None:none",
            selectedValue = "none",
            sorted = true
        )
    )
    String providedAutocomplete;
}
