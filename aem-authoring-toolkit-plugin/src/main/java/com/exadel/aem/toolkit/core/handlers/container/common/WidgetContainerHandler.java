package com.exadel.aem.toolkit.core.handlers.container.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectPredicates;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

public abstract class WidgetContainerHandler implements BiConsumer<Source, Target> {
    private static final Logger LOG = LoggerFactory.getLogger(WidgetContainerHandler.class);

    /**
     * Retrieves the list of fields applicable to the current container, by calling {@link PluginReflectionUtility#getAllSourceFacades(Class)}
     * with additional predicates that allow to sort out the fields set to be ignored at field level and at nesting class
     * level, and then sort out the non-widget fields
     * @param source       Current {@link Source} instance
     * @param target         Current {@link Target} instance
     * @param containerType {@code Class} representing the type of the container
     * @return {@code List<Field>} containing renderable fields, or an empty collection
     */
    public static List<Source> getContainerFields(Source source, Target target, Class<?> containerType) {
        // Extract type of the Java class being the current rendering source
        Class<?> componentType = source.getContainerClass();
        // Build the collection of ignored fields that may be defined at field level and at nesting class level
        // (apart from those defined for the container class itself)
        Stream<ClassMember> classLevelIgnoredFields = componentType != null && componentType.isAnnotationPresent(IgnoreFields.class)
            ? Arrays.stream(componentType.getAnnotation(IgnoreFields.class).value())
            .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                ClassMember.class,
                DialogConstants.PN_SOURCE_CLASS,
                componentType))
            : Stream.empty();
        Stream<ClassMember> fieldLevelIgnoredFields = source.adaptTo(IgnoreFields.class) != null
            ? Arrays.stream(source.adaptTo(IgnoreFields.class).value())
            .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                ClassMember.class,
                DialogConstants.PN_SOURCE_CLASS,
                containerType))
            : Stream.empty();
        List<ClassMember> allIgnoredFields = Stream.concat(classLevelIgnoredFields, fieldLevelIgnoredFields)
            .filter(classField -> PluginReflectionUtility.getClassHierarchy(containerType).stream()
                .anyMatch(superclass -> superclass.equals(classField.source())))
            .collect(Collectors.toList());

        // Create filters to sort out ignored fields (apart from those defined for the container class)
        // and to banish non-widget fields
        // Return the filtered field list
        Predicate<Member> nonIgnoredFields = PluginObjectPredicates.getNotIgnoredMembersPredicate(allIgnoredFields);
        Predicate<Member> dialogFields = DialogWidgets::isPresent;
        return PluginReflectionUtility.getAllSourceFacades(containerType, Arrays.asList(nonIgnoredFields, dialogFields));
    }

    /**
     * Get all container item instances from current class
     * @param annotationClass {@code Class <? extends Annotation>} searching annotationClass
     * @param source           Current {@link Source} instance
     */
    private Map<String, ContainerInfo> getInstancesFromCurrentClass(Class<? extends Annotation> annotationClass, Source source) {
        Map<String, ContainerInfo> containerItemInstancesFromCurrentClass = new LinkedHashMap<>();
        if (source.adaptTo(annotationClass) == null) {
            return containerItemInstancesFromCurrentClass;
        }
        if (annotationClass.equals(TabsWidget.class)) {
            Arrays.stream(source.adaptTo(TabsWidget.class).tabs())
                .forEach(tab -> {
                    ContainerInfo containerInfo = new ContainerInfo(tab.title());
                    try {
                        containerInfo.setAttributes(PluginObjectUtility.getAnnotationFields(tab));
                        containerItemInstancesFromCurrentClass.put(tab.title(), containerInfo);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        LOG.error(exception.getMessage());
                    }
                });
        } else if (annotationClass.equals(AccordionWidget.class)) {
            Arrays.stream(source.adaptTo(AccordionWidget.class).panels())
                .forEach(accordionPanel -> {
                    ContainerInfo containerInfo = new ContainerInfo(accordionPanel.title());
                    try {
                        containerInfo.setAttributes(PluginObjectUtility.getAnnotationFields(accordionPanel));
                        containerItemInstancesFromCurrentClass.put(accordionPanel.title(), containerInfo);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        LOG.error(exception.getMessage());
                    }
                });
        }
        return containerItemInstancesFromCurrentClass;
    }

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source         Current {@link Source} instance
     * @param annotationClass class of container
     * @param target           Current {@link Target} instance
     */
    protected void acceptParent(Source source, Class<? extends Annotation> annotationClass, Target target) {
        String defaultContainerItemName = annotationClass.equals(TabsWidget.class) ? DialogConstants.NN_TAB : DialogConstants.NN_ACCORDION;
        String containerName = annotationClass.equals(TabsWidget.class) ? DialogConstants.NN_TABS : DialogConstants.NN_ACCORDION;
        String exceptionMessage = annotationClass.equals(TabsWidget.class) ? ContainerHandler.TABS_EXCEPTION : ContainerHandler.ACCORDION_EXCEPTION;
        target.create(DialogConstants.NN_ITEMS);

        Map<String, ContainerInfo> containerItemInstancesFromCurrentClass = getInstancesFromCurrentClass(annotationClass, source);

        Class<?> containerType = source.getContainerClass();
        List<Source> allFields = getContainerFields(source, target, containerType);

        if (containerItemInstancesFromCurrentClass.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                exceptionMessage + containerType.getName()
            ));
            if (annotationClass.equals(TabsWidget.class)) {
                containerItemInstancesFromCurrentClass.put(StringUtils.EMPTY, new ContainerInfo("Untitled"));
            }
        }

        ContainerHandler.addContainerElements(containerItemInstancesFromCurrentClass, allFields, ArrayUtils.EMPTY_STRING_ARRAY, target.get(DialogConstants.NN_ITEMS), defaultContainerItemName);
        ContainerHandler.handleInvalidContainerItemException(allFields, containerName);
    }
}
