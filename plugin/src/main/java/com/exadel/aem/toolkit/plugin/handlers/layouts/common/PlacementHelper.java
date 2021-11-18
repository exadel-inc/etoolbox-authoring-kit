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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.adapters.ResourceTypeSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.sources.ModifiableMemberSource;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Helper object for distributing class member-bound widgets into container sections, such as {@code Tab}s
 * or {@code AccordionPanel}s, that are defined in the processed Java class or any of its superclasses
 */
public class PlacementHelper {

    private static final String NAMING_COLLISION_MESSAGE_TEMPLATE = "%s named \"%s\" in class \"%s\" " +
        "collides with the %s named \"%s\" in class \"%s\" (%s). This may cause unexpected behavior";

    private static final String REASON_AMBIGUOUS_ORDER = "attributes of the parent class member will have precedence";
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
            List<Source> existingSectionMembers = new ArrayList<>(currentSection.getSources());
            List<Source> assignableSectionMembers = members.stream()
                .filter(member -> isMemberForSection(member, currentSection.getTitle(), isFirstSection))
                .collect(Collectors.toList());
            boolean needResort = !existingSectionMembers.isEmpty() && !assignableSectionMembers.isEmpty();
            existingSectionMembers.addAll(assignableSectionMembers);
            if (needResort) {
                existingSectionMembers.sort(OrderingUtil::compareByRank);
            }
            processedMembers.addAll(assignableSectionMembers);
            if (!ArrayUtils.contains(ignoredSections, currentSection.getTitle())) {
                Target itemsContainer = currentSection.createItemsContainer(container);
                appendToContainer(itemsContainer, existingSectionMembers);
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
        checkForCollisions(sources);
        resolveFieldMethodNameCoincidences(sources);
        Target itemsElement = container.getOrCreateTarget(DialogConstants.NN_ITEMS);

        for (Source source : sources) {
            Target newElement = itemsElement.getOrCreateTarget(NamingUtil.stripGetterPrefix(source.getName()));
            HandlerChains.forMember().accept(source, newElement);
        }
    }

    /**
     * Checks for cases when sources intended to be placed in the same contender differ in {@code type} (e.g., one is a
     * Java method, another is field) but share the same name. If same-named sources have different resource types,
     * the tag names of field-bound sources are left the same, but the names of method-bound sources are changed to avoid
     * merging essentially different data in the same node
     * @param sources   List of sources, such as members of a Java class
     */
    private static void resolveFieldMethodNameCoincidences(List<Source> sources) {
        List<Source> fields = sources
            .stream()
            .filter(source -> DialogConstants.TYPE_FIELD.equals(source.getType()))
            .collect(Collectors.toList());

        for (Source currentField : fields) {
            List<Source> methodsToRename = getMembersWithSameName(
                sources,
                currentField.getName(),
                member -> DialogConstants.TYPE_METHOD.equals(member.getType())
                    && isSameOrSuperClass(currentField, member)
                    && hasDifferentResourceType(currentField, member)
            );
            if (methodsToRename.isEmpty()) {
                continue;
            }
            Map<String, List<Source>> methodGroupsByResourceType = new HashMap<>();
            methodsToRename.forEach(method -> methodGroupsByResourceType.computeIfAbsent(
                    method.adaptTo(ResourceTypeSetting.class).getValue(),
                    key -> new ArrayList<>())
                    .add(method));

            for (Map.Entry<String, List<Source>> methodGroupByResourceType : methodGroupsByResourceType.entrySet()) {
                List<Source> methodGroup = methodGroupByResourceType.getValue();
                String simpleResourceType = StringUtils.substringAfterLast(methodGroupByResourceType.getKey(), CoreConstants.SEPARATOR_SLASH);
                String newName = currentField.getName() + CoreConstants.SEPARATOR_UNDERSCORE + simpleResourceType.toLowerCase();
                methodGroup.forEach(method -> method.adaptTo(ModifiableMemberSource.class).setName(newName));
            }
        }
    }

    /**
     * Retrieves the list of {@code Source} objects matching the provided name. Names of fields and methods are coerced,
     * e,g, both {@code private String text;} and {@code public String getText() {...}} are considered sharing the same
     * name
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing the common name of sources to select
     * @return An ordered list of {@code Source} objects
     */
    private static LinkedList<Source> getMembersWithSameName(List<Source> sources, String name) {
        return getMembersWithSameName(sources, name, null);
    }

