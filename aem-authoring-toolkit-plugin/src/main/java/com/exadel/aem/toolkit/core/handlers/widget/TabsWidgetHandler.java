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
package com.exadel.aem.toolkit.core.handlers.widget;

import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.core.handlers.Handler;
import org.w3c.dom.Element;

import java.lang.reflect.Field;

/**
 * The {@link Handler} for a tabbed TouchUI dialog
 */
public class TabsWidgetHandler extends WidgetContainerHandler {

    /**
     * Processes the user-defined data and writes it to XML entity
     *
     * @param element Current XML element
     * @param field   Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {
        acceptParent(element, TabsWidget.class, field);
    }
}
