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

import com.exadel.aem.toolkit.api.annotations.container.Accordion;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.google.common.collect.ImmutableMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.List;

/**
 * {@link Handler} implementation used to create markup responsible for Granite {@code Accordion} widget functionality
 * within the {@code cq:dialog} XML node
 */
class AccordionHandler implements WidgetSetHandler {
    private static final String DEFAULT_TAB_NAME = "accordion";
    private static final String EMPTY_ACCORDION_EXCEPTION_MESSAGE = "No valid fields found in accordion class ";

    /**
     * Processes the user-defined data and writes it to XML entity
     *
     * @param element Current XML element
     * @param field   Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {

        Element tabItemsElement = (Element) element
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));
        Accordion accordion = field.getDeclaredAnnotation(Accordion.class);
        Class<?> accordionType = field.getType();

        // Get the filtered fields collection for the current container; early return if collection is empty
        List<Field> fields = getContainerFields(element, field, accordionType);

        if (fields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    EMPTY_ACCORDION_EXCEPTION_MESSAGE + accordionType.getName()
            ));
            return;
        }
        addAccordion(tabItemsElement, accordion, fields);
    }

    private void addAccordion(Element tabCollectionElement, Accordion accordion, List<Field> fields) {
        String nodeName = getXmlUtil().getUniqueName(accordion.title(), DEFAULT_TAB_NAME, tabCollectionElement);
        Element tabElement = getXmlUtil().createNodeElement(
                nodeName,
                ImmutableMap.of(
                        JcrConstants.PN_TITLE, accordion.title(),
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        tabCollectionElement.appendChild(tabElement);
        Handler.appendToContainer(tabElement, fields);
    }
}
