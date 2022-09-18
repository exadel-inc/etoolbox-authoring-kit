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
package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@SuppressWarnings("unused")
public class ForceIgnoreFreshnessTestCases {

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog(forceIgnoreFreshness = true)
    public static class SimpleDialog {
        @DialogField(label = "Text")
        @TextField
        private String text;
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog(forceIgnoreFreshness = true)
    @Tabs({
        @Tab(title = "First"),
        @Tab(title = "Second")
    })
    public static class TabbedDialog {
        @DialogField(label = "Checkbox")
        @Checkbox(checked = true)
        @Place("First")
        private  boolean checkbox;

        @DialogField(label = "Text")
        @TextField
        @Place("Second")
        private String text;
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog(forceIgnoreFreshness = true)
    @Accordion({
        @AccordionPanel(title = "First"),
        @AccordionPanel(title = "Second")
    })
    public static class AccordionDialog {
        @DialogField(label = "Switch")
        @Switch
        @Place("First")
        private  boolean mySwitch;

        @DialogField(label = "Text")
        @TextField
        @Place("Second")
        private String text;
    }
}
