package com.exadel.aem.toolkit.core.handlers.widget.common;

import com.exadel.aem.toolkit.api.annotations.meta.DialogAnnotation;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * {@link Handler} implementation used to create markup responsible for AEM Authoring Toolkit {@code CustomAnnotation} functionality
 */
public class CustomDialogAnnotationHandler implements Handler, BiConsumer<Element, Class<?>> {

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param dialogClass {@code Class} object representing the tab-defining class
     */
    @Override
    public void accept(Element element, Class<?> dialogClass) {
        if (!classHasCustomDialogAnnotation(dialogClass)) {
            return;
        }
        List<DialogAnnotation> customAnnotations = getCustomDialogAnnotations(dialogClass);
        PluginRuntime.context().getReflectionUtility().getCustomDialogHandlers().stream()
                .filter(handler -> customAnnotations.stream()
                        .anyMatch(annotation -> customAnnotationMatchesHandler(annotation, handler)))
                .forEach(handler -> handler.accept(element, dialogClass));
    }

    /**
     * Retrieves list of {@link DialogAnnotation} instances defined for the current {@code Class}
     * @param componentClass The {@code Class} being processed
     * @return List of values, empty or non-empty
     */
    private List<DialogAnnotation> getCustomDialogAnnotations(Class<?> componentClass) {
        return Arrays.stream(componentClass.getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class) != null)
                .map(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class))
                .collect(Collectors.toList());
    }

    /**
     * Gets whether current {@code Class} has a custom dialog annotation attached
     * @param componentClass The {@code Class} being processed
     * @return True or false
     */
    private boolean classHasCustomDialogAnnotation(Class<?> componentClass) {
        return Arrays.stream(componentClass.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType().getDeclaredAnnotation(DialogAnnotation.class) != null);
    }

    /**
     * Used while enumerating available {@code CustomDialogHandler}s to set matching between a handler and a {@code CustomDialogAnnotation},
     * since one handler may serve for several annotations, and, optionally, vice versa
     * @param annotation {@link DialogAnnotation} instance
     * @param handler {@link DialogHandler} instance
     * @return True if the two arguments are "matching" via their properties, otherwise, false
     */
    private boolean customAnnotationMatchesHandler(DialogAnnotation annotation, DialogHandler handler) {
        return StringUtils.equals(annotation.source(), handler.getName());
    }
}
