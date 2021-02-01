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

package com.exadel.aem.toolkit.test.custom.handler.order;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.CustomAnnotationForOrderTest;

@Handles(value = CustomAnnotationForOrderTest.class, before = CustomHandler3.class, after = CustomHandler1.class)
public class CustomHandler1 implements DialogWidgetHandler {

    @Override
    public void accept(Source source, Target target) {
        target
            .attribute("ch1", "ch1")
            .attribute("ch2", "ch1")
            .attribute("ch3", "ch1")
            .attribute("ch4", "ch1")
            .attribute("ch5", "ch1");
    }
}
