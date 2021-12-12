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
package com.exadel.aem.toolkit.plugin.handlers.placement.layouts;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Column;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.handlers.placement.PlacementHelper;
import com.exadel.aem.toolkit.plugin.handlers.placement.registries.MembersRegistry;
import com.exadel.aem.toolkit.plugin.handlers.placement.registries.SectionsRegistry;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.targets.RootTarget;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Presents a common base for handlers processing layout logic for Granite UI containers, such as the accordion
 * container, tab container, etc.
 */
interface ComplexLayoutHandler extends BiConsumer<Source, Target> {

    /**
     * Processes a container-backing Java class and appends the results to the root {@link Target} object
     * @param source         {@code Source} instance used as the data supplier for the markup
     * @param target         The root of rendering for the current component
     * @param annotationType {@code Class} instance representing types of container sections to process
     */
    @SuppressWarnings("SameParameterValue") // annotationClass is used as a parameter in view of code scalability
    default void doLayout(
        Source source,
        Target target,
        Class<? extends Annotation> annotationType) {

        doLayout(source, target, Collections.singletonList(annotationType));
    }

    /**
     * Processes a container-backing Java class and appends the results to the root {@link Target} object
     * @param source          {@code Source} instance used as the data supplier for the markup
     * @param target          The root of rendering for the current component
     * @param annotationTypes Collection of {@code Class<?>} objects representing types of container sections to
     *                        process
     */
    default void doLayout(
        Source source,
        Target target,
        List<Class<? extends Annotation>> annotationTypes) {

        // We consider this target to be a dialog node. We initialize sections and members registries that will be used
        // by this handler and the nested containers' handlers
        SectionsRegistry sectionsRegistry = SectionsRegistry.from(source, target, annotationTypes);
        MembersRegistry membersRegistry = new MembersRegistry(ClassUtil.getSources(source.adaptTo(Class.class)));
        target.adaptTo(RootTarget.class).setMembers(membersRegistry);

        // If tabs/accordion panels/columns collection is empty and yet there are sources to be placed, fire an exception
        if (sectionsRegistry.getAvailable().isEmpty() && !membersRegistry.getAvailable().isEmpty()) {
            InvalidContainerException ex = new InvalidContainerException();
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }

        // Now we can proceed with building markup. Initialize basic string variables
        String containerTag;
        String resourceType;
        if (annotationTypes.contains(Column.class)) {
            containerTag = DialogConstants.NN_FIXED_COLUMNS;
            resourceType = ResourceTypes.FIXED_COLUMNS;
        } else if (annotationTypes.contains(AccordionPanel.class)) {
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

        // Place available sources in sections and dismiss the placed ones from the common registry
        PlacementHelper.builder()
            .container(itemsContainer)
            .sections(sectionsRegistry)
            .members(membersRegistry)
            .build()
            .doPlacement();

        // Afterwards there still can be "orphaned" sources in the "all sources" collection. They are either designed
        // to be rendered within a container widget, such as Accordion or Tabs, or are members for which a non-existent
        // container was specified
        for (Source unplaced : membersRegistry.getAvailable()) {
            String placeValue = unplaced.adaptTo(PlaceSetting.class).getValue();
            InvalidContainerException ex = new InvalidContainerException(placeValue);
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }
    }
}
