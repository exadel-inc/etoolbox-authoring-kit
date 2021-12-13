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
package com.exadel.aem.toolkit.plugin.handlers.placement.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.placement.PlacementHelper;
import com.exadel.aem.toolkit.plugin.handlers.placement.registries.MembersRegistry;
import com.exadel.aem.toolkit.plugin.handlers.placement.registries.SectionsRegistry;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.targets.RootTarget;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Presents a common base for handler classes responsible for laying out child components within Granite UI
 */
abstract class ContainerHandler {

    static final Function<MemberSource, List<Class<?>>> ANNOTATED_MEMBER_TYPE =
        memberSource -> Collections.singletonList(memberSource.getValueType());
    static final Function<MemberSource, List<Class<?>>> ANNOTATED_MEMBER_TYPE_AND_REPORTING_CLASS =
        memberSource -> Arrays.asList(memberSource.getValueType(), memberSource.getReportingClass());

    private static final String RECURSION_MESSAGE_TEMPLATE = "Recursive rendering prohibited: a member of type \"%s\" " +
        "was set to be rendered within a container created with member of type \"%s\"";

    /* ----------------------------------
       Retrieving members for a container
       ---------------------------------- */

    /**
     * Retrieves the list of sources that can be inserted in the current container. Where to retrieve sources from,
     * depends on the nature of a current container handler.
     * <p>E.g., for a {@code @FieldSet} or {@code @MultiField}-annotated entry, members of the underlying class can be
     * retrieved. But for an entry that does not necessarily refer to a container class, such as in-dialog {@code @Tabs},
     * we need to take into account the members of the "surrounding" class (the "host" class) in which the current entry
     * is declared.</p>
     * <p>Note that for a nested class the exact set of members is retrieved; while dealing with a host class, the routine
     * returns a greater number members that are expected to be filtered and distributed between sections of the current
     * container with the use of {@link PlacementHelper}
     * {@link PlacementHelper}</p>
     * @param container Class member holding a multi-section container
     * @param target    Current {@link Target instance}
     * @return {@code List} containing {@code Source}-typed placeable members, or an empty list
     */
    List<Source> getAvailableForContainer(Source container, Target target) {
        List<Source> result = new ArrayList<>();

        // Extract data from the source object
        MemberSource memberSource = container.adaptTo(MemberSource.class);
        Class<?> declaringClass = memberSource.getDeclaringClass();
        Class<?> valueTypeClass = memberSource.getValueType();

        // Neither the valueTypeClass, nor the reportingClass can be equal to, or a descendant of the class that
        // is currently being processed. Otherwise, we will face the "render MyFieldset inside MyFieldset" situation
        // and get a stack overflow
        if (ClassUtils.isAssignable(valueTypeClass, declaringClass)) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidLayoutException(
                String.format(RECURSION_MESSAGE_TEMPLATE, valueTypeClass.getName(), declaringClass.getName())));
            return Collections.emptyList();
        }

        // There can be one or more sources of members for this container.
        // 1) For e.g. a FieldSet, the only source is the class represented by the return type of the @FieldSet-annotated
        // field or method, or else the class specified in @FieldSet(value=...). The same applies to a MultiField
        // 2) However for e.g. an Accordion widget, there are two possible sources: same as (1), and the "surrounding"
        // class
        List<Class<?>> hostsOfRenderedMembers = getRenderedClassesProvider().apply(memberSource);
        for (Class<?> hostClass : hostsOfRenderedMembers) {
            boolean isDeclaringClass = hostClass.equals(declaringClass);

            // Create the filter to sort out ignored fields (apart from those defined for the container class), exclude
            // the current source (if needed), banish non-widget fields, and do custom filtering (if needed)
            List<ClassMemberSetting> ignoredMembers = getIgnoredMembers(memberSource, hostClass);
            Predicate<Source> nonIgnoredMembersFilter = source -> ignoredMembers.stream().noneMatch(ignored -> ignored.matches(source));
            if (isDeclaringClass) {
                nonIgnoredMembersFilter = nonIgnoredMembersFilter.and(source -> !isSameClassMember(source, container));
            }

            // Then, retrieve members for the current class, filter applied without ordering.
            // If this class is not an underlying class of e.g. a FieldSet or MultiField-annotated member, but the
            // "surrounding" class within which the current member is declared -- then we must use the set of members
            // that is already attached to the target, and not collect the available members once more
            List<Source> renderedMembers = isDeclaringClass
                ? target.getRoot().adaptTo(RootTarget.class).getMembers().getAllAvailable().stream().filter(nonIgnoredMembersFilter).collect(Collectors.toList())
                : ClassUtil.getSources(hostClass, nonIgnoredMembersFilter, false);
            result.addAll(renderedMembers);
        }

        // Finally, order the whole of the members collection
        return OrderingUtil.sortMembers(result);
    }

    /**
     * When overridden in a derived class, returns a {@code Function} used to extract one or more {@code Class} objects
     * from the given {@code Source}. This is required to compose a collection of class members eligible for the current
     * container.
     * <p>Normally we expect a single class - the return type of the annotated member, or else two classes - the
     * class represented by the return type, and the class in which the annotated member is declared</p>
     * @return {@code Function} reference that accepts one argument of type {@code MemberSource} and returns a non-null
     * list of {@code Class} objects
     */
    abstract Function<MemberSource, List<Class<?>>> getRenderedClassesProvider();

    /**
     * Called from {@link ContainerHandler#getAvailableForContainer(Source, Target)} to find out which members are
     * being ignored due to the {@code @Ignore} directives that are put either at class level or field/method level
     * @param container   {@code Source} instance representing a field or a method marked with a "container"-type
     *                    annotation
     * @param membersHost {@code Class} reference that refers to one of the classes this member is associated with
     *                    (either the value type of the corresponding field/method or the declaring class)
     * @return List of {@link ClassMemberSetting} objects, or an empty list
     */
    @SuppressWarnings("deprecation") // Processing of IgnoreFields is retained for compatibility and will be removed
    // in a version after 2.0.2
    private static List<ClassMemberSetting> getIgnoredMembers(MemberSource container, Class<?> membersHost) {
        // Note: This functionality is shared across single-section placement routines ({@code FieldSet}, {@code MultiField})
        // and multi-section ones. First do not use MembersRegistry, while second do. That's why it is not encapsulated
        // in MembersRegistry (unlike the "ignored sections" functionality)

        // Build the collection of ignored members at nesting class level
        // (apart from those defined for the container class itself)
        Class<?> ignoredMembersHost = container.getReportingClass();

        Stream<ClassMemberSetting> classLevelIgnoredMembers = Stream.empty();
        if (ignoredMembersHost.isAnnotationPresent(Ignore.class)) {
            classLevelIgnoredMembers = Arrays
                .stream(ignoredMembersHost.getAnnotation(Ignore.class).members())
                .map(memberPtr -> new ClassMemberSetting(memberPtr).populateDefaults(ignoredMembersHost));
        } else if (ignoredMembersHost.isAnnotationPresent(IgnoreFields.class)) {
            classLevelIgnoredMembers = Arrays
                .stream(ignoredMembersHost.getAnnotation(IgnoreFields.class).value())
                .map(memberPtr -> new ClassMemberSetting(memberPtr).populateDefaults(ignoredMembersHost));

        }

        // Now build collection of ignored members at member level
        Stream<ClassMemberSetting> memberLevelIgnoredMembers = Stream.empty();
        if (container.adaptTo(Ignore.class) != null) {
            memberLevelIgnoredMembers = Arrays
                .stream(container.adaptTo(Ignore.class).members())
                .map(member -> new ClassMemberSetting(member).populateDefaults(membersHost));
        } else if (container.adaptTo(IgnoreFields.class) != null) {
            memberLevelIgnoredMembers = Arrays
                .stream(container.adaptTo(IgnoreFields.class).value())
                .map(member -> new ClassMemberSetting(member).populateDefaults(membersHost));
        }

        // Join the collections and make sure that only members from any of the superclasses of the current source's class
        // are present
        return Stream
            .concat(classLevelIgnoredMembers, memberLevelIgnoredMembers)
            .filter(memberSettings ->
                ClassUtil.getInheritanceTree(membersHost)
                    .stream()
                    .anyMatch(superclass -> superclass.equals(memberSettings.getSource()))
            )
            .collect(Collectors.toList());
    }

    /* ----------------------
       Populating a container
       ---------------------- */

    /**
     * Used to fill plain (single-section) containers nested within a Granite UI dialog
     * @param members Collection of widget-holding class members that relate to the current container
     * @param target  {@code Target} to place widgets in
     */
    void populateSingleSectionContainer(Source source, List<Source> members, Target target) {
        MembersRegistry membersRegistry = new MembersRegistry(
            target.getRoot().adaptTo(RootTarget.class).getMembers(),
            members);
        PlacementHelper.builder()
            .source(source)
            .container(target)
            .members(membersRegistry)
            .build()
            .doPlacement();
    }

    /**
     * Used to fill multi-section containers nested within a Granite UI dialog. This method extracts container sections,
     * such as {@code Tab}s or {@code AccordionPanel}s, from the current {@code Source} and fills the {@code Target}
     * @param member Class member holding a multi-section container
     * @param target {@code Target} to place widgets in
     */
    void populateMultiSectionContainer(Source member, Target target) {
        target.createTarget(DialogConstants.NN_ITEMS);

        SectionsRegistry sectionsRegistry = SectionsRegistry.from(member, target);
        if (sectionsRegistry.getAvailable().isEmpty()) {
            InvalidContainerException ex = new InvalidContainerException();
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }

        List<Source> placeableMembers = getAvailableForContainer(member, target);
        MembersRegistry membersRegistry = new MembersRegistry(
            target.getRoot().adaptTo(RootTarget.class).getMembers(),
            placeableMembers);

        PlacementHelper.builder()
            .source(member)
            .container(target.getTarget(DialogConstants.NN_ITEMS))
            .sections(sectionsRegistry)
            .members(membersRegistry)
            .build()
            .doPlacement();
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Gets whether the two {@code Source} objects represent the same class member
     * @param first  {@code Source} instance, non-null
     * @param second {@code Source} instance, non-null
     * @return True or false
     */
    private static boolean isSameClassMember(Source first, Source second) {
        return (first instanceof MemberSource)
            && (second instanceof MemberSource)
            && ((MemberSource) first).getDeclaringClass().equals(((MemberSource) second).getDeclaringClass())
            && StringUtils.equals(first.getName(), second.getName());
    }
}
