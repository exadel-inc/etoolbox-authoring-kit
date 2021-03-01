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

package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;


public class PlaceWidget {

    @AemComponent(
        path = "test-component",
        title = "test-component-dialog"
    )
    @Dialog
    public static class Test1 {
        @TextField
        String field0;

        @Place(before = @ClassMember(name = "field0"))
        @TextField
        String field1;

        @Place(before = @ClassMember(name = "field6"))
        @TextField
        String field2;

        @Place(before = @ClassMember(name = "field2"))
        @TextField
        String field3;

        @Place(after = @ClassMember(name = "field2"))
        @TextField
        String field4;

        @TextField
        String field5;

        @Place(before = @ClassMember(name = "field0"))
        @TextField
        String field6;

        @Place(after = @ClassMember(name = "field6"))
        @TextField
        String field7;

        @TextField
        String field8;

        @Place(before = @ClassMember(name = "field5"))
        @TextField
        String field9;
    }

    @AemComponent(
        path = "test-component",
        title = "test-component-dialog"
    )
    @Dialog
    public static class Test2 implements ITest2 {

        @Place(
            before = @ClassMember(name = "field1()", source = ITest2.class)
        )
        @TextField
        String field0;

        @Place(
            after = @ClassMember(name = "field0"),
            before = @ClassMember(name = "field3()", source = ITest2.class)
        )
        @TextField
        String field2;

        @TextField
        String field4;

        @TextField
        String field6;

        @TextField
        String field8;
    }

    public interface ITest2 {

        @TextField
        default String getField1() {
            return null;
        }

        @Place(
            after = @ClassMember(name = "field2", source = Test2.class),
            before = @ClassMember(name = "field4", source = Test2.class))
        @TextField
        default String getField3() {
            return null;
        }

        @Place(
            after = @ClassMember(name = "field4", source = Test2.class),
            before = @ClassMember(name = "field6", source = Test2.class))
        @TextField
        default String getField5() {
            return null;
        }

        @Place(
            after = @ClassMember(name = "field6", source = Test2.class),
            before = @ClassMember(name = "field8", source = Test2.class))
        @TextField
        default String getField7() {
            return null;
        }

        @Place(after = @ClassMember(name = "field8", source = Test2.class))
        @TextField
        default String getField9() {
            return null;
        }
    }
}
