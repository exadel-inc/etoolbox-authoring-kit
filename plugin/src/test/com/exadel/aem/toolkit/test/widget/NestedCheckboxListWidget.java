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
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "Nested CheckboxList Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class NestedCheckboxListWidget {
    @Checkbox(text = "Level 1 Checkbox", sublist = Sublist.class)
    boolean option1L1;

    static class Sublist {
        @Checkbox(text = "Level 2 Checkbox 1")
        boolean option2L1;

        @Checkbox(text = "Level 2 Checkbox 2", sublist = Sublist2.class)
        boolean option2L2;
    }

    private static class Sublist2 {
        @Checkbox(text = "Level 3 Checkbox 1")
        boolean option3L1;

        @Checkbox(text = "Level 3 Checkbox 2")
        boolean option3L2;
    }
}
