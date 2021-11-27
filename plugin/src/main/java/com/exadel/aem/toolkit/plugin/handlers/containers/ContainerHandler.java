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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Presents a common base for handler classes responsible for laying out child components within Granite UI
 */
public abstract class ContainerHandler {

    private static final String RECURSION_MESSAGE_TEMPLATE = "Recursive rendering prohibited: a member of type \"%s\" " +
        "was set to be rendered within a container of type \"%s\"";

    /* ----------------------------------
       Retrieving members for a container
       ---------------------------------- */

    /**
     * Retrieves the list of sources that match the current container. This is performed by calling {@code
     * ClassUtil.getSources()} with the additional predicate that allows to filter out sources that are set to be
     * ignored at either the "member itself" level, or the "declaring class" level
     * @param container         Current {@link Source} instance
     * @param useReportingClass True to use {@link MemberSource#getReportingClass()} to look for the ignored members
     *                          (this is the case for the MultiField and FieldSet handlers, because ignored members are
     *                          commonly specified outside the fieldset content). False to use the same {@link
     *                          MemberSource#getValueType()} for wither placeable and ignored members
     * @return {@code List} containing {@code Source}-typed placeable members, or an empty list
     */
    @SuppressWarnings("deprecation") // Processing of IgnoreFields is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    protected List<Source> getMembersForContainer(Source container, boolean useReportingClass) {
        MemberSource memberSource = container.adaptTo(MemberSource.class);
        Class<?> declaringClass = memberSource.getDeclaringClass();
        Class<?> valueTypeClass = memberSource.getValueType();
        Class<?> reportingClass = useReportingClass ? memberSource.getReportingClass() : valueTypeClass;

        // Neither the valueTypeClass, nor the reportingClass can be equal to, or a descendant of the class that
        // is currently being processed, or we will face the "render MyFieldset inside MyFieldset" situation and get a
        // stack overflow
        if (ClassUtils.isAssignable(valueTypeClass, declaringClass)) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidLayoutException(
                String.format(RECURSION_MESSAGE_TEMPLATE, valueTypeClass.getName(), declaringClass.getName())));
            return Collections.emptyList();
        }

        // Build the collection of ignored members at nesting class level
        // (apart from those defined for the container class itself)
        Stream<ClassMemberSetting> classLevelIgnoredMembers = Stream.empty();
        if (reportingClass.isAnnotationPresent(Ignore.class)) {
            classLevelIgnoredMembers = Arrays.stream(reportingClass.getAnnotation(Ignore.class).members())
                .map(memberPtr -> new ClassMemberSetting(memberPtr).populateDefaults(reportingClass));
        } else if (reportingClass.isAnnotationPresent(IgnoreFields.class)) {
            classLevelIgnoredMembers = Arrays.stream(reportingClass.getAnnotation(IgnoreFields.class).value())
                .map(memberPtr -> new ClassMemberSetting(memberPtr).populateDefaults(reportingClass));
        }
        // Now build collection of ignored members at member level
        Stream<ClassMemberSetting> fieldLevelIgnoredMembers = Stream.empty();
        if (container.adaptTo(Ignore.class) != null) {
            fieldLevelIgnoredMembers = Arrays.stream(container.adaptTo(Ignore.class).members())
                .map(memberPtr -> new ClassMemberSetting(memberPtr).populateDefaults(valueTypeClass));
        } else if (container.adaptTo(IgnoreFields.class) != null) {
            fieldLevelIgnoredMembers = Arrays.stream(container.adaptTo(IgnoreFields.class).value())
                .map(memberPtr -> new ClassMemberSetting(memberPtr).populateDefaults(valueTypeClass));
        }

        // Join the collections and make sure that only members from any of the superclasses of the current source's class
        // are present
        List<ClassMemberSetting> allIgnoredFields = Stream
            .concat(classLevelIgnoredMembers, fieldLevelIgnoredMembers)
            .filter(memberSettings ->
                ClassUtil.getInheritanceTree(valueTypeClass)
                    .stream()
                    .anyMatch(superclass -> superclass.equals(memberSettings.getSource()))
            )
            .collect(Collectors.toList());

        // Create filters to sort out ignored fields (apart from those defined for the container class)
        // and to banish non-widget fields
        // Return the filtered field list
        Predicate<Source> nonIgnoredMembers = source -> allIgnoredFields.stream().noneMatch(ignored -> ignored.matches(source));
        return ClassUtil.getSources(valueTypeClass, nonIgnoredMembers);
    }

    /* ----------------------
       Populating a container
       ---------------------- */

    /**
     * Used to fill in plain (single-section) containers
     * @param members Collection of widget-holding class members that relate to the current container
     * @param target  {@code Target} to place widgets in
     */
    protected void populatePlainContainer(List<Source> members, Target target) {
        PlacementHelper.builder()
            .container(target)
            .members(members)
            .build()
            .doPlacement();
    }

    /**
     * Used to fill in multi-section containers nested within a Granite UI dialog. This method extracts container sections,
     * such as {@code Tab}s or {@code AccordionPanel}s, from the current {@code Source} and fills in the {@code Target}
     * with them
     * @param member          Class member holding a multi-section container
     * @param target          {@code Target} to place widgets in
     * @param annotationClass Class of nested container, such as {@code Tabs} or {@code Accordion}
     */
    protected void populateMultiSectionContainer(Source member, Target target, Class<? extends Annotation> annotationClass) {
        target.createTarget(DialogConstants.NN_ITEMS);

        List<Section> containerSections = getSections(member, annotationClass);
        List<Source> placeableMembers = getMembersForContainer(member, false);

        if (containerSections.isEmpty() && !placeableMembers.isEmpty()) {
            InvalidContainerException ex = new InvalidContainerException();
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }

        PlacementHelper placementHelper = PlacementHelper.builder()
            .container(target.getTarget(DialogConstants.NN_ITEMS))
            .sections(containerSections)
            .ignoredSections(ArrayUtils.EMPTY_STRING_ARRAY)
            .members(placeableMembers)
            .build();
        placementHelper.doPlacement();
        placeableMembers.removeAll(placementHelper.getProcessedMembers());

        if (!placeableMembers.isEmpty()) {
            placeableMembers
                .stream()
                .map(m -> new InvalidContainerException(m.adaptTo(PlaceSetting.class).getValue()))
                .forEach(ex -> PluginRuntime.context().getExceptionHandler().handle(ex));
        }
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Retrieves container sections declared by the current source
     * @param source Current {@link Source} instance
     * @param annotationClass Container annotation to look for, such as a {@link Tabs} or {@link Accordion}
     * @return Collection of {@code SectionFacade} objects
     */
    private static List<Section> getSections(Source source, Class<? extends Annotation> annotationClass) {
        List<Section> result = new ArrayList<>();
        if (source.adaptTo(annotationClass) == null) {
            return result;
        }
        if (annotationClass.equals(Tabs.class)) {
            Arrays.stream(source.adaptTo(Tabs.class).value())
                .forEach(tab -> result.add(new TabFacade(tab, false)));
        } else if (annotationClass.equals(Accordion.class)) {
            Arrays.stream(source.adaptTo(Accordion.class).value())
                .forEach(accordionPanel -> result.add(new AccordionPanelFacade(accordionPanel, false)));
        } else if (annotationClass.equals(FixedColumns.class)) {
            Arrays.stream(source.adaptTo(FixedColumns.class).value())
                .forEach(column -> result.add(new ColumnFacade(column)));
        }
        return result;
    }
}
