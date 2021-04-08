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

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@AemComponent(
    path = TestConstants.DEFAULT_COMPONENT_NAME,
    title = TestConstants.DEFAULT_COMPONENT_TITLE
)
@Dialog
@SuppressWarnings("unused")
public class ComponentWithPanelsAsNestedClasses {

    @AccordionPanel(title = "First panel")
    private static class Panel1 {
        @DialogField(
                label = "Field 1",
                description = "This is the first field."
        )
        @TextField
        String field1;
    }

    @AccordionPanel(title = "Second panel")
    private static class Panel2 {
        @DialogField(
            label = "Field 2",
            description = "This is the second field."
        )
        @TextField
        String field2;
    }
}
