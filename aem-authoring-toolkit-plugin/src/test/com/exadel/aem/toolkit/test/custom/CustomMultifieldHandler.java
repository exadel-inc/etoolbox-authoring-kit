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

package com.exadel.aem.toolkit.test.custom;

import java.lang.reflect.Field;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.HandlesWidgets;

@HandlesWidgets(MultiField.class)
@SuppressWarnings("unused")
public class CustomMultifieldHandler implements DialogWidgetHandler {
    @Override
    public String getName() {
        return "customMultifieldHandler";
    }

    @Override
    public void accept(Element element, Field field) {
        element.setAttribute("multifieldSpecial", "This is added to Multifields");
    }
}
