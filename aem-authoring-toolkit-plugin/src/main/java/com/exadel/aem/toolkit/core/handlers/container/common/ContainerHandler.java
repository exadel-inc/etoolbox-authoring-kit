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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOn;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.core.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectPredicates;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlContainerUtility;


public abstract class ContainerHandler implements Handler, BiConsumer<Class<?>, Element> {
    private static final Logger LOG = LoggerFactory.getLogger(ContainerHandler.class);

    static final String TABS_EXCEPTION = "No tabs defined for the dialog at ";
    static final String ACCORDION_EXCEPTION = "No accordions defined for the dialog at ";

    /**
     * Appends containerItem attributes to a pre-built containerItem-defining XML element
     * @param containerElement         {@link Element} instance representing a TouchUI dialog containerItem
     * @param containerItem            {@link ContainerInfo} stores information about current container item
     * @param defaultContainerItemName {@link String} name of current container containerItem
     */
    private static void appendContainerSection(Element containerElement, ContainerInfo containerItem, List<Field> fields, String defaultContainerItemName) {
        String nodeName = PluginRuntime.context().getXmlUtility().getUniqueName(containerItem.getTitle(), defaultContainerItemName, containerElement);
        Element containerItemElement = PluginRuntime.context().getXmlUtility().createNodeElement(
            nodeName,
            ImmutableMap.of(
                JcrConstants.PN_TITLE, containerItem.getTitle(),
                JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
            ));
        if (defaultContainerItemName.equals(DialogConstants.NN_TAB)) {
            Tab newTab = PluginObjectUtility.create(Tab.class,
                containerItem.getAttributes());
            appendTabAttributes(containerItemElement, newTab);
        } else if (defaultContainerItemName.equals(DialogConstants.NN_ACCORDION)) {
            AccordionPanel accordionPanel = PluginObjectUtility.create(AccordionPanel.class,
                containerItem.getAttributes());
            Element parentConfig = PluginRuntime.context().getXmlUtility().createNodeElement(
                DialogConstants.NN_PARENT_CONFIG);
            List<String> skippedList = new ArrayList<>();
            skippedList.add(DialogConstants.PN_TITLE);
            PluginRuntime.context().getXmlUtility().mapProperties(parentConfig, accordionPanel, skippedList);
            containerItemElement.appendChild(parentConfig);
        }
        containerElement.appendChild(containerItemElement);
        PluginXmlContainerUtility.append(containerItemElement, fields);
    }

    /**
     * Render XML markup for all existing tabs or accordion panels
     * @param allContainerItemInstances {@code Map<String, ContainerInfo >} Map where we store names of tabs or accordion panels as keys and all its fields as values
     * @param allFields                 {@code List<Field>} All *non-nested* fields from superclasses and the current class
     * @param ignoredContainerItems     {@link String[]} Array of ignored tabs or accordion panels for the current class
     * @param containerItemsElement     {@link Element} instance representing a TouchUI dialog tab or accordion panel
     * @param defaultContainerItemName  {@link String} name of current container item
     */
    static void addContainerElements(Map<String, ContainerInfo> allContainerItemInstances, List<Field> allFields, String[] ignoredContainerItems, Element containerItemsElement, String defaultContainerItemName) {
        // Iterate container item registry, from the first ever defined container item to the last
        // Within the iteration loop, we
        // 1) add fields from the "all fields" collection that are applicable to the current container item, to the container item's field collection
        // 2) re-sort the current container item's fields collection with the field ranking comparator
        // 3) remove managed fields from the "all fields" collection
        // 4) render XML markup for the current container item
        Iterator<Map.Entry<String, ContainerInfo>> containerItemInstanceIterator = allContainerItemInstances.entrySet().iterator();
        int iterationStep = 0;
        while (containerItemInstanceIterator.hasNext()) {
            final boolean isFirstContainerItem = iterationStep++ == 0;
            ContainerInfo currentContainerItemInstance
                = containerItemInstanceIterator.next().getValue();
            List<Field> storedCurrentContainerItemFields = new ArrayList<>();
            for (String key : currentContainerItemInstance.getFields().keySet()) {
                storedCurrentContainerItemFields.add((Field) currentContainerItemInstance.getFields().get(key));
            }
            List<Field> moreCurrentContainerItemFields = allFields.stream()
                .filter(field -> isFieldForContainerItem(field, currentContainerItemInstance.getTitle(), isFirstContainerItem))
                .collect(Collectors.toList());
            boolean needResort = !storedCurrentContainerItemFields.isEmpty() && !moreCurrentContainerItemFields.isEmpty();
            storedCurrentContainerItemFields.addAll(moreCurrentContainerItemFields);
            if (needResort) {
                storedCurrentContainerItemFields.sort(PluginObjectPredicates::compareByRanking);
            }
            allFields.removeAll(moreCurrentContainerItemFields);
            if (ArrayUtils.contains(ignoredContainerItems, currentContainerItemInstance.getTitle())) {
                continue;
            }
            appendContainerSection(containerItemsElement, currentContainerItemInstance, storedCurrentContainerItemFields, defaultContainerItemName);
        }
    }

