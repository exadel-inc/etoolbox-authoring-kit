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

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTab;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;
import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_TITLE;
import static com.exadel.aem.toolkit.core.util.TestConstants.LABEL_TAB_0;
import static com.exadel.aem.toolkit.core.util.TestConstants.LABEL_TAB_1;

@SuppressWarnings("unused")
public class ExceptionsTestCases {

    @Dialog(
            name = DEFAULT_COMPONENT_NAME,
            title = DEFAULT_COMPONENT_TITLE,
            layout = DialogLayout.TABS
    )
    public static class ComponentWithNonExistentTab extends ComplexComponent1 {
        @TextField
        @PlaceOnTab(LABEL_TAB_1)
        String validField;

        @TextField
        @PlaceOnTab(LABEL_TAB_0)
        String invalidField;
    }

    @Dialog(
            name = DEFAULT_COMPONENT_NAME,
            title = DEFAULT_COMPONENT_TITLE,
            layout = DialogLayout.TABS
    )
    @DependsOnTab(tabTitle = LABEL_TAB_1, query = "true")
    @DependsOnTab(tabTitle = LABEL_TAB_0, query = "true")
    public static class ComponentWithNonExistentDependsOnTab extends ComplexComponent1 {}

    @Dialog(
            name = DEFAULT_COMPONENT_NAME,
            title = DEFAULT_COMPONENT_TITLE,
            layout = DialogLayout.TABS
    )
    @HtmlTag(
            className = "wr@pper!",
            tagName = ""
    )
    public static class ComponentWithWrongHtmlTag1 {}

    @Dialog(
            name = DEFAULT_COMPONENT_NAME,
            title = DEFAULT_COMPONENT_TITLE,
            layout = DialogLayout.TABS
    )
    @HtmlTag(
            tagName = "..--"
    )
    public static class ComponentWithWrongHtmlTag2 {}
}
