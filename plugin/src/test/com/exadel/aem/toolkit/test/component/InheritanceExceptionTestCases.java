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
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@SuppressWarnings("unused")
public class InheritanceExceptionTestCases {

    private static class Grandparent {
        @DialogField(description = "Grandparent.text1")
        @TextField
        private String text1;

        @DialogField(ranking = 1, description = "Grandparent.text2")
        @TextField
        private String text2;
    }

    private static class Parent extends Grandparent {

        @DialogField(description = "Parent.text1")
        @TextField
        private String text1; // will not cause an exception because of not being a rendering target

        @DialogField(description = "Parent.text2")
        @TextField
        private String text2; // will not cause an exception because of not being a rendering target
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class Child extends Parent {
        @DialogField(description = "Child.text1")
        @TextField
        private String text1; // will not cause an exception because placed underneath the field from superclass by ranking

        @DialogField(description = "Child.text2")
        @TextField
        private String text2; // *will* cause an exception because placed above the field from grandparent class by ranking
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class Child2 extends Parent {
        @DialogField(description = "Child.text1")
        @TextArea
        private String text1; // *will* cause an exception because exposing different resource type
    }
}

