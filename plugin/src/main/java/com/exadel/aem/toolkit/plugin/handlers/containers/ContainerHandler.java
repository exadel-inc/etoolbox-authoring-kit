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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;
import com.exadel.aem.toolkit.plugin.adapters.WidgetContainerSetup;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Presents a common base for handler classes responsible for laying out child components within Granite UI
 */
public abstract class ContainerHandler {

    protected static final Function<MemberSource, List<Class<?>>> ANNOTATED_MEMBER_TYPE =
        memberSource -> Collections.singletonList(memberSource.getValueType());
    protected static final Function<MemberSource, List<Class<?>>> ANNOTATED_MEMBER_TYPE_AND_REPORTING_CLASS =
        memberSource -> Arrays.asList(memberSource.getValueType(), memberSource.getReportingClass());

    private static final String RECURSION_MESSAGE_TEMPLATE = "Recursive rendering prohibited: a member of type \"%s\" " +
        "was set to be rendered within a container created with member of type \"%s\"";

    /* ----------------------------------
       Retrieving members for a container
       ---------------------------------- */

    /**
     * Retrieves the list of sources that match the current container. This is performed by calling {@code
     * ClassUtil.getSources()} with the additional predicate that allows to filter out sources that are set to be
     * ignored at either the "member itself" level, or the "declaring class" level
     * @param container Current {@link Source} instance
     * @return {@code List} containing {@code Source}-typed placeable members, or an empty list
     */
    protected List<Source> getMembersForContainer(Source container) {
        return getMembersForContainer(container, null);
    }

    /**
     * Retrieves the list of sources that match the current container
     * @param container Current {@link Source} instance
     * @param filter    An additional filter applied to all the sources that can be retrieved from a host class. Can be
     *                  used, for instance, to specify a particular container title
     * @return {@code List} containing {@code Source}-typed placeable members, or an empty list
     * @see ContainerHandler#getMembersForContainer(Source)
     */
    private List<Source> getMembersForContainer(Source container, Predicate<Source> filter) {
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
        // 2) However, for e.g. an Accordion widget, there are two possible sources: same as (1), and the "surrounding"
        // class
        List<Class<?>> hostsOfRenderableMembers = getRenderedClassesProvider().apply(memberSource);
        for (Class<?> hostClass : hostsOfRenderableMembers) {
            boolean excludeCurrentSource = hostClass.equals(declaringClass);

            // Create the filter to sort out ignored fields (apart from those defined for the container class), exclude
            // the current source (if needed), banish non-widget fields, and do custom filtering (if needed)
            List<ClassMemberSetting> ignoredMembers = getIgnoredMembers(memberSource, hostClass);
            Predicate<Source> nonIgnoredMembers = source -> ignoredMembers.stream().noneMatch(ignored -> ignored.matches(source));
            if (filter != null) {
                nonIgnoredMembers = nonIgnoredMembers.and(filter);
            }
            if (excludeCurrentSource) {
                nonIgnoredMembers = nonIgnoredMembers.and(source -> !isSameClassMember(source, container));
            }

            // Then, retrieve members for the current class, filter applied, without ordering
            result.addAll(ClassUtil.getSources(hostClass, nonIgnoredMembers, false));
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
    protected abstract Function<MemberSource, List<Class<?>>> getRenderedClassesProvider();

    @SuppressWarnings("deprecation") // Processing of IgnoreFields is retained for compatibility and will be removed
    // in a version after 2.0.2
    private List<ClassMemberSetting> getIgnoredMembers(MemberSource container, Class<?> membersHost) {
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
     * Used to fill in plain (single-section) containers
     * @param members Collection of widget-holding class members that relate to the current container
     * @param target  {@code Target} to place widgets in
     */
    protected void populateSingleSectionContainer(List<Source> members, Target target) {
        PlacementHelper.builder()
            .container(target)
            .members(members)
            .build()
            .doPlacement();
    }

    /**
     * Used to fill in multi-section containers nested within a Granite UI dialog. This method extracts container
     * sections, such as {@code Tab}s or {@code AccordionPanel}s, from the current {@code Source} and fills the {@code
     * Target}
     * @param container Class member holding a multi-section container
     * @param target    {@code Target} to place widgets in
     */
    protected void populateMultiSectionContainer(Source container, Target target) {
        target.createTarget(DialogConstants.NN_ITEMS);

        List<Section> containerSections = container
            .adaptTo(WidgetContainerSetup.class)
            .useHierarchyFrom(target)
            .getSections();

        if (containerSections.isEmpty()) {
            InvalidContainerException ex = new InvalidContainerException();
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }

        // Unlike in LayoutHandler, we are only interested in members that have the section name matching one of the
        // existing sections by either simple or full ("hierarchical") title
        List<Source> placeableMembers = getMembersForContainer(
            container,
            member -> member
                .tryAdaptTo(Place.class)
                .map(Place::value)
                .map(placeValue -> containerSections.stream().anyMatch(section -> section.canContain(member)))
                .orElse(false));

        PlacementHelper placementHelper = PlacementHelper.builder()
            .container(target.getTarget(DialogConstants.NN_ITEMS))
            .sections(containerSections)
            .ignoredSections(Collections.emptyList())
            .members(placeableMembers)
            .build();
        placementHelper.doPlacement();
        placeableMembers.removeAll(placementHelper.getProcessedMembers());
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
