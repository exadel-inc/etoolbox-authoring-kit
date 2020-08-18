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
