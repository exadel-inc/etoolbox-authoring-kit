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
package com.exadel.aem.toolkit.core.handlers.container;

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOn;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.*;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The {@link Handler} for a tabbed TouchUI dialog
 */
public class AccordionContainerHandler implements Handler, BiConsumer<Class<?>, Element> {
    private static final String DEFAULT_TAB_NAME = "accordion";
    private static final String NO_TABS_DEFINED_EXCEPTION_MESSAGE = "No accordions defined for the dialog at ";

    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     *
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement  XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, Element parentElement) {
        Element tabItemsElement = (Element) parentElement.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ACCORDION, ResourceTypes.ACCORDION))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        String[] ignoredTabs = componentClass.isAnnotationPresent(IgnoreTabs.class)
                ? componentClass.getAnnotation(IgnoreTabs.class).value()
                : new String[]{};

        Map<String, AccordionPanel> tabInstances = new LinkedHashMap<>();

        Map<String, List<Field>> tabFields = new HashMap<>();

        for (Class<?> cls : PluginReflectionUtility.getClassHierarchy(componentClass)) {
            List<Class<?>> tabClasses = Arrays.stream(cls.getDeclaredClasses())
                    .filter(nestedCls -> nestedCls.isAnnotationPresent(AccordionPanel.class))
                    .collect(Collectors.toList());
            Collections.reverse(tabClasses);
            tabClasses.forEach(nestedCls -> {
                String tabTitle = nestedCls.getAnnotation(AccordionPanel.class).title();
                tabInstances.put(tabTitle, nestedCls.getAnnotation(AccordionPanel.class));
                tabFields.putIfAbsent(tabTitle, new LinkedList<>());
                tabFields.get(tabTitle).addAll(Arrays.asList(nestedCls.getDeclaredFields()));
            });
            if (cls.isAnnotationPresent(Dialog.class)) {
                Arrays.stream(cls.getAnnotation(Dialog.class).accordionTabs())
                        .forEach(accordionTab -> {
                            tabInstances.put(accordionTab.title(), accordionTab);
                            tabFields.putIfAbsent(accordionTab.title(), new LinkedList<>());
                        });
            }
        }

        List<Field> allFields = PluginReflectionUtility.getAllFields(componentClass);

        if (tabInstances.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    NO_TABS_DEFINED_EXCEPTION_MESSAGE + componentClass.getSimpleName()
            ));
            tabInstances.put(StringUtils.EMPTY, PluginObjectUtility.create(AccordionPanel.class,
                    Collections.singletonMap(DialogConstants.PN_TITLE, StringUtils.EMPTY)));
            tabFields.putIfAbsent(StringUtils.EMPTY, new LinkedList<>());
        }


        Iterator<Map.Entry<String, AccordionPanel>> tabIterator = tabInstances.entrySet().iterator();
        int iterationStep = 0;

        while (tabIterator.hasNext()) {
            final boolean isFirstTab = iterationStep++ == 0;
            AccordionPanel currentTab = tabIterator.next().getValue();
            List<Field> storedCurrentTabFields = tabFields.get(currentTab.title());
            List<Field> moreCurrentTabFields = allFields.stream()
                    .filter(field -> isFieldForAccordion(field, currentTab, isFirstTab))
                    .collect(Collectors.toList());
            boolean needResort = !storedCurrentTabFields.isEmpty() && !moreCurrentTabFields.isEmpty();
            storedCurrentTabFields.addAll(moreCurrentTabFields);
            if (needResort) {
                storedCurrentTabFields.sort(PluginObjectPredicates::compareByRanking);
            }
            allFields.removeAll(moreCurrentTabFields);
            if (ArrayUtils.contains(ignoredTabs, currentTab.title())) {
                continue;
            }
            addAccordion(tabItemsElement, currentTab, storedCurrentTabFields);
        }

        allFields.forEach(field -> PluginRuntime.context().getExceptionHandler()
                .handle(new InvalidTabException(
                        field.isAnnotationPresent(PlaceOn.class)
                                ? field.getAnnotation(PlaceOn.class).value()
                                : StringUtils.EMPTY
                )));
    }

    /**
     * Adds a accordion definition to the XML markup
     *
     * @param tabCollectionElement The {@link Element} instance to append particular fields' markup
     * @param accordionPanel       The {@link AccordionPanel} instance to render as a dialog accordion
     * @param fields               The list of {@link Field} instances to render as dialog fields
     */
    private void addAccordion(Element tabCollectionElement, AccordionPanel accordionPanel, List<Field> fields) {
        String nodeName = getXmlUtil().getUniqueName(accordionPanel.title(), DEFAULT_TAB_NAME, tabCollectionElement);
        Element tabElement = getXmlUtil().createNodeElement(
                nodeName,
                ImmutableMap.of(
                        JcrConstants.PN_TITLE, accordionPanel.title(),
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        tabCollectionElement.appendChild(tabElement);
        PluginXmlContainerUtility.append(tabElement, fields);
    }

    /**
     * The predicate to match a {@code Field} against particular {@code Accordion}
     *
     * @param field          {@link Field} instance to analyze
     * @param accordionPanel {@link AccordionPanel} annotation to analyze
     * @param isDefaultTab   True if the current accordion accepts fields for which no accordion was specified; otherwise, false
     * @return True or false
     */
    private static boolean isFieldForAccordion(Field field, AccordionPanel accordionPanel, boolean isDefaultTab) {
        if (!field.isAnnotationPresent(PlaceOn.class)) {
            return isDefaultTab;
        }
        return accordionPanel.title().equalsIgnoreCase(field.getAnnotation(PlaceOn.class).value());
    }
}