    /**
     * Appends tab attributes to a pre-built tab-defining XML element
     * @param tabElement {@link Element} instance representing a TouchUI dialog tab
     * @param tab        {@link Tab} annotation that contains settings
     */
    private static void appendTabAttributes(Element tabElement, Tab tab) {
        tabElement.setAttribute(JcrConstants.PN_TITLE, tab.title());
        Attribute attribute = tab.attribute();
        PluginRuntime.context().getXmlUtility().mapProperties(tabElement, attribute);
        PluginRuntime.context().getXmlUtility().appendDataAttributes(tabElement, attribute.data());
    }

    /**
     * Handle an InvalidContainerException for fields for which a non-existent tab or accordion panel was specified
     * @param allFields     {@code List<Field>} all stored dialog fields
     * @param containerName {@link String} name of current container
     */
    static void handleInvalidContainerItemException(List<Field> allFields, String containerName) {
        for (Field field : allFields) {
            if (field.isAnnotationPresent(PlaceOnTab.class)) {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidContainerException(field.getAnnotation(PlaceOnTab.class).value(), containerName));
            } else if (field.isAnnotationPresent(PlaceOn.class)) {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidContainerException(field.getAnnotation(PlaceOn.class).value(), containerName));
            } else {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidContainerException(StringUtils.EMPTY, containerName));
            }
        }
    }

    /**
     * The predicate to match a {@code Field} against particular {@link Tab} or {@link AccordionPanel}
     * @param field                  {@link Field} instance to analyze
     * @param containerItemTitle     String annotation to analyze
     * @param isDefaultContainerItem True if the current container item accepts fields for which no container item was specified; otherwise, false
     * @return True or false
     */
    private static boolean isFieldForContainerItem(Field field, String containerItemTitle, boolean isDefaultContainerItem) {
        if (!field.isAnnotationPresent(PlaceOnTab.class) && !field.isAnnotationPresent(PlaceOn.class)) {
            return isDefaultContainerItem;
        }
        if (field.isAnnotationPresent(PlaceOn.class)) {
            return containerItemTitle.equalsIgnoreCase(field.getAnnotation(PlaceOn.class).value());
        }
        return containerItemTitle.equalsIgnoreCase(field.getAnnotation(PlaceOnTab.class).value());
    }

    /**
     * Compose the "overall" registry of container items
     * @param containerItemInstancesFromCurrentClass {@code Map<String, ContainerInfo >} Map where names of container items
     *                                               from current class are stored as keys and all its fields as values
     * @param containerItemInstancesFromSuperClasses {@code Map<String, ContainerInfo >} Map where names of container items
     *                                               from super classes are stored as keys and all their fields as values
     * @return {@code Map<String,ContainerInfo>} map containing all container items
     */
    private static Map<String, ContainerInfo> getAllContainerItemInstances(Map<String, ContainerInfo> containerItemInstancesFromCurrentClass, Map<String, ContainerInfo> containerItemInstancesFromSuperClasses) {
        // Compose the "overall" registry of container items.
        // Whether the current class has any container items that match container items from superclasses,we consider that the "right" order
        // of container items is defined herewith, and place container items from the current class first, then rest of the container items.
        // Otherwise, we consider the container items of the current class to be an "addendum" of container items from superclasses, and put
        // them in the end
        if (containerItemInstancesFromCurrentClass.keySet().stream().anyMatch(containerItemInstancesFromSuperClasses::containsKey)) {
            return Stream.concat(containerItemInstancesFromCurrentClass.entrySet().stream(), containerItemInstancesFromSuperClasses.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (child, parent) -> parent.merge(child),
                    LinkedHashMap::new));
        } else {
            return Stream.concat(containerItemInstancesFromSuperClasses.entrySet().stream(), containerItemInstancesFromCurrentClass.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    ContainerInfo::merge,
                    LinkedHashMap::new));
        }
    }

    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     * @param componentClass  {@code Class<?>} instance used as the source of markup
     * @param parentElement   XML document root element
     * @param annotationClass class of container items element
     */
    protected void acceptParent(Class<?> componentClass, Element parentElement, Class<? extends Annotation> annotationClass) {
        String containerName = annotationClass.equals(Tab.class) ? DialogConstants.NN_TABS : DialogConstants.NN_ACCORDION;
        String defaultContainerItemName = annotationClass.equals(Tab.class) ? DialogConstants.NN_TAB : DialogConstants.NN_ACCORDION;
        String exceptionMessage = annotationClass.equals(Tab.class) ? TABS_EXCEPTION : ACCORDION_EXCEPTION;
        String resourceType = annotationClass.equals(Tab.class) ? ResourceTypes.TABS : ResourceTypes.ACCORDION;

        Element containerTabItemsElement = (Element) parentElement.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER))
            .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS))
            .appendChild(getXmlUtil().createNodeElement(containerName, resourceType))
            .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        // Initialize ignored tabs or accordion panels list for the current class if IgnoreTabs annotation is present.
        // Note that "ignored tabs" setting is not inherited and is for current class only, unlike tabs collection
        String[] ignoredTabs = componentClass.isAnnotationPresent(IgnoreTabs.class)
            ? componentClass.getAnnotation(IgnoreTabs.class).value()
            : new String[]{};


        // Retrieve superclasses of the current class, from top of the hierarchy to the most immediate ancestor,
        // populate container item registry and store fields that are within @Tab or @AccordionPanel-marked nested classes
        // (because we will not have access to them later)
        Map<String, ContainerInfo> containerItemInstancesFromSuperClasses = getContainerElements(PluginReflectionUtility.getClassHierarchy(componentClass, false), annotationClass, containerName);

        // Retrieve tabs or accordions of the current class same way
        Map<String, ContainerInfo> containerItemsInstancesFromCurrentClass = getContainerElements(Collections.singletonList(componentClass), annotationClass, containerName);

        // Compose the "overall" registry of tabs or accordions.
        Map<String, ContainerInfo> allContainerItemInstances = getAllContainerItemInstances(containerItemsInstancesFromCurrentClass, containerItemInstancesFromSuperClasses);

        // Get all *non-nested* fields from superclasses and the current class
        List<Field> allFields = PluginReflectionUtility.getAllFields(componentClass);

        // If tabs or accordions collection is empty and yet there are fields to be placed, fire an exception and create a default tab
        if (allContainerItemInstances.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidContainerException(
                exceptionMessage + componentClass.getSimpleName(), containerName
            ));
            allContainerItemInstances.put(StringUtils.EMPTY, new ContainerInfo("Untitled"));
        }

        // Render XML markup for all existing container items
        addContainerElements(allContainerItemInstances, allFields, ignoredTabs, containerTabItemsElement, defaultContainerItemName);

        // Afterwards there still can be "orphaned" fields in the "all fields" collection. They are probably fields
        // for which a non-existent tab or accordion panel was specified. Handle an InvalidContainerItemException for each of them.
        handleInvalidContainerItemException(allFields, containerName);
    }

    /**
     * Retrieves a collection of container items derived from the specified hierarchical collection of classes. Calls to this
     * method are used to compile a "container item registry" consisting of all container items from the current class and/or its superclasses
     * @param classes         The {@code Class<?>}-es to search for defined container items
     * @param annotationClass The annotationClass are searching for
     * @param containerName   The name of current container
     * @return Map of entries, each specified by a container item title and containing a {@link ContainerInfo} aggregate object
     */
    private Map<String, ContainerInfo> getContainerElements(List<Class<?>> classes, Class<? extends Annotation> annotationClass, String containerName) {
        Map<String, ContainerInfo> result = new LinkedHashMap<>();
        Map<String, Object> annotationMap;
        try {
            for (Class<?> cls : classes) {
                List<Class<?>> containerItemClasses = Arrays.stream(cls.getDeclaredClasses())
                    .filter(nestedCls -> nestedCls.isAnnotationPresent(annotationClass))
                    .collect(Collectors.toList());
                Collections.reverse(containerItemClasses);
                for (Class<?> containerItemClass : containerItemClasses) {
                    Annotation currentAnnotation = containerItemClass.getDeclaredAnnotation(annotationClass);
                    annotationMap = PluginObjectUtility.getAnnotationFields(currentAnnotation);
                    ContainerInfo containerInfo = new ContainerInfo(annotationMap.get(DialogConstants.PN_TITLE).toString());
                    containerInfo.setAttributes(annotationMap);
                    Arrays.stream(containerItemClass.getDeclaredFields()).forEach(field -> containerInfo.setField(field.getName(), field));
                    result.put(annotationMap.get(DialogConstants.PN_TITLE).toString(), containerInfo);
                }
                if (cls.isAnnotationPresent(Dialog.class)) {
                    getCurrentDialogContainerElements(result, cls);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            LOG.error(exception.getMessage());
        }
        return result;
    }

    /**
     * Put all tabs or accordion panes from current Dialog to result Map
     * @param result {@code Map<String,ContainerInfo>} map containing all container items
     * @param cls    {@code Class<?>} current class that contains container elements
     */
    private void getCurrentDialogContainerElements(Map<String, ContainerInfo> result, Class<?> cls) {
        try {
            Map<String, Object> map = PluginObjectUtility.getAnnotationFields(cls.getDeclaredAnnotation(Dialog.class));
            List<Object> list = new ArrayList<>();
            List<Object> panelsAndTabs = Stream.concat(Stream.of(map.get(DialogConstants.NN_PANELS)), Stream.of(map.get(DialogConstants.NN_TABS))).collect(Collectors.toList());
            for (Object o : panelsAndTabs) {
                Object[] objects = (Object[]) o;
                list.addAll(Arrays.asList(objects));
            }
            list.forEach(item -> {
                try {
                    Map<String, Object> fields = PluginObjectUtility.getAnnotationFields((Annotation) item);
                    String title = (String) fields.get(DialogConstants.PN_TITLE);
                    ContainerInfo containerInfo = new ContainerInfo(title);
                    containerInfo.setAttributes(fields);
                    result.put(title, containerInfo);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    LOG.error(exception.getMessage());
                }
            });
        } catch (IllegalAccessException | InvocationTargetException exception) {
            LOG.error(exception.getMessage());
        }
    }
}
