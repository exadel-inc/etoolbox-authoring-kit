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

import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.handlers.container.common.CommonTabUtils;
import com.exadel.aem.toolkit.core.handlers.container.common.TabInstance;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlContainerUtility;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link Handler} for a tabbed TouchUI dialog
 */
public class TabsContainerHandler implements Handler, BiConsumer<Class<?>, Element> {
    private static final String DEFAULT_TAB_NAME = "tab";
    private static final String NO_TABS_DEFINED_EXCEPTION_MESSAGE = "No tabs defined for the dialog at ";

    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     *
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement  XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, Element parentElement) {
        // Render the generic XML markup for tabs setting
        Element tabItemsElement = (Element) parentElement.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_TABS, ResourceTypes.TABS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        // Initialize ignored tabs list for the current class if IgnoreTabs annotation is present.
        // Note that "ignored tabs" setting is not inherited and is for current class only, unlike tabs collection
        String[] ignoredTabs = componentClass.isAnnotationPresent(IgnoreTabs.class)
                ? componentClass.getAnnotation(IgnoreTabs.class).value()
                : new String[]{};

        // Retrieve superclasses of the current class, from top of the hierarchy to the most immediate ancestor,
        // populate tab registry and store fields that are within @Tab-marked nested classes
        // (because we will not have access to them later)
        Map<String, TabInstance> tabInstancesFromSuperClasses = getTabInstances(PluginReflectionUtility.getClassHierarchy(componentClass, false));

        // Retrieve tabs of the current class same way
        Map<String, TabInstance> tabInstancesFromCurrentClass = getTabInstances(Collections.singletonList(componentClass));

        // Compose the "overall" registry of tabs.
        // Whether the current class has any tabs that match tabs from superclasses,we consider that the "right" order
        // of tabs is defined herewith, and place tabs from the current class first, then rest of the tabs.
        // Otherwise, we consider the tabs of the current class to be an "addendum" of tabs from superclasses, and put
        // them in the end
        Map<String, TabInstance> allTabInstances;
        if (tabInstancesFromCurrentClass.keySet().stream().anyMatch(tabInstancesFromSuperClasses::containsKey)) {
            allTabInstances = Stream.concat(tabInstancesFromCurrentClass.entrySet().stream(), tabInstancesFromSuperClasses.entrySet().stream())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (child, parent) -> parent.merge(child),
                            LinkedHashMap::new));
        } else {
            allTabInstances = Stream.concat(tabInstancesFromSuperClasses.entrySet().stream(), tabInstancesFromCurrentClass.entrySet().stream())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            TabInstance::merge,
                            LinkedHashMap::new));
        }

        // Get all *non-nested* fields from superclasses and the current class
        List<Field> allFields = PluginReflectionUtility.getAllFields(componentClass);

        // If tabs collection is empty and yet there are fields to be placed, fire an exception and create a default tab
        if (allTabInstances.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidTabException(
                    NO_TABS_DEFINED_EXCEPTION_MESSAGE + componentClass.getSimpleName()
            ));
            Tab newTab = PluginObjectUtility.create(Tab.class,
                    Collections.singletonMap(DialogConstants.PN_TITLE, StringUtils.EMPTY));
            allTabInstances.put(StringUtils.EMPTY, new TabInstance(newTab));
        }

        // Iterate tab registry, from the first ever defined tab to the last
        // Within the iteration loop, we
        // 1) add fields from the "all fields" collection that are applicable to the current tab, to the tab's field collection
        // 2) re-sort the current tab's fields collection with the field ranking comparator
        // 3) remove managed fields from the "all fields" collection
        // 4) render XML markup for the current tab
        Iterator<Map.Entry<String, TabInstance>> tabInstanceIterator = allTabInstances.entrySet().iterator();
        int iterationStep = 0;

        while (tabInstanceIterator.hasNext()) {
            final boolean isFirstTab = iterationStep++ == 0;
            TabInstance currentTabInstance = tabInstanceIterator.next().getValue();
            List<List<Field>> tabFields = CommonTabUtils.getStoredCurrentTabFields(allFields, currentTabInstance, isFirstTab);
            List<Field> storedCurrentTabFields = tabFields.get(0);
            allFields = tabFields.get(1);
            if (ArrayUtils.contains(ignoredTabs, currentTabInstance.getTab().title())) {
                continue;
            }
            appendTab(tabItemsElement, currentTabInstance.getTab(), storedCurrentTabFields);
        }

        // Afterwards there still can be "orphaned" fields in the "all fields" collection. They are probably fields
        // for which a non-existent tab was specified. Handle an InvalidTabException for each of them
        CommonTabUtils.handleInvalidTabException(allFields);
    }

    private void appendTab(Element tabCollectionElement, Tab tab, List<Field> fields) {
        String nodeName = getXmlUtil().getUniqueName(tab.title(), DEFAULT_TAB_NAME, tabCollectionElement);
        Element tabElement = getXmlUtil().createNodeElement(
                nodeName,
                ImmutableMap.of(
                        JcrConstants.PN_TITLE, tab.title(),
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        tabCollectionElement.appendChild(tabElement);
        appendTabAttributes(tabElement, tab);
        PluginXmlContainerUtility.append(tabElement, fields);
    }

    /**
     * Appends tab attributes to a pre-built tab-defining XML element
     *
     * @param tabElement {@link Element} instance representing a TouchUI dialog tab
     * @param tab        {@link Tab} annotation that contains settings
     */
    private void appendTabAttributes(Element tabElement, Tab tab) {
        tabElement.setAttribute(JcrConstants.PN_TITLE, tab.title());
        Attribute attribute = tab.attribute();
        getXmlUtil().mapProperties(tabElement, attribute);
        getXmlUtil().appendDataAttributes(tabElement, attribute.data());
    }

    /**
     * Retrieves a collection of tabs derived from the specified hierarchical collection of classes. Calls to this
     * method are used to compile a "tab registry" consisting of all tabs from the current class and/or its superclasses
     *
     * @param classes The {@code Class<?>}-es to search for defined tabs
     * @return Map of entries, each specified by a tab title and containing a {@link TabInstance} aggregate object
     */
    public Map<String, TabInstance> getTabInstances(List<Class<?>> classes) {
        Map<String, TabInstance> result = new LinkedHashMap<>();
        for (Class<?> cls : classes) {
            List<Class<?>> tabClasses = Arrays.stream(cls.getDeclaredClasses())
                    .filter(nestedCls -> nestedCls.isAnnotationPresent(Tab.class))
                    .collect(Collectors.toList());
            Collections.reverse(tabClasses);
            tabClasses.forEach(nestedCls -> {
                String tabTitle = nestedCls.getAnnotation(Tab.class).title();
                result.put(
                        tabTitle,
                        new TabInstance(
                                nestedCls.getAnnotation(Tab.class),
                                Arrays.asList(nestedCls.getDeclaredFields())));
            });
            if (cls.isAnnotationPresent(Dialog.class)) {
                Arrays.stream(cls.getAnnotation(Dialog.class).tabs())
                        .forEach(tab -> result.put(tab.title(), new TabInstance(tab)));
            }
        }
        return result;
    }

}
