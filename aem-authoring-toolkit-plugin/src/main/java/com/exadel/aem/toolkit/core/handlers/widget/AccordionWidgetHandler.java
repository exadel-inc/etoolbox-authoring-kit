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

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOn;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link Handler} implementation used to create markup responsible for Granite {@code Accordion} widget functionality
 * within the {@code cq:dialog} XML node
 */
class AccordionWidgetHandler implements WidgetSetHandler {
    private static final String DEFAULT_TAB_NAME = "accordion";
    private static final String EMPTY_ACCORDION_EXCEPTION_MESSAGE = "No valid fields found in accordion class ";
    private static final String NO_TABS_DEFINED_EXCEPTION_MESSAGE = "No accordions defined for the dialog at ";

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

        Map<String, AccordionPanel> panelInstancesFromCurrentClass = new LinkedHashMap<>();
        Map<String, List<Field>> tabFields = new HashMap<>();

        if (field.isAnnotationPresent(AccordionWidget.class)) {
            Arrays.stream(field.getAnnotation(AccordionWidget.class).panels())
                    .forEach(panel -> {
                        panelInstancesFromCurrentClass.put(panel.title(), panel);
                        tabFields.putIfAbsent(panel.title(), new LinkedList<>());
                    });
        }
        Class<?> accordionType = field.getType();

        List<Field> fields = getContainerFields(element, field, accordionType);

        if (panelInstancesFromCurrentClass.isEmpty() && !fields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    NO_TABS_DEFINED_EXCEPTION_MESSAGE + accordionType.getName()
            ));
            panelInstancesFromCurrentClass.put(StringUtils.EMPTY, PluginObjectUtility.create(AccordionPanel.class,
                    Collections.singletonMap(DialogConstants.PN_TITLE, StringUtils.EMPTY)));
        }

        Iterator<Map.Entry<String, AccordionPanel>> panelInstanceIterator = panelInstancesFromCurrentClass.entrySet().iterator();
        int iterationStep = 0;
        while (panelInstanceIterator.hasNext()) {
            final boolean isFirstTab = iterationStep++ == 0;
            AccordionPanel currentTab = panelInstanceIterator.next().getValue();
            List<Field> storedCurrentTabFields = tabFields.get(currentTab.title());
            List<Field> moreCurrentTabFields = fields.stream()
                    .filter(field1 -> isFieldForAccordion(field1, currentTab, isFirstTab))
                    .collect(Collectors.toList());
            boolean needResort = !storedCurrentTabFields.isEmpty() && !moreCurrentTabFields.isEmpty();
            storedCurrentTabFields.addAll(moreCurrentTabFields);
            if (needResort) {
                storedCurrentTabFields.sort(PluginReflectionUtility.Predicates::compareDialogFields);
            }
            fields.removeAll(moreCurrentTabFields);
            addAccordion(tabItemsElement, currentTab, storedCurrentTabFields);
        }
    }

    private void addAccordion(Element tabCollectionElement, AccordionPanel accordion, List<Field> fields) {
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

    private static boolean isFieldForAccordion(Field field, AccordionPanel accordionPanel, boolean isDefaultTab) {
        if (!field.isAnnotationPresent(PlaceOn.class)) {
            return isDefaultTab;
        }
        return accordionPanel.title().equalsIgnoreCase(field.getAnnotation(PlaceOn.class).value());
    }
}
