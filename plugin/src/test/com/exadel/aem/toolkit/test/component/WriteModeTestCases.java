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

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@SuppressWarnings("unused")
public class WriteModeTestCases {

    private WriteModeTestCases() {
    }

    @AemComponent(
        path = TestConstants.NONEXISTENT_COMPONENT_NAME,
        writeMode = WriteMode.CREATE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class FolderCreatingComponent {

        @DialogField(label = "Text input")
        @TextField
        private String text;
    }

    @AemComponent(
        path = TestConstants.NONEXISTENT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class FolderMissingComponent {

        @DialogField(label = "Text input")
        @TextField
        private String text;
    }
}
