package com.exadel.aem.toolkit.plugin.handlers.layouts.common;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.plugin.util.stream.Filter;

public abstract class WidgetContainerHandler implements BiConsumer<Source, Target> {

    /**
     * Retrieves the list of sources that match the current container.
     * This is performed by calling {@link PluginReflectionUtility#getAllSources(Class)}
     * with additional predicates that allow to filter out sources that are set to be ignored at either
     * the "member itself" level or at "declaring class" level. Afterwards the non-widget fields are also filtered out
     * @param container         Current {@link Source} instance
     * @param useReportingClass True to use {@link Source#getReportingClass()} to look for ignored members (this is
     *                          the case for {@code Multifield} or {@code FieldSet}-bound members);
     *                          False to use same {@link Source#getValueType()} as for the rest of method logic
     * @return {@code List<Source>} containing placeable members, or an empty collection
     */
    protected List<Source> getEntriesForContainer(Source container, boolean useReportingClass) {
        Class<?> valueTypeClass = container.getValueType();
        Class<?> reportingClass = useReportingClass ? container.getReportingClass() : valueTypeClass;
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
                PluginReflectionUtility. getClassHierarchy(valueTypeClass)
                    .stream()
                    .anyMatch(superclass -> superclass.equals(memberSettings.source()))
            )
            .collect(Collectors.toList());

        // Create filters to sort out ignored fields (apart from those defined for the container class)
        // and to banish non-widget fields
        // Return the filtered field list
        Predicate<Source> nonIgnoredMembers = Filter.getNotIgnoredSourcesPredicate(allIgnoredFields);
        Predicate<Source> dialogFields = DialogWidgets::isPresent;
        return PluginReflectionUtility.getAllSources(valueTypeClass, Arrays.asList(nonIgnoredMembers, dialogFields));
    }

    /**
     * Used to fill in plain (single-section) containers
     * @param members Collection of widget-holding class members that relate to the current container
     * @param target  {@code Target} to place widgets in
     */
    protected void populateContainer(List<Source> members, Target target) {
        PlacementHelper.builder()
            .container(target)
            .members(members)
            .build()
            .doPlacement();
    }

    /**
     * Used to fill in multi-section containers nested within a TouchUI dialog. This method extracts container sections,
     * such as {@code Tab}s or {@code AccordionPanel}s, from the current {@code Source} and fills in the {@code Target}
     * with them
     * @param member          Class member holding a multi-section container
     * @param target          {@code Target} to place widgets in
     * @param annotationClass Class of nested container, such as {@code Tabs} or {@code Accordion}
     */
    protected void populateNestedContainer(Source member, Target target, Class<? extends Annotation> annotationClass) {
        target.createTarget(DialogConstants.NN_ITEMS);

        List<SectionFacade> containerSections = getContainerSections(member, annotationClass);
        List<Source> placeableSources = getEntriesForContainer(member, false);

        if (containerSections.isEmpty() && !placeableSources.isEmpty()) {
            InvalidContainerException ex = new InvalidContainerException();
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }

        PlacementHelper placementHelper = PlacementHelper.builder()
            .container(target.getTarget(DialogConstants.NN_ITEMS))
            .sections(containerSections)
            .ignoredSections(ArrayUtils.EMPTY_STRING_ARRAY)
            .members(placeableSources)
            .build();
        placementHelper.doPlacement();
        placeableSources.removeAll(placementHelper.getProcessedMembers());

        if (!placeableSources.isEmpty()) {
            ContainerHandler.handleInvalidContainerException(placeableSources);
        }
    }

    /**
     * Retrieves container sections declared by the current source
     * @param source Current {@link Source} instance
     * @param annotationClass Container annotation to look for, such as a {@link Tabs} or {@link Accordion}
     */
    private static List<SectionFacade> getContainerSections(Source source, Class<? extends Annotation> annotationClass) {
        List<SectionFacade> result = new ArrayList<>();
        if (source.adaptTo(annotationClass) == null) {
            return result;
        }
        if (annotationClass.equals(Tabs.class)) {
            Arrays.stream(source.adaptTo(Tabs.class).value())
                .forEach(tab -> result.add(new TabFacade(tab, false)));
        } else if (annotationClass.equals(Accordion.class)) {
            Arrays.stream(source.adaptTo(Accordion.class).value())
                .forEach(accordionPanel -> {
                    result.add(new AccordionPanelFacade(accordionPanel, false));
                });
        }
        return result;
    }
}
