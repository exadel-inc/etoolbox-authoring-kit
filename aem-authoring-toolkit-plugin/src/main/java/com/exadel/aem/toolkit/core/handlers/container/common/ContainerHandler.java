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

package com.exadel.aem.toolkit.core.handlers.container.common;

import com.exadel.aem.toolkit.api.annotations.container.*;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.*;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public interface ContainerHandler extends Handler, BiConsumer<Class<?>, Element> {
    default void acceptParent(Class<?> componentClass, Element parentElement, Class<? extends Annotation> annotation, String containerName, String resourceType, String exceptionMessage, String DEFAULT_TAB_NAME) {
        Element tabItemsElement = (Element) parentElement.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS))
                .appendChild(getXmlUtil().createNodeElement(containerName, resourceType))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        // Initialize ignored tabs list for the current class if IgnoreTabs annotation is present.
        // Note that "ignored tabs" setting is not inherited and is for current class only, unlike tabs collection
        String[] ignoredTabs = componentClass.isAnnotationPresent(IgnoreTabs.class)
                ? componentClass.getAnnotation(IgnoreTabs.class).value()
                : new String[]{};


        // Retrieve superclasses of the current class, from top of the hierarchy to the most immediate ancestor,
        // populate tab registry and store fields that are within @Tab-marked nested classes
        // (because we will not have access to them later)
        Map<String, TabContainerInstance> tabInstancesFromSuperClasses = getTabInstances(PluginReflectionUtility.getClassHierarchy(componentClass, false), annotation, containerName);

        // Retrieve tabs of the current class same way
        Map<String, TabContainerInstance> tabInstancesFromCurrentClass = getTabInstances(Collections.singletonList(componentClass), annotation, containerName);

        // Compose the "overall" registry of tabs.
        // Whether the current class has any tabs that match tabs from superclasses,we consider that the "right" order
        // of tabs is defined herewith, and place tabs from the current class first, then rest of the tabs.
        // Otherwise, we consider the tabs of the current class to be an "addendum" of tabs from superclasses, and put
        // them in the end
        Map<String, TabContainerInstance> allTabInstances;
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
                            TabContainerInstance::merge,
                            LinkedHashMap::new));
        }

        // Get all *non-nested* fields from superclasses and the current class
        List<Field> allFields = PluginReflectionUtility.getAllFields(componentClass);

        // If tabs collection is empty and yet there are fields to be placed, fire an exception and create a default tab
        if (allTabInstances.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidTabException(
                    exceptionMessage + componentClass.getSimpleName()
            ));
            if (containerName.equals("tabs")) {
                allTabInstances.put(StringUtils.EMPTY, new TabContainerInstance("newTab"));
            }
        }

        // Iterate tab registry, from the first ever defined tab to the last
        // Within the iteration loop, we
        // 1) add fields from the "all fields" collection that are applicable to the current tab, to the tab's field collection
        // 2) re-sort the current tab's fields collection with the field ranking comparator
        // 3) remove managed fields from the "all fields" collection
        // 4) render XML markup for the current tab
        Iterator<Map.Entry<String, TabContainerInstance>> tabInstanceIterator = allTabInstances.entrySet().iterator();
        int iterationStep = 0;

        while (tabInstanceIterator.hasNext()) {
            final boolean isFirstTab = iterationStep++ == 0;
            TabContainerInstance currentTabInstance
                    = tabInstanceIterator.next().getValue();
            List<Field> storedCurrentTabFields = new ArrayList<>();
            for (String key : currentTabInstance.getFields().keySet()) {
                storedCurrentTabFields.add((Field) currentTabInstance.getFields().get(key));
            }
            List<Field> moreCurrentTabFields = allFields.stream()
                    .filter(field1 -> isFieldForTab(field1, currentTabInstance.getTab(), isFirstTab))
                    .collect(Collectors.toList());
            boolean needResort = !storedCurrentTabFields.isEmpty() && !moreCurrentTabFields.isEmpty();
            storedCurrentTabFields.addAll(moreCurrentTabFields);
            if (needResort) {
                storedCurrentTabFields.sort(PluginObjectPredicates::compareByRanking);
            }
            allFields.removeAll(moreCurrentTabFields);
            if (ArrayUtils.contains(ignoredTabs, currentTabInstance.getTab())) {
                continue;
            }
            appendTab(tabItemsElement, currentTabInstance, storedCurrentTabFields, DEFAULT_TAB_NAME);
        }

        // Afterwards there still can be "orphaned" fields in the "all fields" collection. They are probably fields
        // for which a non-existent tab was specified. Handle an InvalidTabException for each of them
        CommonTabUtils.handleInvalidTabException(allFields);
    }

    default void appendTab(Element tabCollectionElement, TabContainerInstance tab, List<Field> fields, String DEFAULT_TAB_NAME) {
        String nodeName = getXmlUtil().getUniqueName(tab.getTab(), DEFAULT_TAB_NAME, tabCollectionElement);
        Element tabElement = getXmlUtil().createNodeElement(
                nodeName,
                ImmutableMap.of(
                        JcrConstants.PN_TITLE, tab.getTab(),
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        if (DEFAULT_TAB_NAME.equals("tab")) {
            Tab newTab = PluginObjectUtility.create(Tab.class,
                    tab.getAttributes());
            appendTabAttributes(tabElement, newTab);
        } else {
            AccordionPanel accordionPanel = PluginObjectUtility.create(AccordionPanel.class,
                    tab.getAttributes());
            List<String> list = new ArrayList<>();
            list.add("title");
            getXmlUtil().mapProperties(tabElement, accordionPanel, list);
        }
        tabCollectionElement.appendChild(tabElement);
        PluginXmlContainerUtility.append(tabElement, fields);
    }

    /**
     * Appends tab attributes to a pre-built tab-defining XML element
     *
     * @param tabElement {@link Element} instance representing a TouchUI dialog tab
     * @param tab        {@link Tab} annotation that contains settings
     */
    default void appendTabAttributes(Element tabElement, Tab tab) {
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
     * @return Map of entries, each specified by a tab title and containing a {@link TabContainerInstance} aggregate object
     */
    default Map<String, TabContainerInstance> getTabInstances(List<Class<?>> classes, Class<? extends Annotation> annotation, String containerName) {
        Map<String, TabContainerInstance> result = new LinkedHashMap<>();
        Map<String, Object> annotationMap;
        for (Class<?> cls : classes) {
            List<Class<?>> tabClasses = Arrays.stream(cls.getDeclaredClasses())
                    .filter(nestedCls -> nestedCls.isAnnotationPresent(annotation))
                    .collect(Collectors.toList());
            Collections.reverse(tabClasses);
            for (Class<?> tabClass : tabClasses) {
                Annotation annotation2 = tabClass.getDeclaredAnnotation(annotation);
                annotationMap = getAnnotationFields(annotation2);
                TabContainerInstance tabContainerInstance = new TabContainerInstance(annotationMap.get("title").toString());
                tabContainerInstance.setAttributes(annotationMap);
                Arrays.stream(tabClass.getDeclaredFields()).forEach(field -> tabContainerInstance.setFields(field.getName(), field));
                result.put(annotationMap.get("title").toString(), tabContainerInstance);
            }
            if (cls.isAnnotationPresent(Dialog.class)) {
                if (containerName.equals("tabs")) {
                    Arrays.stream(cls.getAnnotation(Dialog.class).tabs())
                            .forEach(tab -> {
                                TabContainerInstance tabContainerInstance = new TabContainerInstance(tab.title());
                                tabContainerInstance.setAttributes(getAnnotationFields(tab));
                                result.put(tab.title(), tabContainerInstance);
                            });
                } else {
                    Arrays.stream(cls.getAnnotation(Dialog.class).accordionTabs())
                            .forEach(tab -> {
                                TabContainerInstance tabContainerInstance = new TabContainerInstance(tab.title());
                                tabContainerInstance.setAttributes(getAnnotationFields(tab));
                                result.put(tab.title(), tabContainerInstance);
                            });
                }
            }
        }
        return result;
    }

    default Map<String, Object> getAnnotationFields(Annotation annotation) {
        try {
            Object handler = Proxy.getInvocationHandler(annotation);
            Field f = handler.getClass().getDeclaredField("memberValues");
            f.setAccessible(true);
            return (Map<String, Object>) f.get(handler);
        } catch (Exception ignored) {
            //ignored
        }
        return new HashMap<String, Object>();
    }

    static boolean isFieldForTab(Field field, String tabTitle, boolean isDefaultTab) {
        if (!field.isAnnotationPresent(PlaceOnTab.class) && !field.isAnnotationPresent(PlaceOn.class)) {
            return isDefaultTab;
        }
        if (field.isAnnotationPresent(PlaceOn.class)) {
            return tabTitle.equalsIgnoreCase(field.getAnnotation(PlaceOn.class).value());
        }
        return tabTitle.toString().equalsIgnoreCase(field.getAnnotation(PlaceOnTab.class).value());
    }
}
