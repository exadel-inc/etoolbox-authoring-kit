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

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

@SuppressWarnings("unused")
public class PlacementTestCases {

    private PlacementTestCases() {
    }

    @AemComponent(
        path = "test-component",
        title = "test-component-dialog"
    )
    @Dialog
    public static class Test1 {
        @TextField
        private String field0;

        @Place(before = @ClassMember("field0"))
        @TextField
        private String field1;

        @Place(before = @ClassMember("field6"))
        @TextField
        private String field2;

        @Place(before = @ClassMember("field2"))
        @TextField
        private String field3;

        @Place(after = @ClassMember("field2"))
        @TextField
        private String field4;

        @TextField
        private String field5;

        @Place(before = @ClassMember("field0"))
        @TextField
        private String field6;

        @Place(after = @ClassMember("field6"))
        @TextField
        private String field7;

        @TextField
        private String field8;

        @Place(before = @ClassMember(value = "field5"))
        @TextField
        private String field9;
    }

    @AemComponent(
        path = "test-component",
        title = "test-component-dialog"
    )
    @Dialog
    public static class Test2 implements Test2Interface {

        @Place(
            before = @ClassMember(value = "getField1", source = Test2Interface.class)
        )
        @TextField
        private String field0;

        @Place(
            after = @ClassMember(value = "field0"),
            before = @ClassMember(value = "getField3", source = Test2Interface.class)
        )
        @TextField
        private String field2;

        @TextField
        private String field4;

        @TextField
        private String field6;

        @TextField
        private String field8;
    }

    public interface Test2Interface {
        @Place(
            before = @ClassMember(value = "field2", source = Test2.class))
        @TextField
        default String getField1() {
            return null;
        }

        @Place(
            after = @ClassMember(value = "field2", source = Test2.class),
            before = @ClassMember(value = "field4", source = Test2.class))
        @TextField
        default String getField3() {
            return null;
        }

        @Place(
            after = @ClassMember(value = "field4", source = Test2.class),
            before = @ClassMember(value = "field6", source = Test2.class))
        @TextField
        default String getField5() {
            return null;
        }

        @Place(
            after = @ClassMember(value = "field6", source = Test2.class),
            before = @ClassMember(value = "field8", source = Test2.class))
        @TextField
        default String getField7() {
            return null;
        }

        @Place(after = @ClassMember(value = "field8", source = Test2.class))
        @TextField
        default String getField9() {
            return null;
        }
    }
}
