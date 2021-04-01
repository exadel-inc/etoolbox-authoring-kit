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

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAutoOrder;

@SuppressWarnings("unused")
public class AutoOrderingTestCases {

    private static final List<String> ATTRIBUTE_LIST = new ArrayList<>(3);

    static {
        ATTRIBUTE_LIST.add("customAutoOrder1");
        ATTRIBUTE_LIST.add("customAutoOrder2");
        ATTRIBUTE_LIST.add("customAutoOrder3");
    }

    @Handles(value = CustomWidgetAutoOrder.class)
    public static class CustomAutoOrder1 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            if (source.adaptTo(CustomWidgetAutoOrder.class) != null) {
                target
                    .attribute(ATTRIBUTE_LIST.get(0), ATTRIBUTE_LIST.get(0))
                    .attribute(ATTRIBUTE_LIST.get(1), ATTRIBUTE_LIST.get(0))
                    .attribute(ATTRIBUTE_LIST.get(2), ATTRIBUTE_LIST.get(0));
            }
        }
    }

    @Handles(value = CustomWidgetAutoOrder.class)
    @SuppressWarnings("deprecation") // Reference to DialogWidgetHandler retained for compatibility testing
    public static class CustomAutoOrder2 implements DialogWidgetHandler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(1), ATTRIBUTE_LIST.get(1))
                .attribute(ATTRIBUTE_LIST.get(2), ATTRIBUTE_LIST.get(1));
        }
    }

    @Handles(CustomWidgetAutoOrder.class)
    public static class CustomAutoOrder3 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(2), ATTRIBUTE_LIST.get(2));
        }
    }
}
