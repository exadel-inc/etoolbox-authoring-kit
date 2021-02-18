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

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.source.Sources;
import com.exadel.aem.toolkit.plugin.util.PluginContainerUtility;
import com.exadel.aem.toolkit.plugin.util.stream.Sorter;

/**
 * Helper object for distributing class member-bound widgets into container sections, such as {@code Tab}s
 * or {@code AccordionPanel}s, that are defined in the processed Java class or any of its superclasses
 */
public class PlacementHelper {
    private Target container;
    private List<SectionFacade> sectionHelpers;
    private String[] ignoredSections;
    private List<Source> members;
    private List<Source> processedMembers;

    private PlacementHelper() {
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
     * were placed are stored in a separate collection and ca be retrieved via {@link PlacementHelper#getProcessedMembers()}
     */
    void doPlacement() {
        processedMembers = new ArrayList<>();
        Iterator<SectionFacade> sectionIterator = sectionHelpers.iterator();
        int iterationStep = 0;
        while (sectionIterator.hasNext()) {
            final boolean isFirstSection = iterationStep++ == 0;
            SectionFacade currentSection = sectionIterator.next();
            List<Source> sectionMembers = new ArrayList<>();
            for (Member member : currentSection.getMembers()) {
                sectionMembers.add(Sources.fromMember(member, member.getDeclaringClass()));
            }
            List<Source> assignableSectionMembers = members.stream()
                .filter(member -> isMemberForSection(member, currentSection.getTitle(), isFirstSection))
                .collect(Collectors.toList());
            boolean needResort = !sectionMembers.isEmpty() && !assignableSectionMembers.isEmpty();
            sectionMembers.addAll(assignableSectionMembers);
            if (needResort) {
                sectionMembers.sort(Sorter::compareByRank);
            }
            processedMembers.addAll(assignableSectionMembers);
            if (!ArrayUtils.contains(ignoredSections, currentSection.getTitle())) {
                Target itemsContainer = currentSection.createItemsContainer(container);
                PluginContainerUtility.appendToContainer(itemsContainer, sectionMembers);
            }
        }
    }

    /**
     * Matches a class member against a particular section, such as a {@code Tab} or an {@code AccordionPanel}
     * @param member           Class member (a field, or a method) to analyze
     * @param sectionTitle     String representing the title of a container section
     * @param isDefaultSection True if the current container item accepts fields for which no container item was specified; otherwise, false
     * @return True or false
     */
    private static boolean isMemberForSection(Source member, String sectionTitle, boolean isDefaultSection) {
        if (StringUtils.isBlank(member.adaptTo(PlaceSetting.class).getValue())) {
            return isDefaultSection;
        }
        return sectionTitle.equalsIgnoreCase(member.adaptTo(PlaceSetting.class).getValue());
    }

    /**
     * Retrieves a builder object for creating a {@link PlacementHelper} instance and populating it with data
     * @return {@code PlacementHelper} builder
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Used for creating a {@link PlacementHelper} instance and populating it with data
     */
    static class Builder {
        private Target container;
        private List<SectionFacade> sectionHelpers;
        private String[] ignoredSections;
        private List<Source> members;

        private Builder() {
        }

        /**
         * Assigns the reference to a {@code Target} object
         * @param value {@code Target} instance representing a TouchUI dialog tab or accordion panel. This one can be
         *              modified during the execution, by adding new child containers
         * @return This instance
         */
        Builder container(Target value) {
            this.container = value;
            return this;
        }

        /**
         * Assigns the collection of sections members can be distributed into
         * @param value  Collection of {@link SectionFacade} object containing data for rendering container sections
         * @return This instance
         */
        Builder sections(List<SectionFacade> value) {
            this.sectionHelpers = value;
            return this;
        }

        /**
         * Assigns the collection of ignored sections' titles
         * @param value Array of ignored tabs or accordion panels (identified by their titles) for the current class
         * @return This instance
         */
        Builder ignoredSections(String[] value) {
            this.ignoredSections = value;
            return this;
        }

        /**
         * Assigns the collection placeable members
         * @param value All <i>non-nested</i> class members from superclasses and the current class
         * @return This instance
         */
        Builder members(List<Source> value) {
            this.members = value;
            return this;
        }

        /**
         * Creates the {@code PlacementHelper} object
         * @return {@link PlacementHelper} instance
         */
        PlacementHelper build() {
            PlacementHelper result = new PlacementHelper();
            result.container = container;
            result.sectionHelpers = sectionHelpers;
            result.ignoredSections = ignoredSections;
            result.members = members;
            return result;
        }
    }
}
