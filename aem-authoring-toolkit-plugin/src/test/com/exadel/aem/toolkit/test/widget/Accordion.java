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

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOn;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.button.Button;
import com.exadel.aem.toolkit.api.annotations.widgets.button.ButtonType;
import com.exadel.aem.toolkit.api.annotations.widgets.common.ElementSizeConstants;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;

@Dialog(
        name = DEFAULT_COMPONENT_NAME,
        title = "Accordion Test Dialog",
        accordionTabs = {
                @AccordionPanel(title = "Basic"),
                @AccordionPanel(title = "Basic2")
        }
)
@SuppressWarnings("unused")
public class Accordion {

    @Button(
            type = ButtonType.SUBMIT,
            autocomplete = "on",
            formId = "test-form",
            text = "test-text",
            textComment = "test-comment",
            hideText = true,
            active = true,
            icon = "search",
            iconSize = ElementSizeConstants.LARGE,
            size = ElementSizeConstants.LARGE,
            block = true,
            command = "shift+s",
            trackingElement = "test-element",
            trackingFeature = "test-feature"
    )
    @PlaceOn("Basic")
    String buttonField1;

    @Button(
            type = ButtonType.SUBMIT,
            autocomplete = "on",
            formId = "test-form",
            text = "test-text",
            textComment = "test-comment",
            hideText = true,
            active = true,
            icon = "search",
            iconSize = ElementSizeConstants.LARGE,
            size = ElementSizeConstants.LARGE,
            block = true,
            command = "shift+s",
            trackingElement = "test-element",
            trackingFeature = "test-feature"
    )
    @PlaceOn("Basic2")
    String buttonField;

    @AccordionWidget(
            title = "Field",
            panels = {@AccordionPanel(title = "Accordion Widget Panel 1")})
    @PlaceOn("Basic2")
    AccordionExample accordion;

    static class AccordionExample {
        @PlaceOn("Accordion Widget Panel 1")
        @DialogField
        @TextField
        String field6;
    }

}
