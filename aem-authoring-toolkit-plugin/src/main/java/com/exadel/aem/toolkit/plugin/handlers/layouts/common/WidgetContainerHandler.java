package com.exadel.aem.toolkit.plugin.handlers.layouts.common;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginContainerUtility;

public abstract class WidgetContainerHandler implements BiConsumer<Source, Target> {

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     * @param annotationClass class of container
     */
    protected void populateContainer(Source source, Target target, Class<? extends Annotation> annotationClass) {
        target.createTarget(DialogConstants.NN_ITEMS);

        List<SectionFacade> containerSections = getContainerSections(source, annotationClass);
        List<Source> placeableSources = PluginContainerUtility.getContainerEntries(source, false);

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
     * Retrieves container sections declared by the current source, such as a class member
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
