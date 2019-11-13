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

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.Autocomplete;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@link Handler} implementation for creating markup responsible for Granite UI {@code Multifield} widget functionality
 * within the {@code cq:dialog} XML node
 */
public class AutocompleteHandler implements Handler, BiConsumer<Element, Field> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param field Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {
        Autocomplete autocomplete = field.getDeclaredAnnotation(Autocomplete.class);
        Element datasource = getXmlUtil().createNodeElement(DialogConstants.NN_DATASOURCE, autocomplete.datasource().annotationType().getAnnotation(ResourceType.class).value());
        getXmlUtil().mapProperties(datasource, autocomplete.datasource());
        element.appendChild(datasource);

        Element options = getXmlUtil().createNodeElement(DialogConstants.NN_OPTIONS, autocomplete.options().annotationType().getAnnotation(ResourceType.class).value());
        getXmlUtil().mapProperties(options, autocomplete.options());
        element.appendChild(options);

        Element values = getXmlUtil().createNodeElement(DialogConstants.NN_VALUES, autocomplete.values().annotationType().getAnnotation(ResourceType.class).value());
        getXmlUtil().mapProperties(values, autocomplete.values());
        element.appendChild(values);
    }
}
