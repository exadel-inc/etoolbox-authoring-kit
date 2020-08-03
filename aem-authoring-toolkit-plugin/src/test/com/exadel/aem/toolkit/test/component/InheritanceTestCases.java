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

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.core.util.TestConstants;

@SuppressWarnings("unused")
public class InheritanceTestCases {

    private static class InheritanceBase {
        @DialogField(
                label = "Base label",
                required = true,
                ranking = 1
        )
        @TextField
        private String text;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_TITLE,
            componentGroup = TestConstants.DEFAULT_COMPONENT_GROUP,
            resourceSuperType = TestConstants.DEFAULT_COMPONENT_SUPERTYPE,
            disableTargeting = true,
            layout = DialogLayout.FIXED_COLUMNS
    )
    public static class InheritanceOverride extends InheritanceBase {
        @DialogField(
                label = "Override label",
                required = true
        )
        @TextField
        private String text;

        @DialogField(name = "./fieldset")
        @FieldSet(title = "Fieldset")
        private FieldsetOverride fieldsetOverride;
    }

    private static class FieldsetBase {
        @DialogField(
                name = "baseFieldsetText",
                label = "Fieldset base label",
                ranking = 1
        )
        @TextField
        private String fieldsetText;
    }

    private static class FieldsetInterim extends FieldsetBase {
        @DialogField(
                name = "interimFieldsetText",
                label = "Fieldset interim label"
        )
        @TextField
        private String fieldsetText2;
    }

    private static class FieldsetOverride extends FieldsetInterim {
        @DialogField(
                name = "overrideFieldsetText",
                label = "Fieldset override label"
        )
        @TextField
        private String fieldsetText;
    }
}

