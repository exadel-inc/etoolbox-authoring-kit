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
package com.exadel.aem.toolkit.test.component.placement;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;
import com.exadel.aem.toolkit.api.markers._Super;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@SuppressWarnings("unused")
public class ReplaceTestCases {

    public static class Parent {

        @DialogField(
            label = "Title"
        )
        @TextField
        private int title;

        @DialogField(
            label = "Another Field"
        )
        @TextField
        private int anotherField;


        @DialogField(
            label = "Untouched Field"
        )
        @TextField
        private int untouchedField;
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        writeMode = WriteMode.CREATE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class Child extends Parent {

        @DialogField(
            label = "Title Child"
        )
        @TextField
        @Replace(@ClassMember(source = _Super.class, value = "title"))
        private int childTitle;


        @DialogField(
            label = "Another Field Child"
        )
        @TextField
        @Replace(@ClassMember(source = Parent.class, value = "anotherField"))
        private int anotherFieldChild;
    }
}
