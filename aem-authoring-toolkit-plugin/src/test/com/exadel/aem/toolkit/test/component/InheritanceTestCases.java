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

    private static class DuplicateBase {
        @DialogField
        @TextField
        private String text1;

        @DialogField(ranking = 1)
        @TextField
        private String text2;
    }

    private static class DuplicateInterim extends DuplicateBase {

        @DialogField
        @TextField
        private String text1; // will not cause an exception because of not being a rendering target

        @DialogField
        @TextField
        private String text2; // will not cause an exception because of not being a rendering target
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    public static class DuplicateOverride extends DuplicateInterim {
        @DialogField
        @TextField
        private String text1; // will not cause an exception because placed underneath the field from superclass by order

        @DialogField
        @TextField
        private String text2; // will cause an exception because placed above the field from superclass by order
    }
}

