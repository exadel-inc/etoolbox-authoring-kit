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
package com.exadel.aem.toolkit.plugin.handlers.placement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.handlers.placement.registries.MembersRegistry;
import com.exadel.aem.toolkit.plugin.handlers.placement.registries.SectionsRegistry;
import com.exadel.aem.toolkit.plugin.handlers.placement.sections.Section;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Helper object for distributing class member-bound widgets into container sections, such as {@code Tab}s
 * or {@code AccordionPanel}s, that are defined in the processed Java class or any of its superclasses
 */
public class PlacementHelper {

    private Source source;
    private Target container;

    private SectionsRegistry sections;
    private MembersRegistry members;

    /**
     * Default (instantiation-preventing) constructor
     */
    private PlacementHelper() {
    }

    /* ----------------------
       Public placement logic
       ---------------------- */

    /**
     * Attempts to place as many as possible available members to the sections of the container provided
     */
    public void doPlacement() {
        if (sections != null && !sections.getAvailable().isEmpty()) {
            doMultiSectionPlacement();
        } else {
            doSimplePlacement();
        }
    }

    /**
     * Called by {@link PlacementHelper#doPlacement()} to process multi-section installations
     */
    private void doMultiSectionPlacement() {
        Iterator<Section> sectionIterator = sections.getAvailable().iterator();
        int iterationStep = 0;
        while (sectionIterator.hasNext()) {
            final boolean isFirstSection = iterationStep++ == 0;
            Section currentSection = sectionIterator.next();
            List<Source> currentSectionMembers = new ArrayList<>(currentSection.getSources());
            List<Source> assignableSectionMembers = members.getAllAvailable().stream()
                .filter(member -> currentSection.canContain(member, isFirstSection))
                .collect(Collectors.toList());
            boolean needToSortAgain = !currentSectionMembers.isEmpty() && !assignableSectionMembers.isEmpty();
            currentSectionMembers.addAll(assignableSectionMembers);
            if (needToSortAgain) {
                currentSectionMembers = OrderingUtil.sortMembers(currentSectionMembers);
            }
            if (!currentSection.isIgnored()) {
                doSectionPlacement(currentSectionMembers, currentSection);
            } else {
                currentSectionMembers.forEach(item -> members.checkOut(item));
            }
        }
    }

    /**
     * Appends the provided subset of member sources to the container produced by the given {@link Section}
     * @param candidates List of sources, such as members of a Java class
     * @param section    {@code Section} object referring to a markup section, such as a tab, an accordion panel,
     *                   or a column
     */
    private void doSectionPlacement(List<Source> candidates, Section section) {
        PlacementCollisionSolver.checkForCollisions(candidates);
        PlacementCollisionSolver.resolveFieldMethodNameCoincidences(candidates);

        Target itemsElement = section.createItemsContainer(container).getOrCreateTarget(DialogConstants.NN_ITEMS);

        for (Source candidate : candidates) {
            members.checkOut(candidate);
            if (((AdaptationBase<?>) candidate).hasAdaptation(PlaceSetting.class)
                // The source could be "soft-checked out" before. Then it may have an attached target. We check if it has one,
                // and if so, we just move the target to the new place. Otherwise, we create a new target
                && candidate.adaptTo(PlaceSetting.class).getMatchingTarget() != null) {
                Target existingElement = candidate.adaptTo(PlaceSetting.class).getMatchingTarget();
                // Check for a "mutual-nested" defect, e.g. when member_A needs to be placed
                // in a section of member_B, and member_B wants to be placed in a section of member_A
                // We do the check twice to also cover a more difficult case (member_A to member_B, member_B to member_C,
                // member_C to member_A). As we swap members, we make sure it is not true that every member can be contained
                // within another
                PlacementCollisionSolver.checkForCircularPlacement(source, container, candidate, existingElement);
                itemsElement.addTarget(existingElement);
            } else {
                Target newElement = itemsElement.getOrCreateTarget(NamingUtil.stripGetterPrefix(candidate.getName()));
                PlacementCollisionSolver.checkForCircularPlacement(source, container, candidate, newElement);
                HandlerChains.forMember().accept(candidate, newElement);
            }
        }
    }

    /**
     * Appends all the available member sources to the container node specified for this helper
     * instance. This is a rendition of placement suitable for a single-section setup
     */
    private void doSimplePlacement() {
        PlacementCollisionSolver.checkForCollisions(members.getAvailable());
        PlacementCollisionSolver.resolveFieldMethodNameCoincidences(members.getAvailable());
        Target itemsElement = container.getOrCreateTarget(DialogConstants.NN_ITEMS);

        while (!members.getAvailable().isEmpty()) {
            Source candidate = members.getAvailable().get(0);
            Target newElement = itemsElement.getOrCreateTarget(NamingUtil.stripGetterPrefix(candidate.getName()));

            // For historic reasons, we allow members annotated with @Place("...") in a single-column buildup without
            // throwing a "section not found" exception. But we should not be done with any of these members before we make
            // sure they won't fit in any of the nested containers. Therefore, when doing exactly single-column placement,
            // we engage not a full-scale checkout of a @Place-annotated member, but rather a "soft checkout".
            // Such member is placed "temporarily". It remains available for secondary PlacementHelpers, e.g. for a helper
            // invoked from an in-dialog Tabs or Accordion container
            if (!candidate.tryAdaptTo(Place.class).isPresent()) {
                members.checkOut(candidate);
            } else {
                PlaceSetting placeSetting = candidate.adaptTo(PlaceSetting.class);
                if (!placeSetting.getValue().isEmpty()) {
                    members.softCheckOut(candidate);
                    placeSetting.setMatchingTarget(newElement);
                } else {
                    members.checkOut(candidate);
                }
            }
            // Run handling strictly after checkout so that the involved handlers are informed on the updates
            // of the sources' states
            HandlerChains.forMember().accept(candidate, newElement);
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
        private Source source;
        private Target container;

        private SectionsRegistry sections;
        private MembersRegistry members;

        private Builder() {
        }

        /**
         * Assigns the reference to the placement source
         * @param value {@code Source} instance representing the source of building up a Granite UI container
         * @return This instance
         */
        public Builder source(Source value) {
            this.source = value;
            return this;
        }

        /**
         * Assigns the reference to a container object
         * @param value {@code Target} instance representing a Granite UI container. This one can
         *              be modified during the execution by adding new child containers
         * @return This instance
         */
        public Builder container(Target value) {
            this.container = value;
            return this;
        }

        /**
         * Assigns the {@link SectionsRegistry} object containing of sections that the dialog members can be distributed
         * into
         * @param value Collection of {@link Section} objects that preserve data for rendering container sections
         * @return This instance
         */
        public Builder sections(SectionsRegistry value) {
            this.sections = value;
            return this;
        }

        /**
         * Assigns the {@link MembersRegistry} object containing placeable members
         * @param value Registry of {@link Source} objects that represent placeable class members
         * @return This instance
         */
        public Builder members(MembersRegistry value) {
            this.members = value;
            return this;
        }

        /**
         * Creates the {@code PlacementHelper} object
         * @return {@link PlacementHelper} instance
         */
        public PlacementHelper build() {
            PlacementHelper result = new PlacementHelper();
            result.source = source;
            result.container = container;
            result.sections = sections;
            result.members = members;
            return result;
        }
    }
}
