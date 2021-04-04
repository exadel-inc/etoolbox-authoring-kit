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

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionVariant;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "Accordion Test Dialog"
)
@Dialog
@Accordion(
    value = {
        @AccordionPanel(title = "Basic", disabled = true),
        @AccordionPanel(title = "Basic2", active = true)
    }
)
@SuppressWarnings("unused")
public class AccordionWidget {

    @DialogField(label = "Field 1")
    @TextField
    @Place("Basic")
    private String field1;

    @Accordion(
        value = @AccordionPanel(title = "Accordion Widget Panel 1", disabled = true),
        variant = AccordionVariant.LARGE,
        margin = true
    )
    @Place("Basic2")
    AccordionFieldSet accordion;

    private static class AccordionFieldSet {
        @Place("Accordion Widget Panel 1")
        @DialogField
        @TextField
        String field6;
    }
}
