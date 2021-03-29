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
import com.exadel.aem.toolkit.test.custom.annotation.DialogAnnotationForOrderingTest;

@SuppressWarnings("unused")
// Expected order 2 -> 3 -> 1
public class DialogManualOrderingTestCases {

    private static final List<String> ATTRIBUTE_LIST = new ArrayList<>(3);

    private static final String ATTRIBUTE_VALUE_ORDER = "order";

    static {
        ATTRIBUTE_LIST.add("property1");
        ATTRIBUTE_LIST.add("property2");
        ATTRIBUTE_LIST.add("property3");
    }

    private static int counter = 0;

    @Handles(value = DialogAnnotationForOrderingTest.class, after = DialogHandler2.class)
    public static class DialogHandler1 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            counter++;
            target
                .attribute(ATTRIBUTE_LIST.get(0), ATTRIBUTE_VALUE_ORDER + counter);
        }
    }

    @Handles(value = DialogAnnotationForOrderingTest.class)
    public static class DialogHandler2 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            counter++;
            target
                .attribute(ATTRIBUTE_LIST.get(1), ATTRIBUTE_VALUE_ORDER + counter);
        }
    }

    @Handles(value = DialogAnnotationForOrderingTest.class, before = DialogHandler1.class)
    public static class DialogHandler3 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            counter++;
            target
                .attribute(ATTRIBUTE_LIST.get(2), ATTRIBUTE_VALUE_ORDER + counter);
        }
    }
}
