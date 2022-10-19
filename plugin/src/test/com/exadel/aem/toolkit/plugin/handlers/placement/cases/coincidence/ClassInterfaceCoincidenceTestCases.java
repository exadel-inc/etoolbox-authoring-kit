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
package com.exadel.aem.toolkit.plugin.handlers.placement.coincidence;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@SuppressWarnings("unused")
public class ClassInterfaceCoincidenceTestCases {

    private interface ClassInterface {
        @DialogField(
            label = "Title from interface method"
        )
        @TextField
        String getTitle();
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class NoIssue implements ClassInterface {

        @DialogField(
            label = "Title from field"
        )
        @TextField
        private String title;

        @Override
        public String getTitle() {
            return title;
        }
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class CoincidenceResolved implements ClassInterface {

        @DialogField(
            label = "Title from field"
        )
        @TextArea
        private String title;

        @DialogField(
            label = "Title from method"
        )
        @TextField
        @Override
        public String getTitle() {
            return title;
        }
    }


    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class CoincidenceException implements ClassInterface {

        @DialogField(
            label = "Title from field"
        )
        @TextArea
        private String title;

        @DialogField(
            label = "Title from method"
        )
        @TextArea
        @Override
        public String getTitle() {
            return title;
        }
    }
}