    /**
     * Retrieves the list of {@code Source} objects matching the provided name. Names of fields and methods are coerced,
     * e,g, both {@code private String text;} and {@code public String getText() {...}} are considered sharing the same
     * name
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing the common name of sources to select
     * @param filter  Nullable {@code Predicate} used as the additional filter of matching sources
     * @return An ordered list of {@code Source} objects
     */
    private static LinkedList<Source> getMembersWithSameName(List<Source> sources, String name, Predicate<Source> filter) {
        return sources
            .stream()
            .filter(source -> StringUtils.equals(NamingUtil.stripGetterPrefix(source.getName()), name))
            .filter(source -> filter == null || filter.test(source))
            .map(source -> source.adaptTo(MemberSource.class))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Called from {@link PlacementHelper#resolveFieldMethodNameCoincidences(List)} to detect whether the two provided
     * {@code Source}s represent classes that are the same or else are in the "child - parent" relation
     * @param first  {@code Source} instance representing a class member
     * @param second {@code Source} instance representing a class member
     * @return True or false
     */
    private static boolean isSameOrSuperClass(Source first, Source second) {
        Class<?> firstClass = first.adaptTo(MemberSource.class).getDeclaringClass();
        Class<?> secondClass = second.adaptTo(MemberSource.class).getDeclaringClass();
        return ClassUtils.isAssignable(firstClass, secondClass);
    }

    /**
     * Called from {@link PlacementHelper#resolveFieldMethodNameCoincidences(List)} to detect whether the two provided
     * {@code Source}s represent Granite UI components with the different resource types
     * @param first  {@code Source} instance representing a class member
     * @param second {@code Source} instance representing a class member
     * @return True or false
     */
    private static boolean hasDifferentResourceType(Source first, Source second) {
        return !first.adaptTo(ResourceTypeSetting.class).getValue().equals(second.adaptTo(ResourceTypeSetting.class).getValue());
    }


    /* ------------------------------
       Checking /reporting collisions
       ------------------------------ */

    /**
     * Tests the provided collection of members for possible collisions (Java class members that produce the same tag name),
     * and throws an exception if: <br>
     *     - a member from a superclass is positioned after the same-named member from a subclass, therefore, will "shadow"
     *     it and produce unexpected UI display; <br>
 *         - a member from a class has a resource type other than of a same-named member from a superclass or interface,
     *     therefore, is at risk of producing a "mixed" markup
     * @param sources {@code List} of sources available for rendering
     */
    private static void checkForCollisions(List<Source> sources) {
        List<String> distinctNames = sources
            .stream()
            .map(Source::getName)
            .map(NamingUtil::stripGetterPrefix)
            .distinct()
            .collect(Collectors.toList());
        if (distinctNames.size() ==  sources.size()) {
            // All names are different: there are no collisions
            return;
        }
        for (String name : distinctNames) {
            checkForNameCollisions(sources, name);
            checkForResourceTypeCollisions(sources, name);
        }
    }

    /**
     * Tests the provided collection of member sources sharing the particular name for naming collisions. If a collision
     * is found, the {@link InvalidLayoutException} is thrown
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing a common name of sources being tested
     */
    private static void checkForNameCollisions(List<Source> sources, String name) {
        LinkedList<Source> sameNameMembers = getMembersWithSameName(sources, name);
        LinkedList<Source> sameNameMembersByOrigin = sameNameMembers
            .stream()
            .sorted(OrderingUtil::compareByOrigin)
            .collect(Collectors.toCollection(LinkedList::new));

        if (!sameNameMembers.getLast().equals(sameNameMembersByOrigin.getLast())) {
            reportCollision(
                sameNameMembersByOrigin.getLast().adaptTo(MemberSource.class),
                sameNameMembers.getLast().adaptTo(MemberSource.class),
                REASON_AMBIGUOUS_ORDER);
        }

    }

    /**
     * Tests the provided collection of member sources sharing the particular name for collisions in exposed resource
     * types. If a collision is found, the {@link InvalidLayoutException} is thrown
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing the common name of the sources being tested
     */
    private static void checkForResourceTypeCollisions(List<Source> sources, String name) {
        List<Source> sameNameMembers = getMembersWithSameName(sources, name);

        // We consider fields separately from methods
        // because we allow that a field, and a method of the same name have different resource types
        for (String currentMemberType : Arrays.asList(DialogConstants.TYPE_METHOD, DialogConstants.TYPE_FIELD)) {

            Map<String, Source> membersByResourceType = sameNameMembers
                .stream()
                .filter(member -> StringUtils.equals(member.getType(), currentMemberType))
                .collect(Collectors.toMap(
                    source -> source.adaptTo(ResourceTypeSetting.class).getValue(),
                    source -> source,
                    (first, second) -> second,
                    LinkedHashMap::new));
            if (membersByResourceType.size() > 1) {
                Source[] competitorArray = membersByResourceType.values().toArray(new Source[0]);
                reportCollision(
                    competitorArray[1].adaptTo(MemberSource.class),
                    competitorArray[0].adaptTo(MemberSource.class),
                    REASON_DIFFERENT_RESTYPE);
            }
        }
    }

    /**
     * Throws a formatted exception whenever a collision is found
     * @param first  {@code MemberSource} instance representing the first member of a collision
     * @param second {@code MemberSource} instance representing the second member of a collision
     * @param reason String explaining the essence of the collision
     */
    private static void reportCollision(MemberSource first, MemberSource second, String reason) {
        PluginRuntime
            .context()
            .getExceptionHandler()
            .handle(new InvalidLayoutException(String.format(
                NAMING_COLLISION_MESSAGE_TEMPLATE,
                first.getType(),
                first.getName(),
                first.getDeclaringClass().getSimpleName(),
                second.getType().toLowerCase(),
                second.getName(),
                second.getDeclaringClass().getSimpleName(),
                reason)));
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
