package com.exadel.aem.toolkit.plugin.handlers.container.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginContainerUtility;
import com.exadel.aem.toolkit.plugin.util.PluginObjectUtility;

public abstract class WidgetContainerHandler implements BiConsumer<Source, Target> {

    private static final Logger LOG = LoggerFactory.getLogger(WidgetContainerHandler.class);

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     * @param annotationClass class of container
     */
    protected void populateContainer(Source source, Target target, Class<? extends Annotation> annotationClass) {
        String containerName = annotationClass.equals(TabsWidget.class) ? DialogConstants.NN_TABS : DialogConstants.NN_ACCORDION;
        String containerSectionName = annotationClass.equals(TabsWidget.class) ? DialogConstants.NN_TAB : DialogConstants.NN_ACCORDION;
        String exceptionMessage = annotationClass.equals(TabsWidget.class) ? ContainerHandler.TABS_EXCEPTION : ContainerHandler.ACCORDION_EXCEPTION;

        target.create(DialogConstants.NN_ITEMS);

        Map<String, ContainerSection> containerSections = getContainerSections(source, annotationClass);

        List<Source> placeableSources = PluginContainerUtility.getContainerEntries(source, false);

        if (containerSections.isEmpty() && !placeableSources.isEmpty()) {
            InvalidSettingException ex = new InvalidSettingException(exceptionMessage + source.getValueType().getName());
            PluginRuntime.context().getExceptionHandler().handle(ex);
            containerSections.put(StringUtils.EMPTY, new ContainerSection("Untitled"));
        }

        ContainerHandler.addToContainer(
            target.get(DialogConstants.NN_ITEMS),
            placeableSources,
            containerSections,
            ArrayUtils.EMPTY_STRING_ARRAY,
            containerSectionName);

        ContainerHandler.handleInvalidContainerException(placeableSources, containerName);
    }

    /**
     * Retrieves container sections declared by the current source, such as a class member
     * @param source Current {@link Source} instance
     * @param annotationClass Container annotation to look for, such as a {@link TabsWidget} or {@link AccordionWidget}
     */
    private static Map<String, ContainerSection> getContainerSections(Source source, Class<? extends Annotation> annotationClass) {
        Map<String, ContainerSection> result = new LinkedHashMap<>();
        if (source.adaptTo(annotationClass) == null) {
            return result;
        }
        if (annotationClass.equals(TabsWidget.class)) {
            Arrays.stream(source.adaptTo(TabsWidget.class).tabs())
                .forEach(tab -> {
                    ContainerSection containerInfo = new ContainerSection(tab.title());
                    try {
                        containerInfo.setAttributes(PluginObjectUtility.getAnnotationFields(tab));
                        result.put(tab.title(), containerInfo);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        LOG.error(exception.getMessage());
                    }
                });
        } else if (annotationClass.equals(AccordionWidget.class)) {
            Arrays.stream(source.adaptTo(AccordionWidget.class).panels())
                .forEach(accordionPanel -> {
                    ContainerSection containerInfo = new ContainerSection(accordionPanel.title());
                    try {
                        containerInfo.setAttributes(PluginObjectUtility.getAnnotationFields(accordionPanel));
                        result.put(accordionPanel.title(), containerInfo);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        LOG.error(exception.getMessage());
                    }
                });
        }
        return result;
    }
}
