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
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.DialogContainerSetup;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.adapters.WidgetContainerSetup;
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

    /**
     * Processes a container-backing Java class and appends the results to the root {@link Target} object
     * @param source          {@code Source} instance used as the source of markup
     * @param target          The root of rendering for the current component
     * @param annotationType {@code Class} instance representing types of container sections to process
     */
    @SuppressWarnings("SameParameterValue") // annotationClass is used as a parameter in view of code scalability
    void doLayout(
        Source source,
        Target target,
        Class<? extends Annotation> annotationType) {

        doLayout(source, target, Collections.singletonList(annotationType));
    }

    /**
     * Processes a container-backing Java class and appends the results to the root {@link Target} object
     * @param source          {@code Source} instance used as the source of markup
     * @param target          The root of rendering for the current component
     * @param annotationTypes Collection of {@code Class<?>} objects representing types of container sections to
     *                        process
     */
    void doLayout(
        Source source,
        Target target,
        List<Class<? extends Annotation>> annotationTypes) {

        // Initialize basic variables
        String containerTag;
        String resourceType;
        if (annotationTypes.contains(AccordionPanel.class)) {
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
        List<Section> allContainerSections = source
            .adaptTo(DialogContainerSetup.class)
            .useScope(target.getScope())
            .useInClassAnnotations(annotationTypes)
            .getSections();

        // Initialize ignored sections array for the current class.
        // Note that "ignored sections" setting is not inherited and is for current class only, unlike the collection
        // of tabs or panels itself
        List<String> ignoredSections = source.adaptTo(DialogContainerSetup.class).getIgnoredSections();

        // Get all *non-nested* sources from the superclasses, and from the current class
        List<Source> allMembers = ClassUtil.getSources(source.adaptTo(Class.class));

        // If tabs/accordion panels/columns collection is empty and yet there are sources to be placed, fire an exception
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
        List<String> widgetSectionsTitles = getWidgetSectionTitles(source.adaptTo(Class.class));
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

    /* --------------------------------
       Container-like widgets' sections
       -------------------------------- */

    private static List<String> getWidgetSectionTitles(Class<?> componentClass) {
        List<Source> containerSources = ClassUtil.getSources(componentClass, source -> source.adaptTo(WidgetContainerSetup.class).isPresent());
        return containerSources
            .stream()
            .flatMap(source -> source.adaptTo(WidgetContainerSetup.class).getSections().stream())
            .map(Section::getTitle)
            .collect(Collectors.toList());
    }
}
