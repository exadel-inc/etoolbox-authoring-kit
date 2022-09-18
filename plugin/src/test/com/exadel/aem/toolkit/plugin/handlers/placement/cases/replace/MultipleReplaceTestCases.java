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
package com.exadel.aem.toolkit.plugin.handlers.placement.cases.replace;

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
public class MultipleReplaceTestCases {

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        writeMode = WriteMode.CREATE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class SameClassReplacement {

        @DialogField(
            label = "Title"
        )
        @TextField
        private int title;

        @DialogField(
            label = "Title Replace First"
        )
        @TextField
        @Replace(@ClassMember(value = "title"))
        private int replacementTitle;

        @DialogField(
            label = "Title Replace Second"
        )
        @TextField
        @Replace(@ClassMember(value = "title"))
        private int anotherReplacementTitle;
    }

    static class GrandParent {
        @DialogField(
            label = "Title"
        )
        @TextField
        private int title;
    }

    static class Parent extends GrandParent {
        @DialogField(
            label = "Title Replace First"
        )
        @TextField
        @Replace(@ClassMember(source = _Super.class, value = "title"))
        private int replacementTitle;
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        writeMode = WriteMode.CREATE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class HierarchyReplacement extends Parent {

        @DialogField(
            label = "Title Replace Second"
        )
        @TextField
        @Replace(@ClassMember(source = GrandParent.class, value = "title"))
        private int anotherReplacementTitle;
    }
}
