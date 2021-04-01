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
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_TITLE;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_0;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.LABEL_TAB_1;

@SuppressWarnings("unused")
public class ExceptionsTestCases {

    @AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class ComponentWithNonexistentTab extends ComplexComponent1 {
        @TextField
        @Place(LABEL_TAB_1)
        String validField;

        @TextField
        @Place(LABEL_TAB_0)
        String invalidField;
    }

    @AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    @DependsOnTab(tabTitle = LABEL_TAB_1, query = "true")
    @DependsOnTab(tabTitle = LABEL_TAB_0, query = "true")
    public static class ComponentWithNonexistentDependsOnTab extends ComplexComponent1 {}
}
