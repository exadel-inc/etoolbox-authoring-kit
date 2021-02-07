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

package com.exadel.aem.toolkit.plugin.handlers.container.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.PlaceOnSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.source.Sources;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginContainerUtility;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;
import com.exadel.aem.toolkit.plugin.util.PluginObjectUtility;
import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.plugin.util.PluginXmlUtility;
import com.exadel.aem.toolkit.plugin.util.stream.Sorter;

public abstract class ContainerHandler implements BiConsumer<Class<?>, Target> {
    private static final Logger LOG = LoggerFactory.getLogger(ContainerHandler.class);

    static final String TABS_EXCEPTION = "No tabs defined for the dialog at ";
    static final String ACCORDION_EXCEPTION = "No accordion panels defined for the dialog at ";

    private static final String DEFAULT_CONTAINER_SECTION_TITLE = "Untitled";


    /* -----------------------
       Inheritable class logic
       ----------------------- */

    /**
     * Implements {@code BiConsumer<Class<?>, Target>} pattern
     * to process component-backing Java class and append the results to the XML root node
     * @param componentClass  {@code Class<?>} instance used as the source of markup
     * @param target   XML document root element
     * @param annotationClass class of container items element
     */
    protected void populateContainer(Class<?> componentClass, Target target, Class<? extends Annotation> annotationClass) {
        String containerName = annotationClass.equals(Tab.class) ? DialogConstants.NN_TABS : DialogConstants.NN_ACCORDION;
        String containerSectionName = annotationClass.equals(Tab.class) ? DialogConstants.NN_TAB : DialogConstants.NN_ACCORDION;
        String resourceType = annotationClass.equals(Tab.class) ? ResourceTypes.TABS : ResourceTypes.ACCORDION;
        String exceptionMessage = annotationClass.equals(Tab.class) ? TABS_EXCEPTION : ACCORDION_EXCEPTION;

        Target containerItemsElement = target
            .createTarget(DialogConstants.NN_CONTENT)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER)
            .createTarget(DialogConstants.NN_ITEMS)
            .createTarget(containerName)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, resourceType)
            .createTarget(DialogConstants.NN_ITEMS);

        // Initialize ignored sections (tabs or accordion panels) list for the current class.
        // Note that "ignored sections" setting is not inherited and is for current class only, unlike the very collection
        // of tabs or panels
        String[] ignoredSections = ArrayUtils.EMPTY_STRING_ARRAY;
        if (componentClass.isAnnotationPresent(IgnoreTabs.class)) {
            ignoredSections = componentClass.getAnnotation(IgnoreTabs.class).value();
        }
        if (componentClass.isAnnotationPresent(Ignore.class) && componentClass.getAnnotation(Ignore.class).sections().length > 0) {
            ignoredSections = ArrayUtils.addAll(ignoredSections, componentClass.getAnnotation(Ignore.class).sections());
        }

        // Retrieve superclasses of the current class, from top of the hierarchy to the most immediate ancestor,
        // populate container section registry and store fields that are within @Tab or @AccordionPanel-marked nested classes
        // (because we will not have access to them later)
        Map<String, ContainerSection> containerSectionsFromSuperClasses = getContainerSections(PluginReflectionUtility.getClassHierarchy(componentClass, false), annotationClass, target.getScope());

        // Retrieve tabs or accordions of the current class same way
        Map<String, ContainerSection> containerSectionsFromCurrentClass = getContainerSections(Collections.singletonList(componentClass), annotationClass, target.getScope());

        // Compose the "overall" registry of tabs or accordions.
        Map<String, ContainerSection> allContainerSections = mergeSectionsFromCurrentClassAndSuperclasses(containerSectionsFromCurrentClass, containerSectionsFromSuperClasses);

        // Get all *non-nested* fields from the superclasses, and from the current class
        List<Source> allSources = PluginReflectionUtility.getAllSources(componentClass);

        // If tabs or accordions collection is empty and yet there are fields to be placed, fire an exception and create a default tab
        if (allContainerSections.isEmpty() && !allSources.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidContainerException(
                exceptionMessage + componentClass.getSimpleName(), containerName
            ));
            allContainerSections.put(StringUtils.EMPTY, new ContainerSection(DEFAULT_CONTAINER_SECTION_TITLE));
        }

        // Render XML markup for all existing container items
        addToContainer(containerItemsElement, allSources, allContainerSections, ignoredSections,  containerSectionName);

        // Afterwards there still can be "orphaned" sources in the "all sources" collection. They are probably members
        // for which a non-existent tab or accordion panel was specified. Handle an InvalidContainerItemException for each of them.
        handleInvalidContainerException(allSources, containerName);
    }

    /**
     * Retrieves a collection of container items derived from the specified hierarchical collection of classes. Calls to this
     * method are used to compile a "container item registry" consisting of all container items from the current class and/or its superclasses
     * @param classes         The {@code Class<?>}-es to search for defined container items
     * @param annotationClass The annotationClass are searching for
     * @param scope           Current XML scope
     * @return Map of entries, each specified by a container item title and containing a {@link ContainerSection} aggregate object
     */
    private Map<String, ContainerSection> getContainerSections(List<Class<?>> classes, Class<? extends Annotation> annotationClass, XmlScope scope) {
        Map<String, ContainerSection> result = new LinkedHashMap<>();
        Map<String, Object> annotationMap;
        try {
            for (Class<?> cls : classes) {
                List<Class<?>> containerSectionClasses = Arrays.stream(cls.getDeclaredClasses())
                    .filter(nestedCls -> nestedCls.isAnnotationPresent(annotationClass))
                    .collect(Collectors.toList());
                Collections.reverse(containerSectionClasses);
                for (Class<?> containerSectionClass : containerSectionClasses) {
                    Annotation currentAnnotation = containerSectionClass.getDeclaredAnnotation(annotationClass);
                    annotationMap = PluginObjectUtility.getAnnotationFields(currentAnnotation);
                    ContainerSection containerInfo = new ContainerSection(annotationMap.get(DialogConstants.PN_TITLE).toString());
                    containerInfo.setAttributes(annotationMap);
                    Arrays.stream(containerSectionClass.getDeclaredFields()).forEach(field -> containerInfo.setField(field.getName(), field));
                    result.put(annotationMap.get(DialogConstants.PN_TITLE).toString(), containerInfo);
                }
                if (cls.isAnnotationPresent(Dialog.class) || cls.isAnnotationPresent(DesignDialog.class)) {
                    appendSectionsFromClass(result, cls, scope);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            LOG.error(exception.getMessage());
        }
        return result;
    }

    /**
     * Put all tabs or accordion panels from the current Dialog to the resulting map
     * @param result {@code Map<String,ContainerInfo>} map containing all container items
     * @param cls {@code Class<?>} current class that contains container elements
     */
    private void appendSectionsFromClass(Map<String, ContainerSection> result, Class<?> cls, XmlScope scope) {
        try {
            Map<String, Object> map;
            if (XmlScope.CQ_DIALOG.equals(scope)) {
                map = PluginObjectUtility.getAnnotationFields(cls.getDeclaredAnnotation(Dialog.class));
            } else {
                map = PluginObjectUtility.getAnnotationFields(cls.getDeclaredAnnotation(DesignDialog.class));
            }
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
                    ContainerSection containerInfo = new ContainerSection(title);
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

    /**
     * Compose the "overall" registry of container sections (such as tabs or accordion panels)
     * @param sectionsFromCurrentClass A {@code Map} storing names of container sections from current class
     * as keys and section details as values
     * @param sectionsFromSuperClasses A {@code Map} storing names of container sections from superclasses
     * as keys and section details as values
     * @return {@code Map} map containing all container sections
     */
    private static Map<String, ContainerSection> mergeSectionsFromCurrentClassAndSuperclasses(
        Map<String, ContainerSection> sectionsFromCurrentClass,
        Map<String, ContainerSection> sectionsFromSuperClasses) {
        // Whether the current class has any container items that match container items from superclasses, we consider that the "right" order
        // of container items is defined herewith, and place container items from the current class first, then rest of the container items.
        // Otherwise, we consider the container items of the current class to be an "addendum" of container items from superclasses, and put
        // them in the end
        if (sectionsFromCurrentClass.keySet().stream().anyMatch(sectionsFromSuperClasses::containsKey)) {
            return Stream.concat(sectionsFromCurrentClass.entrySet().stream(), sectionsFromSuperClasses.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (child, parent) -> parent.merge(child),
                    LinkedHashMap::new));
        } else {
            return Stream.concat(sectionsFromSuperClasses.entrySet().stream(), sectionsFromCurrentClass.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    ContainerSection::merge,
                    LinkedHashMap::new));
        }
    }


    /* ---------------
       Utility methods
       --------------- */

    /**
     * Render XML markup for all existing tabs or accordion panels
     * @param containerItems {@code Map<String, ContainerInfo >} Map where we store names of tabs or accordion panels as keys and all its fields as values
     * @param sources {@code List<Field>} All *non-nested* fields from superclasses and the current class
     * @param ignoredContainerItems {@link String[]} Array of ignored tabs or accordion panels for the current class
     * @param container {@link Target} instance representing a TouchUI dialog tab or accordion panel
     * @param containerItemName {@link String} name of current container item
     */
    static void addToContainer(
        Target container,
        List<Source> sources,
        Map<String, ContainerSection> containerItems,
        String[] ignoredContainerItems,
        String containerItemName) {

        // Iterate the container item registry, from the first ever defined container item to the last
        // Within the iteration loop, we
        // 1) add fields from the "all fields" collection that are applicable to the current container item, to the container item's field collection
        // 2) re-sort the current container item's fields collection with the field ranking comparator
        // 3) remove managed fields from the "all fields" collection
        // 4) render XML markup for the current container item
        Iterator<Map.Entry<String, ContainerSection>> containerItemInstanceIterator = containerItems.entrySet().iterator();
        int iterationStep = 0;
        while (containerItemInstanceIterator.hasNext()) {
            final boolean isFirstContainerItem = iterationStep++ == 0;
            ContainerSection currentContainerItemInstance
                = containerItemInstanceIterator.next().getValue();
            List<Source> storedCurrentContainerItemFields = new ArrayList<>();
            for (String key : currentContainerItemInstance.getFields().keySet()) {
                Member member = (Member) currentContainerItemInstance.getFields().get(key);
                storedCurrentContainerItemFields.add(Sources.fromMember(member, member.getDeclaringClass()));
            }
            List<Source> moreCurrentContainerItemFields = sources.stream()
                .filter(field -> isFieldForContainerItem(field, currentContainerItemInstance.getTitle(), isFirstContainerItem))
                .collect(Collectors.toList());
            boolean needResort = !storedCurrentContainerItemFields.isEmpty() && !moreCurrentContainerItemFields.isEmpty();
            storedCurrentContainerItemFields.addAll(moreCurrentContainerItemFields);
            if (needResort) {
                storedCurrentContainerItemFields.sort(Sorter::compareByRank);
            }
            sources.removeAll(moreCurrentContainerItemFields);
            if (ArrayUtils.contains(ignoredContainerItems, currentContainerItemInstance.getTitle())) {
                continue;
            }
            addToContainerSection(container, storedCurrentContainerItemFields, currentContainerItemInstance, containerItemName);
        }
    }

    /**
     * Appends containerItem attributes to a pre-built containerItem-defining XML element
     * @param container {@link Element} instance representing a TouchUI dialog containerItem
     * @param containerItem {@link ContainerSection} stores information about the current container item
     * @param containerItemName {@link String} name of current container containerItem
     */
    private static void addToContainerSection(
        Target container,
        List<Source> sources,
        ContainerSection containerItem,
        String containerItemName) {

        String nodeName = PluginNamingUtility.getUniqueName(containerItem.getTitle(), containerItemName, container);
        Target containerItemsNode = container.createTarget(nodeName)
            .attribute(JcrConstants.PN_TITLE, containerItem.getTitle())
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);
        if (containerItemName.equals(DialogConstants.NN_TAB)) {
            Tab newTab = PluginObjectUtility.create(Tab.class,
                containerItem.getAttributes());
            appendTabAttributes(containerItemsNode, newTab);
        } else if (containerItemName.equals(DialogConstants.NN_ACCORDION)) {
            AccordionPanel accordionPanel = PluginObjectUtility.create(AccordionPanel.class,
                containerItem.getAttributes());
            containerItemsNode
                .createTarget(DialogConstants.NN_PARENT_CONFIG)
                .attributes(accordionPanel, member -> !member.getName().equals(DialogConstants.PN_TITLE));
        }
        PluginContainerUtility.appendToContainer(containerItemsNode, sources);
    }

    /**
     * Appends tab attributes to a pre-built tab-defining XML element
     * @param tabElement {@link Element} instance representing a TouchUI dialog tab
     * @param tab {@link Tab} annotation that contains settings
     */
    private static void appendTabAttributes(Target tabElement, Tab tab) {
        tabElement.attribute(JcrConstants.PN_TITLE, tab.title());
        Attribute attribute = tab.attribute();
        tabElement.attributes(attribute, PluginObjectUtility.getPropertyMappingFilter(attribute));
        PluginXmlUtility.appendDataAttributes(tabElement, attribute.data());
    }

    /**
     * The predicate to match a {@code Field} against particular {@link Tab} or {@link AccordionPanel}
     * @param source {@link Field} instance to analyze
     * @param containerItemTitle String annotation to analyze
     * @param isDefaultContainerItem True if the current container item accepts fields for which no container item was specified; otherwise, false
     * @return True or false
     */
    private static boolean isFieldForContainerItem(Source source, String containerItemTitle, boolean isDefaultContainerItem) {
        if (StringUtils.isBlank(source.adaptTo(PlaceOnSetting.class).getValue())) {
            return isDefaultContainerItem;
        }
        return containerItemTitle.equalsIgnoreCase(source.adaptTo(PlaceOnSetting.class).getValue());
    }

    /**
     * Handle an InvalidContainerException for sources (class members) for which a non-existent tab or accordion panel
     * was specified
     * @param sources {@code List<Field>} all stored dialog fields
     * @param containerName {@link String} name of current container
     */
    static void handleInvalidContainerException(List<Source> sources, String containerName) {
        for (Source source : sources) {
            InvalidContainerException ex = new InvalidContainerException(source.adaptTo(PlaceOnSetting.class).getValue(), containerName);
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }
    }
}
