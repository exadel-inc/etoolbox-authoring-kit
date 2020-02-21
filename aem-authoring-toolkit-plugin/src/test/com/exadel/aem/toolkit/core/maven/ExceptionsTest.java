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

package com.exadel.aem.toolkit.core.maven;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.test.component.ComplexComponent1;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;
import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_TITLE;

public class ExceptionsTest extends ExceptionTestBase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testComponentWithInexistentTabSpecified() {
        testComponent(ComponentWithInexistentTab.class);
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME,
            title = DEFAULT_COMPONENT_TITLE,
            layout = DialogLayout.TABS,
            tabs = @Tab(title = "First tab")
    )
    private static class ComponentWithInexistentTab extends ComplexComponent1 {
    }
}
