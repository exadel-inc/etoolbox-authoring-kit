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
import com.exadel.aem.toolkit.api.annotations.layouts.LayoutType;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "Tabs Test Dialog"
)
@Dialog( // legacy tabs property is retained for compatibility testing
    tabs = {
        @com.exadel.aem.toolkit.api.annotations.container.Tab(title = "First tab"),
        @com.exadel.aem.toolkit.api.annotations.container.Tab(title = "Second tab"),
        @com.exadel.aem.toolkit.api.annotations.container.Tab(title = "Third tab")
    }
)
@SuppressWarnings({"unused", "deprecation"})
public class TabsWidget {
    @DialogField(label = "Field on the first tab")
    @TextField
    @Place("First tab")
    private String field1;

    @Tabs(
        value = {
            @Tab(title = "First Inner", trackingElement = "first"),
            @Tab(title = "Second Inner", trackingElement = "second", active = true, icon = "some/icon", padding = true)
        },
        maximized = true,
        trackingFeature = "feature1",
        trackingWidgetName = "widget1",
        type = LayoutType.LARGE, // this one should be ignored
        padding = false // this one should be ignored
    )
    @Place("First tab")
    private TabsExample tabsField;

    @DialogField(label = "Field on the second tab")
    @TextField
    @Place("Second tab")
    private String field2;

    @DialogField(description = "Field on the third tab")
    @TextField
    @Place("Third tab")
    String field3;

    private static class TabsExample {
        @DialogField(label = "Field 1 on the inner Tab")
        @TextField
        @Place("First Inner")
        String innerFieldInTab;

        @Accordion(value = @AccordionPanel(title = "First Panel", active = true))
        @Place("Second Inner")
        AccordionExample accordion;

        static class AccordionExample {
            @DialogField
            @TextField
            @Place("First Panel")
            String field6;
        }
    }
}
