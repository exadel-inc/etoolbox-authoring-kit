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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.core.SourceFacadeImpl;
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
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectPredicates;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlContainerUtility;

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

        // Initialize ignored tabs list for the current class if IgnoreTabs annotation is present.
        // Note that "ignored tabs" setting is not inherited and is for current class only, unlike tabs collection
        String[] ignoredTabs = componentClass.isAnnotationPresent(IgnoreTabs.class)
                ? componentClass.getAnnotation(IgnoreTabs.class).value()
                : new String[] {};

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
        List<SourceFacade> allSourceFacades = PluginReflectionUtility.getAllSourceFacades(componentClass);

        // If tabs collection is empty and yet there are fields to be placed, fire an exception and create a default tab
        if (allTabInstances.isEmpty() && !allSourceFacades.isEmpty()) {
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
            List<SourceFacade> storedCurrentTabFields = currentTabInstance.getFields();
            List<SourceFacade> moreCurrentTabFields = allSourceFacades.stream()
                    .filter(field -> isFieldForTab(field, currentTabInstance.getTab(), isFirstTab))
                    .collect(Collectors.toList());
            boolean needResort = !storedCurrentTabFields.isEmpty() && !moreCurrentTabFields.isEmpty();
            storedCurrentTabFields.addAll(moreCurrentTabFields);
            if (needResort) {
                storedCurrentTabFields.sort(PluginObjectPredicates::compareByRanking);
            }
            allSourceFacades.removeAll(moreCurrentTabFields);

            if (ArrayUtils.contains(ignoredTabs, currentTabInstance.getTab().title())) {
                continue;
            }
            appendTab(tabItemsElement, currentTabInstance.getTab(), storedCurrentTabFields);
        }

        // Afterwards there still can be "orphaned" fields in the "all fields" collection. They are probably fields
        // for which a non-existent tab was specified. Handle an InvalidTabException for each of them
        allSourceFacades.forEach(sourceFacade -> PluginRuntime.context().getExceptionHandler()
                .handle(new InvalidTabException(
                        sourceFacade.adaptTo(PlaceOnTab.class) != null
                                ? sourceFacade.adaptTo(PlaceOnTab.class).value()
                                : StringUtils.EMPTY
                )));
    }

    /**
     * Retrieves a collection of tabs derived from the specified hierarchical collection of classes. Calls to this
     * method are used to compile a "tab registry" consisting of all tabs from the current class and/or its superclasses
     * @param classes The {@code Class<?>}-es to search for defined tabs
     * @return Map of entries, each specified by a tab title and containing a {@link TabInstance} aggregate object
     */
    private Map<String, TabInstance> getTabInstances(List<Class<?>> classes) {
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
                                Arrays.stream(nestedCls.getDeclaredFields()).map(SourceFacadeImpl::new).collect(Collectors.toList())));
            });
            if (cls.isAnnotationPresent(Dialog.class)) {
                Arrays.stream(cls.getAnnotation(Dialog.class).tabs())
                        .forEach(tab -> result.put(tab.title(), new TabInstance(tab)));
            }
        }
        return result;
    }

    /**
     * Adds a tab definition to the XML markup
     * @param tabCollectionElement The {@link Element} instance to append particular sourceFacades' markup
     * @param tab The {@link Tab} instance to render as a dialog tab
     * @param sourceFacades The list of {@link Field} instances to render as dialog sourceFacades
     */
    private void appendTab(Element tabCollectionElement, Tab tab, List<SourceFacade> sourceFacades){
        String nodeName = getXmlUtil().getUniqueName(tab.title(), DEFAULT_TAB_NAME, tabCollectionElement);
        Element tabElement = getXmlUtil().createNodeElement(
                nodeName,
                ImmutableMap.of(
                        JcrConstants.PN_TITLE, tab.title(),
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        tabCollectionElement.appendChild(tabElement);
        appendTabAttributes(tabElement, tab);
        PluginXmlContainerUtility.append(tabElement, sourceFacades);
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
     * The predicate to match a {@code SourceFacade} against particular {@code Tab}
     * @param sourceFacade  {@link SourceFacade} instance to analyze
     * @param tab {@link Tab} annotation to analyze
     * @param isDefaultTab True if the current tab accepts fields for which no tab was specified; otherwise, false
     * @return True or false
     */
    private static boolean isFieldForTab(SourceFacade sourceFacade, Tab tab, boolean isDefaultTab) {
        if (sourceFacade.adaptTo(PlaceOnTab.class) == null) {
            return isDefaultTab;
        }
        return tab.title().equalsIgnoreCase(sourceFacade.adaptTo(PlaceOnTab.class).value());
    }


    /**
     * Represents an aggregate of {@link Tab} instance and a list of fields designed to be rendered within this tab.
     * Used to compose a sorted "tab registry" for a component class
     * @see TabsHandler#getTabInstances(List)
     */
    private static class TabInstance {
        private Tab tab;

        private List<SourceFacade> fields;

        /**
         * Creates a new {@code TabInstance} wrapped around a specified {@link Tab} with an empty list of associated fields
         * @param tab {@code Tab} object
         */
        private TabInstance(Tab tab) {
            this.tab = tab;
            this.fields = new LinkedList<>();
        }

        /**
         * Creates a new {@code TabInstance} wrapped around a specified {@link Tab} with a particular list of associated
         * fields
         * @param tab {@code Tab} object
         * @param fields List of {@code Field} objects to associate with the current tab
         */
        private TabInstance(Tab tab, List<SourceFacade> fields) {
            this.tab = tab;
            this.fields = new LinkedList<>(fields);
        }

        /**
         * Gets the stored {@link Tab}
         * @return {@code Tab} object
         */
        private Tab getTab() {
            return tab;
        }

        /**
         * Gets the stored list of {@link Field}s
         * @return {@code List<Field>} object
         */
        private List<SourceFacade> getFields() {
            return fields;
        }

        /**
         * Merges a foreign {@code TabInstance} to the current instance, basically by adding other instance's {@code Field}s
         * while preserving the same {@code Tab} reference
         * @param other Foreign {@code TabInstance} object
         * @return This instance
         */
        private TabInstance merge(TabInstance other) {
            this.fields.addAll(other.getFields());
            return this;
        }
    }
}
