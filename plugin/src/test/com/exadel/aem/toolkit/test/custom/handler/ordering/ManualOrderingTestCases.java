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
package com.exadel.aem.toolkit.test.custom.handler.ordering;

import java.util.ArrayList;
import java.util.List;

import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.WidgetAnnotationForOrderingTest;

@SuppressWarnings("unused")
public class ManualOrderingTestCases {

    private static final List<String> ATTRIBUTE_LIST = new ArrayList<>(6);

    static {
        ATTRIBUTE_LIST.add("customHandler0");
        ATTRIBUTE_LIST.add("customHandler1");
        ATTRIBUTE_LIST.add("customHandler2");
        ATTRIBUTE_LIST.add("customHandler3");
        ATTRIBUTE_LIST.add("customHandler4");
        ATTRIBUTE_LIST.add("customHandler5");
    }

    @Handles(value = WidgetAnnotationForOrderingTest.class, before = CustomHandler1.class, after = CustomHandler0.class)
    public static class CustomHandler0 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(0), ATTRIBUTE_LIST.get(0))
                .attribute(ATTRIBUTE_LIST.get(1), ATTRIBUTE_LIST.get(0))
                .attribute(ATTRIBUTE_LIST.get(2), ATTRIBUTE_LIST.get(0))
                .attribute(ATTRIBUTE_LIST.get(3), ATTRIBUTE_LIST.get(0))
                .attribute(ATTRIBUTE_LIST.get(4), ATTRIBUTE_LIST.get(0))
                .attribute(ATTRIBUTE_LIST.get(5), ATTRIBUTE_LIST.get(0));
        }
    }

    @Handles(value = WidgetAnnotationForOrderingTest.class, before = CustomHandler3.class, after = CustomHandler1.class)
    public static class CustomHandler1 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(1), ATTRIBUTE_LIST.get(1))
                .attribute(ATTRIBUTE_LIST.get(2), ATTRIBUTE_LIST.get(1))
                .attribute(ATTRIBUTE_LIST.get(3), ATTRIBUTE_LIST.get(1))
                .attribute(ATTRIBUTE_LIST.get(4), ATTRIBUTE_LIST.get(1))
                .attribute(ATTRIBUTE_LIST.get(5), ATTRIBUTE_LIST.get(1));
        }
    }

    @Handles(value = WidgetAnnotationForOrderingTest.class, before = CustomHandler3.class ,after = CustomHandler0.class)
    public static class CustomHandler2 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(2), ATTRIBUTE_LIST.get(2))
                .attribute(ATTRIBUTE_LIST.get(3), ATTRIBUTE_LIST.get(2))
                .attribute(ATTRIBUTE_LIST.get(4), ATTRIBUTE_LIST.get(2))
                .attribute(ATTRIBUTE_LIST.get(5), ATTRIBUTE_LIST.get(2));
        }
    }

    @Handles(value = WidgetAnnotationForOrderingTest.class, before = CustomHandler0.class, after = CustomHandler3.class)
    public static class CustomHandler3 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(3), ATTRIBUTE_LIST.get(3))
                .attribute(ATTRIBUTE_LIST.get(4), ATTRIBUTE_LIST.get(3))
                .attribute(ATTRIBUTE_LIST.get(5), ATTRIBUTE_LIST.get(3));
        }
    }

    @Handles(value = WidgetAnnotationForOrderingTest.class, after = CustomHandler3.class)
    public static class CustomHandler4 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(4), ATTRIBUTE_LIST.get(4))
                .attribute(ATTRIBUTE_LIST.get(5), ATTRIBUTE_LIST.get(4));
        }
    }

    @Handles(value = WidgetAnnotationForOrderingTest.class, after = CustomHandler3.class)
    public static class CustomHandler5 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(5), ATTRIBUTE_LIST.get(5));
        }
    }
}
