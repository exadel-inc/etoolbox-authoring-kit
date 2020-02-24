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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * The {@link Handler} for a tabbed TouchUI dialog
 */
public class TabsHandler implements Handler, BiConsumer<Class<?>, Element> {
    private static final String DEFAULT_TAB_NAME = "tab";
    private static final String NO_TABS_DEFINED_EXCEPTION_MESSAGE = "No tabs defined for the dialog at ";

    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, Element parentElement) {
        // Render the generic XML markup for tabs setting
        Element tabItemsElement = (Element) parentElement.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_TABS, ResourceTypes.TABS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        // Initialize tab instances registry to collect tabs from the current class and all superclasses, distinct by tab title
        Map<String, Tab> tabInstances = new LinkedHashMap<>();

        // Initialize fields registry to collect fields applicable to a certain tab from whatever class
        Map<String, List<Field>> tabFields = new HashMap<>();

        // Initialize ignored tabs list for the current class if IgnoreTabs annotation is present.
        // Note that "ignored tabs" setting is not inherited and is for current class only, unlike tabs collection
        String[] ignoredTabs = componentClass.isAnnotationPresent(IgnoreTabs.class)
                ? componentClass.getAnnotation(IgnoreTabs.class).value()
                : new String[] {};

        // Enumerate superclasses of the current class, itself included, from top to bottom, populate tab registry and
        // store fields that are withing @Tab-marked nested classes (as we will not have access to them later)
        for (Class<?> cls : PluginReflectionUtility.getAllSuperClasses(componentClass)) {
            List<Class<?>> tabClasses = Arrays.stream(cls.getDeclaredClasses())
                    .filter(nestedCls -> nestedCls.isAnnotationPresent(Tab.class))
                    .collect(Collectors.toList());
            Collections.reverse(tabClasses);
            tabClasses.forEach(nestedCls -> {
                        String tabTitle = nestedCls.getAnnotation(Tab.class).title();
                        tabInstances.put(tabTitle, nestedCls.getAnnotation(Tab.class));
                        tabFields.putIfAbsent(tabTitle, new LinkedList<>());
                        tabFields.get(tabTitle).addAll(Arrays.asList(nestedCls.getDeclaredFields()));
                    });
            if (cls.isAnnotationPresent(Dialog.class)) {
                Arrays.stream(cls.getAnnotation(Dialog.class).tabs())
                        .forEach(tab -> {
                            tabInstances.put(tab.title(), tab);
                            tabFields.putIfAbsent(tab.title(), new LinkedList<>());
                        });
            }
        }

        // Get all *non-nested* fields from superclasses and the current class
        List<Field> allFields = PluginReflectionUtility.getAllFields(componentClass);

        // If tabs collection is empty and yet there are fields to be placed, fire an exception and create a default tab
        if (tabInstances.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    NO_TABS_DEFINED_EXCEPTION_MESSAGE + componentClass.getSimpleName()
            ));
            tabInstances.put(StringUtils.EMPTY, PluginObjectUtility.create(Tab.class,
                    Collections.singletonMap(DialogConstants.PN_TITLE, StringUtils.EMPTY)));
            tabFields.putIfAbsent(StringUtils.EMPTY, new LinkedList<>());
        }

        // Iterate tab registry, from the first ever defined tab to the last
        // Within the iteration loop, we
        // 1) add fields from the "all fields" collection that are applicable to the current tab, to the tab's field collection
        // 2) re-sort the current tab's fields collection with the field ranking comparator
        // 3) remove managed fields from the "all fields" collection
        // 4) render XML markup for the current tab
        Iterator<Map.Entry<String, Tab>> tabIterator = tabInstances.entrySet().iterator();
        int iterationStep = 0;

        while (tabIterator.hasNext()) {
            final boolean isFirstTab = iterationStep++ == 0;
            Tab currentTab = tabIterator.next().getValue();
            List<Field> storedCurrentTabFields = tabFields.get(currentTab.title());
            List<Field> moreCurrentTabFields = allFields.stream()
                    .filter(field -> isFieldForTab(field, currentTab, isFirstTab))
                    .collect(Collectors.toList());
            boolean needResort = !storedCurrentTabFields.isEmpty() && ! moreCurrentTabFields.isEmpty();
            storedCurrentTabFields.addAll(moreCurrentTabFields);
            if (needResort) {
                storedCurrentTabFields.sort(PluginReflectionUtility.Predicates::compareDialogFields);
            }
            allFields.removeAll(moreCurrentTabFields);

            if (ArrayUtils.contains(ignoredTabs, currentTab.title())) {
                continue;
            }
            addTab(tabItemsElement, currentTab, storedCurrentTabFields);
        }

        // Afterwards there still can be "orphaned" fields in the "all fields" collection. They are probably fields
        // for which an non-existent tab was specified. Handle an InvalidTabException for each of them
        allFields.forEach(field -> PluginRuntime.context().getExceptionHandler()
                .handle(new InvalidTabException(
                        field.isAnnotationPresent(PlaceOnTab.class)
                                ? field.getAnnotation(PlaceOnTab.class).value()
                                : StringUtils.EMPTY
                )));
    }

    /**
     * Adds a tab definition to the XML markup
     * @param tabCollectionElement The {@link Element} instance to append particular fields' markup
     * @param tab The {@link Tab} instance to render as a dialog tab
     * @param fields The list of {@link Field} instances to render as dialog fields
     */
    private void addTab(Element tabCollectionElement, Tab tab, List<Field> fields){
        String nodeName = getXmlUtil().getUniqueName(tab.title(), DEFAULT_TAB_NAME, tabCollectionElement);
        Element tabElement = getXmlUtil().createNodeElement(
                nodeName,
                ImmutableMap.of(
                        JcrConstants.PN_TITLE, tab.title(),
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        tabCollectionElement.appendChild(tabElement);
        appendTabAttributes(tabElement, tab);
        Handler.appendToContainer(tabElement, fields);
    }

    /**
     * Appends tab attributes to a pre-built tab-defining XML element
     * @param tabElement {@link Element} instance representing a TouchUI dialog tab
     * @param tab {@link Tab} annotation that contains settings
     */
    private void appendTabAttributes(Element tabElement, Tab tab){
        tabElement.setAttribute(JcrConstants.PN_TITLE, tab.title());
        Attribute attribute = tab.attribute();
        getXmlUtil().mapProperties(tabElement, attribute);
        getXmlUtil().appendDataAttributes(tabElement, attribute.data());
    }

    /**
     * The predicate to match a {@code Field} against particular {@code Tab}
     * @param field  {@link Field} instance to analyze
     * @param tab {@link Tab} annotation to analyze
     * @param isDefaultTab True if the current tab accepts fields for which no tab was specified; otherwise, false
     * @return True or false
     */
    private static boolean isFieldForTab(Field field, Tab tab, boolean isDefaultTab) {
        if (!field.isAnnotationPresent(PlaceOnTab.class)) {
            return isDefaultTab;
        }
        return tab.title().equalsIgnoreCase(field.getAnnotation(PlaceOnTab.class).value());
    }
}
