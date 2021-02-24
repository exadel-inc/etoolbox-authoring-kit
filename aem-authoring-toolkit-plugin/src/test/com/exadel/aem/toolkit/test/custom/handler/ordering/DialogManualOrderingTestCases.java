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

import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.DialogAnnotationForOrderingTest;

@SuppressWarnings("unused")
// Expected order 2 -> 3 -> 1
public class DialogManualOrderingTestCases {

    private static final List<String> ATTRIBUTE_LIST = new ArrayList<>(3);

    private static int counter = 0;

    private static final String ORDER = "order";

    static {
        ATTRIBUTE_LIST.add("property1");
        ATTRIBUTE_LIST.add("property2");
        ATTRIBUTE_LIST.add("property3");
    }

    @Handles(value = DialogAnnotationForOrderingTest.class, after = DialogHandler2.class)
    public static class DialogHandler1 implements DialogHandler {

        @Override
        public void accept(Class<?> source, Target target) {
            counter++;
            target
                .attribute(ATTRIBUTE_LIST.get(0), ORDER + counter);
        }
    }

    @Handles(value = DialogAnnotationForOrderingTest.class)
    public static class DialogHandler2 implements DialogHandler {

        @Override
        public void accept(Class<?> source, Target target) {
            counter++;
            target
                .attribute(ATTRIBUTE_LIST.get(1), ORDER + counter);
        }
    }

    @Handles(value = DialogAnnotationForOrderingTest.class, before = DialogHandler1.class)
    public static class DialogHandler3 implements DialogHandler {

        @Override
        public void accept(Class<?> source, Target target) {
            counter++;
            target
                .attribute(ATTRIBUTE_LIST.get(2), ORDER + counter);
        }
    }
}
