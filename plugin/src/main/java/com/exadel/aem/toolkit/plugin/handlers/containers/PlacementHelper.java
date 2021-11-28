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
package com.exadel.aem.toolkit.plugin.handlers.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Helper object for distributing class member-bound widgets into container sections, such as {@code Tab}s
 * or {@code AccordionPanel}s, that are defined in the processed Java class or any of its superclasses
 */
public class PlacementHelper {


    /* -------------------------------
       Private fields and constructors
       ------------------------------- */

    private Target container;
    private List<Section> sections;
    private List<String> ignoredSections;
    private List<Source> members;
    private final List<Source> processedMembers;

    /**
     * Default (instantiation-preventing) constructor
     */
    private PlacementHelper() {
        processedMembers = new ArrayList<>();
    }


    /**
     * Retrieves the list of sources that were successfully placed with this {@code PlacementHelper}
     * @return List of {@code Source} instances. Empty list is returned if placement was not successful. {@code Null} is
     * returned if placement never occurred ({@link PlacementHelper#doPlacement()} was not called)
     */
    public List<Source> getProcessedMembers() {
        return processedMembers;
    }

    /**
     * Attempts to place as many as possible available members to the sections of the container provided. The members that
     * were placed are stored in a separate collection and can be retrieved via {@link PlacementHelper#getProcessedMembers()}
     */
    public void doPlacement() {
        if (sections != null && !sections.isEmpty()) {
            doMultiSectionPlacement();
        } else {
            appendToContainer(container, members);
        }
    }

    /**
     * Called by {@link PlacementHelper#doPlacement()} to process multi-section installations
     */
    private void doMultiSectionPlacement() {
        Iterator<Section> sectionIterator = sections.iterator();
        int iterationStep = 0;
        while (sectionIterator.hasNext()) {
            final boolean isFirstSection = iterationStep++ == 0;
            Section currentSection = sectionIterator.next();
            List<Source> existingSectionMembers = new ArrayList<>(currentSection.getSources());
            List<Source> assignableSectionMembers = members.stream()
                .filter(member -> currentSection.canContain(member, isFirstSection))
                .collect(Collectors.toList());
            boolean needToSortAgain = !existingSectionMembers.isEmpty() && !assignableSectionMembers.isEmpty();
            existingSectionMembers.addAll(assignableSectionMembers);
            if (needToSortAgain) {
                existingSectionMembers = OrderingUtil.sortMembers(existingSectionMembers);
            }
            processedMembers.addAll(assignableSectionMembers);
            if (!ignoredSections.contains(currentSection.getTitle())) {
                Target itemsContainer = currentSection.createItemsContainer(container);
                appendToContainer(itemsContainer, existingSectionMembers);
            }
        }
    }

    /**
     * Appends provided {@link Source}s to the {@link Target} manifesting a container node
     * @param container {@link Target} manifesting a pre-defined widget container
     * @param sources   List of sources, such as members of a Java class
     */
    private static void appendToContainer(Target container, List<Source> sources) {
        PlacementCollisionSolver.checkForCollisions(sources);
        PlacementCollisionSolver.resolveFieldMethodNameCoincidences(sources);
        Target itemsElement = container.getOrCreateTarget(DialogConstants.NN_ITEMS);

        for (Source source : sources) {
            Target newElement = itemsElement.getOrCreateTarget(NamingUtil.stripGetterPrefix(source.getName()));
            HandlerChains.forMember().accept(source, newElement);
        }
    }


    /* ----------------
       Instance builder
       ----------------*/

    /**
     * Retrieves a builder object for creating a {@link PlacementHelper} instance and populating it with data
     * @return {@code PlacementHelper} builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Used for creating a {@link PlacementHelper} instance and populating it with data
     */
    public static class Builder {
        private Target container;
        private List<Section> sections;
        private List<String> ignoredSections;
        private List<Source> members;

        private Builder() {
        }

        /**
         * Assigns the reference to a {@code Target} object
         * @param value {@code Target} instance representing a Granite UI dialog, tab, or accordion panel. This one can
         *              be modified during the execution by adding new child containers
         * @return This instance
         */
        public Builder container(Target value) {
            this.container = value;
            return this;
        }

        /**
         * Assigns the collection of sections members can be distributed into
         * @param value  Collection of {@link Section} object containing data for rendering container sections
         * @return This instance
         */
        public Builder sections(List<Section> value) {
            this.sections = value;
            return this;
        }

        /**
         * Assigns the collection of ignored sections' titles
         * @param value Array of ignored tabs or accordion panels (identified by their titles) for the current class
         * @return This instance
         */
        public Builder ignoredSections(List<String> value) {
            this.ignoredSections = value;
            return this;
        }

        /**
         * Assigns the collection placeable members
         * @param value All <i>non-nested</i> class members from superclasses and the current class
         * @return This instance
         */
        public Builder members(List<Source> value) {
            this.members = value;
            return this;
        }

        /**
         * Creates the {@code PlacementHelper} object
         * @return {@link PlacementHelper} instance
         */
        public PlacementHelper build() {
            PlacementHelper result = new PlacementHelper();
            result.container = container;
            result.sections = sections;
            result.ignoredSections = ignoredSections;
            result.members = members;
            return result;
        }
    }
}
