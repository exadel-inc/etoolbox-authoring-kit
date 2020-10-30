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
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;

@Dialog(
        name = DEFAULT_COMPONENT_NAME,
        title = "Tabs Test Dialog",
        layout = DialogLayout.TABS,
        tabs = {
                @Tab(title = "First tab"),
                @Tab(title = "Second tab"),
                @Tab(title = "Third tab")
        }
)
@SuppressWarnings("unused")
public class Tabs {
    @DialogField(label = "Field on the first tab")
    @TextField
    @PlaceOnTab("First tab")
    String field1;

    @TabsWidget(
            title = "innerTabs",
            tabs = {
                    @Tab(title = "First Inner"),
                    @Tab(title = "Second Inner")
            })
    @PlaceOn("First tab")
    TabsExample tabsField;


    static class TabsExample {
        @DialogField(label = "Field 1 on the inner Tab")
        @TextField
        @PlaceOn("First Inner")
        String innerFieldInTab;

        @AccordionWidget(title = "Field 2 on the inner Tab", panels = {@AccordionPanel(title = "First Panel")})
        @PlaceOn("Second Inner")
        AccordionExample accordion;

        static class AccordionExample {
            @DialogField
            @TextField
            @PlaceOn("First Panel")
            String field6;
        }
    }

    @DialogField(label = "Field on the second tab")
    @TextField
    @PlaceOnTab("Second tab")
    String field2;

    @DialogField(description = "Field on the third tab")
    @TextField
    @PlaceOnTab("Third tab")
    String field3;
}
