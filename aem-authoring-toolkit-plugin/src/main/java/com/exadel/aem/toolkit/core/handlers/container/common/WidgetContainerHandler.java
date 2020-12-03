package com.exadel.aem.toolkit.core.handlers.container.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectPredicates;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

import static com.exadel.aem.toolkit.core.util.DialogConstants.PN_COMPONENT_CLASS;

public abstract class WidgetContainerHandler implements Handler, BiConsumer<Element, Field> {
    private static final Logger LOG = LoggerFactory.getLogger(WidgetContainerHandler.class);

    /**
     * Retrieves the list of fields applicable to the current container, by calling {@link PluginReflectionUtility#getAllFields(Class)}
     * with additional predicates that allow to sort out the fields set to be ignored at field level and at nesting class
     * level, and then sort out the non-widget fields
     * @param element       Current XML element
     * @param field         Current {@code Field} instance
     * @param containerType {@code Class} representing the type of the container
     * @return {@code List<Field>} containing renderable fields, or an empty collection
     */
    public static List<Field> getContainerFields(Element element, Field field, Class<?> containerType) {
        // Extract type of the Java class being the current rendering source
        Class<?> componentType = (Class<?>) element.getOwnerDocument().getUserData(PN_COMPONENT_CLASS);
        // Build the collection of ignored fields that may be defined at field level and at nesting class level
        // (apart from those defined for the container class itself)
        Stream<ClassField> classLevelIgnoredFields = componentType != null && componentType.isAnnotationPresent(IgnoreFields.class)
            ? Arrays.stream(componentType.getAnnotation(IgnoreFields.class).value())
            .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                ClassField.class,
                DialogConstants.PN_SOURCE_CLASS,
                componentType))
            : Stream.empty();
        Stream<ClassField> fieldLevelIgnoredFields = field.isAnnotationPresent(IgnoreFields.class)
            ? Arrays.stream(field.getAnnotation(IgnoreFields.class).value())
            .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                ClassField.class,
                DialogConstants.PN_SOURCE_CLASS,
                containerType))
            : Stream.empty();
        List<ClassField> allIgnoredFields = Stream.concat(classLevelIgnoredFields, fieldLevelIgnoredFields)
            .filter(classField -> PluginReflectionUtility.getClassHierarchy(containerType).stream()
                .anyMatch(superclass -> superclass.equals(classField.source())))
            .collect(Collectors.toList());

        // Create filters to sort out ignored fields (apart from those defined for the container class)
        // and to banish non-widget fields
        // Return the filtered field list
        Predicate<Field> nonIgnoredFields = PluginObjectPredicates.getNotIgnoredFieldsPredicate(allIgnoredFields);
        Predicate<Field> dialogFields = DialogWidgets::isPresent;
        return PluginReflectionUtility.getAllFields(containerType, Arrays.asList(nonIgnoredFields, dialogFields));
    }

    /**
     * Get all tabs from current class
     * @param annotation {@code Class <? extends Annotation>} searching annotation
     * @param field      Current {@code Field} instance
     */
    private Map<String, ContainerInfo> getInstancesFromCurrentClass(Class<? extends Annotation> annotation, Field field) {
        Map<String, ContainerInfo> tabInstancesFromCurrentClass = new LinkedHashMap<>();
        if (!field.isAnnotationPresent(annotation)) {
            return tabInstancesFromCurrentClass;
        }
        if (annotation.equals(TabsWidget.class)) {
            Arrays.stream(field.getAnnotation(TabsWidget.class).tabs())
                .forEach(tab -> {
                    ContainerInfo containerInfo = new ContainerInfo(tab.title());
                    try {
                        containerInfo.setAttributes(PluginObjectUtility.getAnnotationFields(tab));
                        tabInstancesFromCurrentClass.put(tab.title(), containerInfo);
                    } catch (IllegalAccessException | NoSuchFieldException exception) {
                        LOG.error(exception.getMessage());
                    }
                });
        } else if (annotation.equals(AccordionWidget.class)) {
            Arrays.stream(field.getAnnotation(AccordionWidget.class).panels())
                .forEach(tab -> {
                    ContainerInfo containerInfo = new ContainerInfo(tab.title());
                    try {
                        containerInfo.setAttributes(PluginObjectUtility.getAnnotationFields(tab));
                        tabInstancesFromCurrentClass.put(tab.title(), containerInfo);
                    } catch (IllegalAccessException | NoSuchFieldException exception) {
                        LOG.error(exception.getMessage());
                    }
                });
        }
        return tabInstancesFromCurrentClass;
    }
    public static Class<?> getManagedClass(Field field) {
        // Extract underlying field's type as is
        Class<?> result = field.getType().isArray() ? field.getType().getComponentType() : field.getType();
        // Try to retrieve collection's parameter type
        if (ClassUtils.isAssignable(result, Collection.class)) {
            result = PluginReflectionUtility.getPlainType(field, true);
        }
        // Switch to directly specified type, if any
        if (field.getAnnotation(MultiField.class) != null
            && field.getAnnotation(MultiField.class).field() != _Default.class) {
            result = field.getAnnotation(MultiField.class).field();
        } else if (field.getAnnotation(FieldSet.class) != null
            && field.getAnnotation(FieldSet.class).source() != _Default.class) {
            result = field.getAnnotation(FieldSet.class).source();
        }
        return result;
    }
    public void acceptParent(Element element, Class<? extends Annotation> containerClass, Field field) {
        String defaultTabName = containerClass.equals(TabsWidget.class) ? ContainerHandler.TAB : ContainerHandler.ACCORDION;
        String exceptionMessage = containerClass.equals(TabsWidget.class) ? ContainerHandler.TABS_EXCEPTION : ContainerHandler.ACCORDION_EXCEPTON;
        Element tabItemsElement = (Element) element
            .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        Map<String, ContainerInfo> tabInstancesFromCurrentClass = getInstancesFromCurrentClass(containerClass, field);

        Class<?> tabsType = field.getType();
        List<Field> allFields = getContainerFields(element, field, tabsType);

        if (tabInstancesFromCurrentClass.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                exceptionMessage + tabsType.getName()
            ));
            if (containerClass.equals(TabsWidget.class)) {
                tabInstancesFromCurrentClass.put(StringUtils.EMPTY, new ContainerInfo("newTab"));
            }
        }

        ContainerHandler.addTabs(tabInstancesFromCurrentClass, allFields, ArrayUtils.EMPTY_STRING_ARRAY, tabItemsElement, defaultTabName);
        ContainerHandler.handleInvalidTabException(allFields);
    }
}
