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
package com.exadel.aem.toolkit.test.component.placement.coincidences;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Heading;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@SuppressWarnings("unused")
public class ClassParentInterfaceCoincidenceTestCases {

    private interface ClassInterface {
        @DialogField(
            label = "Title from interface method"
        )
        @TextField
        String getTitle();
    }

    private static class ParentResolved implements ClassInterface {
        @DialogField(
            label = "Title from parent field"
        )
        @TextArea
        private String title;

        @DialogField(
            label = "Title from parent method"
        )
        @TextField
        @Place(before = @ClassMember("title"))
        public String getTitle() {
            return title;
        }
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class CoincidenceResolved extends ParentResolved {

        @DialogField(
            label = "Title from field"
        )
        @TextArea
        private String title;

        @DialogField(
            label = "Title from method"
        )
        @TextField
        @Place(before = @ClassMember("title"))
        @Override
        public String getTitle() {
            return title;
        }
    }


    private static class ParentException implements ClassInterface {
        @DialogField(
            label = "Title from parent field"
        )
        @TextArea
        private String title;

        @DialogField(
            label = "Title from parent method"
        )
        @TextField
        @Place(before = @ClassMember("title"))
        public String getTitle() {
            return title;
        }
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class CoincidenceException extends ParentException {

        @DialogField(
            label = "Title from field"
        )
        @TextArea
        private String title;

        @Heading
        @Place(before = @ClassMember("title"))
        @Override
        public String getTitle() {
            return title;
        }
    }

}
