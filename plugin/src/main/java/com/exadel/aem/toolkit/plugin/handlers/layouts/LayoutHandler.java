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
package com.exadel.aem.toolkit.plugin.handlers.layouts;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.ContainerWidgetSetting;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.handlers.containers.PlacementHelper;
import com.exadel.aem.toolkit.plugin.handlers.containers.Section;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Presents a common base for handlers processing layout logic for Granite UI containers, such as the accordion
 * container, tab container, etc.
 */
abstract class LayoutHandler implements BiConsumer<Source, Target> {

    /* ---------------
       Layout routines
       --------------- */

    /**
     * Processes container-backing Java class and appends the results to the root {@link Target} object
     * @param componentClass  {@code Class<?>} instance used as the source of markup
     * @param target          The root of rendering for the current component
     * @param annotationClass {@code Class<?>} object representing types of container sections to process
     */
    @SuppressWarnings("SameParameterValue") // annotationClass is used as a parameter in view of code scalability
    void doLayout(
        Class<?> componentClass,
        Target target,
        Class<? extends Annotation> annotationClass) {
        doLayout(componentClass, target, Collections.singletonList(annotationClass));
    }

    /**
     * Processes container-backing Java class and appends the results to the root {@link Target} object
     * @param componentClass    {@code Class<?>} instance used as the source of markup
     * @param target            The root of rendering for the current component
     * @param annotationClasses Collection of {@code Class<?>} objects representing types of container sections to
     *                          process
     */
    void doLayout(
        Class<?> componentClass,
        Target target,
        List<Class<? extends Annotation>> annotationClasses) {

        // Initialize basic variables
        String containerTag;
        String resourceType;
        if (annotationClasses.contains(AccordionPanel.class)) {
            containerTag = DialogConstants.NN_ACCORDION;
            resourceType = ResourceTypes.ACCORDION;
        } else {
            containerTag = DialogConstants.NN_TABS;
            resourceType = ResourceTypes.TABS;
        }

        // Create the container object
        Target itemsContainer = target
            .createTarget(DialogConstants.NN_CONTENT)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER)
            .createTarget(DialogConstants.NN_ITEMS)
            .createTarget(containerTag)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, resourceType)
            .createTarget(DialogConstants.NN_ITEMS);

        // Compose the registry of tabs or accordion panels
        List<Section> allContainerSections = getSections(componentClass, target.getScope(), annotationClasses);

        // Initialize ignored sections array for the current class.
        // Note that "ignored sections" setting is not inherited and is for current class only, unlike the collection
        // of tabs or panels itself
        String[] ignoredSections = getIgnoredSectionTitles(componentClass);

        // Get all *non-nested* sources from the superclasses, and from the current class
        List<Source> allMembers = ClassUtil.getSources(componentClass);

        // If tabs or accordion panels collection is empty and yet there are sources to be placed, fire an exception
        if (allContainerSections.isEmpty() && !allMembers.isEmpty()) {
            InvalidContainerException ex = new InvalidContainerException();
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }

        // Place available sources in sections and dismiss the placed ones from the common collection
        PlacementHelper placementHelper = PlacementHelper.builder()
            .container(itemsContainer)
            .sections(allContainerSections)
            .ignoredSections(ignoredSections)
            .members(allMembers)
            .build();
        placementHelper.doPlacement();
        allMembers.removeAll(placementHelper.getProcessedMembers());

        // Afterwards there still can be "orphaned" sources in the "all sources" collection. They are either designed
        // to be rendered within a container widget, such as Accordion or Tabs, or are members for which a non-existent
        // container was specified.
        // We filter out members belonging to widget containers, and handle an InvalidContainerItemException for the rest
        if (allMembers.isEmpty()) {
            return;
        }
        List<String> widgetSectionsTitles = getWidgetSectionTitles(componentClass);
        for (Source unplaced : allMembers) {
            String placeValue = unplaced.adaptTo(PlaceSetting.class).getValue();
            String lastChunk = placeValue.contains(CoreConstants.SEPARATOR_SLASH)
                ? StringUtils.substringAfterLast(placeValue, CoreConstants.SEPARATOR_SLASH)
                : placeValue;
            boolean isValidPlaceValue = widgetSectionsTitles.contains(lastChunk);
            if (isValidPlaceValue) {
                continue;
            }
            InvalidContainerException ex = new InvalidContainerException(placeValue);
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }
    }


    /* ---------------
       Dialog sections
       --------------- */

    /**
     * Retrieves a collection of container sections derived from the specified hierarchical collection of classes
     * @param componentClass    {@code Class<?>} instance used as the source of markup
     * @param scope             String value defining whether to handle the current Java class as a {@code Dialog}
     *                          source, or a {@code DesignDialog} source
     * @param annotationClasses One or more {@code Class<?>} objects representing types of container sections to
     *                          process
     * @return Ordered list of container sections
     */
    private static List<Section> getSections(
        Class<?> componentClass,
        String scope,
        List<Class<? extends Annotation>> annotationClasses) {

        // Retrieve superclasses of the current class, from top of the hierarchy to the most immediate ancestor,
        // populate container section registry and store members that are within @Tab or @AccordionPanel-marked classes
        // (because we will not have access to them later)
        List<Section> containerSectionsFromSuperClasses = getSections(
            ClassUtil.getInheritanceTree(componentClass, false),
            scope,
            annotationClasses);

        // Retrieve tabs or accordion panels of the current class same way
        List<Section> containerSectionsFromCurrentClass = getSections(
            Collections.singletonList(componentClass),
            scope,
            annotationClasses);

        return mergeSectionsFromCurrentClassAndSuperclasses(
            containerSectionsFromCurrentClass,
            containerSectionsFromSuperClasses);
    }

    /**
     * Retrieves a collection of container sections derived from the specified hierarchical collection of classes
     * @param hierarchy         The {@code Class<?>}-es to search for defined container items
     * @param scope             String value defining whether to handle the current Java class as a {@code Dialog}
     *                          source, or a {@code DesignDialog} source
     * @param annotationClasses One or more {@code Class<?>} objects representing types of container sections to
     *                          process
     * @return Ordered list of container sections
     */
    private static List<Section> getSections(
        List<Class<?>> hierarchy,
        String scope,
        List<Class<? extends Annotation>> annotationClasses) {

        List<Section> result = new ArrayList<>();
        for (Class<?> classEntry : hierarchy) {
            appendSectionsFromNestedClasses(result, classEntry, annotationClasses);
            appendSectionsFromCurrentClass(result, classEntry, scope);
        }
        return result;
    }

    /**
     * Puts all sections, such as tabs or accordion panels, from the nested classes of the current Java class that
     * represents a Granite UI dialog into the accumulating collection
     * @param accumulator       The collection of container sections
     * @param componentClass    {@code Class<?>} instance used as the source of markup
     * @param annotationClasses One or more {@code Class<?>} objects representing types of container sections to
     *                          process
     */
    private static void appendSectionsFromNestedClasses(
        List<Section> accumulator,
        Class<?> componentClass,
        List<Class<? extends Annotation>> annotationClasses) {

        List<Class<?>> nestedClasses = Arrays.stream(componentClass.getDeclaredClasses())
            .filter(nestedCls -> annotationClasses.stream().anyMatch(nestedCls::isAnnotationPresent))
            .collect(Collectors.toList());
        Collections.reverse(nestedClasses);
        for (Class<?> nestedClass : nestedClasses) {
            Annotation matchedAnnotation = annotationClasses
                .stream()
                .filter(nestedClass::isAnnotationPresent)
                .findFirst()
                .map(nestedClass::getDeclaredAnnotation)
                .orElse(null);
            Section section = Section.from(matchedAnnotation);
            if (section == null) {
                continue;
            }
            section.getSources().addAll(ClassUtil.getSources(nestedClass));
            accumulator.add(section);
        }
    }

    /**
     * Puts all sections, such as tabs or accordion panels, from the current Java class that represents a Granite UI
     * dialog into the accumulating collection
     * @param accumulator    The collection of container sections
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param scope          String value defining whether to handle the current Java class as a {@code Dialog} source,
     *                       or a {@code DesignDialog} source
     */
    @SuppressWarnings("deprecation") // Processing of container.Tab class and Dialog#tabs() method is retained for
    // compatibility and will be removed in a version after 2.0.2
    private static void appendSectionsFromCurrentClass(List<Section> accumulator, Class<?> componentClass, String scope) {
        com.exadel.aem.toolkit.api.annotations.container.Tab[] legacyTabs = null;
        Tab[] tabs = null;
        AccordionPanel[] panels = null;

        if (Scopes.CQ_DIALOG.equals(scope) && componentClass.getDeclaredAnnotation(Dialog.class) != null) {
            Dialog dialogAnnotation = componentClass.getDeclaredAnnotation(Dialog.class);
            legacyTabs = dialogAnnotation.tabs();
        }
        if (componentClass.getDeclaredAnnotation(Tabs.class) != null) {
            tabs = componentClass.getDeclaredAnnotation(Tabs.class).value();
        } else if (componentClass.getDeclaredAnnotation(Accordion.class) != null) {
            panels = componentClass.getDeclaredAnnotation(Accordion.class).value();
        }
        Stream.of(
            ArrayUtils.nullToEmpty(legacyTabs),
            ArrayUtils.nullToEmpty(tabs),
            ArrayUtils.nullToEmpty(panels))
            .flatMap(Arrays::stream)
            .forEach(section -> accumulator.add(Section.from((Annotation) section)));
    }

    /**
     * Composes the "overall" collection of container sections (such as tabs or accordion panels) by merging sections
     * from the current class and from the hierarchy of superclasses in the proper order
     * @param sectionsFromCurrentClass Collection of sections defined in the current class
     * @param sectionsFromSuperClasses Collection of sections defined in the hierarchy of superclasses
     * @return Resulting ordered list of sections
     */
    private static List<Section> mergeSectionsFromCurrentClassAndSuperclasses(
        List<Section> sectionsFromCurrentClass,
        List<Section> sectionsFromSuperClasses) {
        // Whether the current class has any sections that match sections from superclasses,
        // we consider that the "right" order of container items is defined herewith, and place container items
        // from the current class first, then rest of the container items.
        // Otherwise, we consider the container items of the current class to be an "addendum" of container items
        // from superclasses, and put them in the end
        boolean sectionTitlesIntersect = sectionsFromCurrentClass
            .stream()
            .anyMatch(section -> sectionsFromSuperClasses.stream().anyMatch(otherSection -> otherSection.getTitle().equals(section.getTitle())));
        if (sectionTitlesIntersect) {
            return mergeSections(sectionsFromCurrentClass, sectionsFromSuperClasses);
        }
        return mergeSections(sectionsFromSuperClasses, sectionsFromCurrentClass);
    }

    /**
     * Called by {@link LayoutHandler#mergeSectionsFromCurrentClassAndSuperclasses(List, List)} to compose a synthetic
     * collection with priority defined
     * @param primary   List of {@code SectionHelper} instances the order of which will be preserved
     * @param secondary List of {@code SectionHelper} instances that will supplement the primary list
     * @return Resulting ordered list of sections
     */
    private static List<Section> mergeSections(List<Section> primary, List<Section> secondary) {
        List<Section> result = new ArrayList<>(primary);
        for (Section other : secondary) {
            Section matchingFromCurrentClass = result
                .stream()
                .filter(section -> section.getTitle().equals(other.getTitle()))
                .findFirst()
                .orElse(null);
            if (matchingFromCurrentClass != null) {
                matchingFromCurrentClass.merge(other);
            } else {
                result.add(other);
            }
        }
        return result;
    }

    /**
     * Retrieves the names of container sections set to be ignored
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @return Array of strings, non-null
     */
    @SuppressWarnings("deprecation") // Processing of IgnoreTabs annotation is retained for
    // compatibility and will be removed in a version after 2.0.2
    private static String[] getIgnoredSectionTitles(Class<?> componentClass) {
        String[] result = ArrayUtils.EMPTY_STRING_ARRAY;
        if (componentClass.isAnnotationPresent(IgnoreTabs.class)) {
            result = componentClass.getAnnotation(IgnoreTabs.class).value();
        }
        if (componentClass.isAnnotationPresent(Ignore.class)
            && componentClass.getAnnotation(Ignore.class).sections().length > 0) {
            result = ArrayUtils.addAll(result, componentClass.getAnnotation(Ignore.class).sections());
        }
        return result;
    }

    /* --------------------------------
       Container-like widgets' sections
       -------------------------------- */

    private static List<String> getWidgetSectionTitles(Class<?> componentClass) {
        List<Source> containerSources = ClassUtil.getSources(componentClass, source -> source.adaptTo(ContainerWidgetSetting.class).isPresent());
        return containerSources
            .stream()
            .flatMap(source -> source.adaptTo(ContainerWidgetSetting.class).getSections().stream())
            .map(Section::getTitle)
            .collect(Collectors.toList());
    }
}
