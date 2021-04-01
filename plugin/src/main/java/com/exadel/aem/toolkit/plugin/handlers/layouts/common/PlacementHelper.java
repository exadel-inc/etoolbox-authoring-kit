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
package com.exadel.aem.toolkit.plugin.handlers.layouts.common;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.adapters.ResourceTypeSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Helper object for distributing class member-bound widgets into container sections, such as {@code Tab}s
 * or {@code AccordionPanel}s, that are defined in the processed Java class or any of its superclasses
 */
public class PlacementHelper {

    private static final String NAMING_COLLISION_MESSAGE_TEMPLATE = "Field named \"%s\" in class \"%s\" " +
        "collides with the field having same name in class \"%s\" (%s). This may cause unexpected behavior";

    private static final String REASON_DISORDER = "parent class field will be shown";
    private static final String REASON_DIFFERENT_RESTYPE = "different resource types provided";


    /* -------------------------------
       Private fields and constructors
       ------------------------------- */

    private Target container;
    private List<SectionFacade> sectionHelpers;
    private String[] ignoredSections;
    private List<Source> members;
    private final List<Source> processedMembers;

    /**
     * Default (instantiation-preventing) constructor
     */
    private PlacementHelper() {
        processedMembers = new ArrayList<>();
    }


    /* ----------------
       Instance methods
       ---------------- */


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
        if (sectionHelpers != null && !sectionHelpers.isEmpty()) {
            doMultiSectionPlacement();
        } else {
            appendToContainer(container, members);
        }
    }

    /**
     * Called by {@link PlacementHelper#doPlacement()} to process multi-section installations
     */
    private void doMultiSectionPlacement() {
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
                sectionMembers.sort(OrderingUtil::compareByRank);
            }
            processedMembers.addAll(assignableSectionMembers);
            if (!ArrayUtils.contains(ignoredSections, currentSection.getTitle())) {
                Target itemsContainer = currentSection.createItemsContainer(container);
                appendToContainer(itemsContainer, sectionMembers);
            }
        }
    }


    /* -----------------------
       Private utility methods
       ----------------------- */


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
     * Appends provided {@link Source}s to the {@link Target} manifesting a container node
     * @param container {@link Target} manifesting a pre-defined widget container
     * @param sources   List of sources, such as members of a Java class
     */
    private static void appendToContainer(Target container, List<Source> sources) {
        Map<Source, String> managedFields = new LinkedHashMap<>();
        Target itemsElement = container.getOrCreateTarget(DialogConstants.NN_ITEMS);

        for (Source source : sources) {
            Target newElement = itemsElement.getOrCreateTarget(NamingUtil.stripGetterPrefix(source.getName()));
            HandlerChains.forMember().accept(source, newElement);
            managedFields.put(source, newElement.getName());
        }

        if (!container.getChildren().isEmpty()) {
            checkForCollisions(managedFields);
        }
    }

    /**
     * Tests the provided collection of members for possible collisions (members that share the same tag name),
     * and throws an exception if: <br>
     *     - a member from a superclass is positioned after the same-named member from a subclass, therefore, will "shadow"
     *     it and produce unexpected UI display; <br>
 *         - a member from a subclass has a resource type other than of a same-named member from a superclass, therefore,
     *     is at risk of producing a "mixed" markup
     * @param processedSources {@code Map} instance that matches the sources (Java class members) available for rendering
     *                         to corresponding tag names
     */
    private static void checkForCollisions(Map<Source, String> processedSources) {
        Collection<String> processedTagNames = processedSources.values();
        Set<String> distinctTagNames = new HashSet<>(processedTagNames);
        if (distinctTagNames.size() ==  processedTagNames.size()) {
            return;
        }
        for (String tagName : distinctTagNames) {
            checkForCollisions(tagName, processedSources);
        }
    }

    /**
     * Tests the provided collection of members sharing the particular tag name for collisions
     * @param tagName        String representing the tag name in question
     * @param managedMembers {@code Map} instance that matches the sources (class members) available for rendering
     *                       to corresponding tag names
     */
    private static void checkForCollisions(String tagName, Map<Source, String> managedMembers) {
        LinkedList<Source> sameNameFields = managedMembers.entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(tagName))
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(LinkedList::new));
        LinkedList<Source> sameNameFieldsByOrigin = sameNameFields
            .stream()
            .sorted(OrderingUtil::compareByOrigin)
            .collect(Collectors.toCollection(LinkedList::new));

        if (!sameNameFields.getLast().equals(sameNameFieldsByOrigin.getLast())) {
            PluginRuntime
                .context()
                .getExceptionHandler()
                .handle(new InvalidLayoutException(String.format(
                    NAMING_COLLISION_MESSAGE_TEMPLATE,
                    sameNameFieldsByOrigin.getLast().getName(),
                    sameNameFieldsByOrigin.getLast().adaptTo(MemberSource.class).getDeclaringClass().getSimpleName(),
                    sameNameFields.getLast().adaptTo(MemberSource.class).getDeclaringClass().getSimpleName(),
                    REASON_DISORDER)));
        }

        Map<String, Source> resourceTypeCollisions = sameNameFields
            .stream()
            .collect(Collectors.toMap(
                source -> source.adaptTo(ResourceTypeSetting.class).getValue(),
                source -> source,
                (first, second) -> second,
                LinkedHashMap::new));

        if (resourceTypeCollisions.size() > 1) {
            Source[] contenders = resourceTypeCollisions.values().toArray(new Source[0]);
            PluginRuntime
                .context()
                .getExceptionHandler()
                .handle(new InvalidLayoutException(String.format(
                    NAMING_COLLISION_MESSAGE_TEMPLATE,
                    contenders[1].getName(),
                    contenders[1].adaptTo(MemberSource.class).getDeclaringClass().getSimpleName(),
                    contenders[0].adaptTo(MemberSource.class).getDeclaringClass().getSimpleName(),
                    REASON_DIFFERENT_RESTYPE)));
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
        private List<SectionFacade> sectionHelpers;
        private String[] ignoredSections;
        private List<Source> members;

        private Builder() {
        }

        /**
         * Assigns the reference to a {@code Target} object
         * @param value {@code Target} instance representing a Granite UI dialog tab or accordion panel. This one can be
         *              modified during the execution, by adding new child containers
         * @return This instance
         */
        public Builder container(Target value) {
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
            result.sectionHelpers = sectionHelpers;
            result.ignoredSections = ignoredSections;
            result.members = members;
            return result;
        }
    }
}
