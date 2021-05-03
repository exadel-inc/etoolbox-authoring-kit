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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.CollectionUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DomAdapter;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.targets.TargetImpl;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance via calls to custom handlers attached to the
 * annotations (either built-in or custom) that are extractable from the provided {@link Source}
 */
public class CasualAnnotationsHandler implements BiConsumer<Source, Target> {
    private List<Handler> predefinedHandlers;

    /**
     * Default constructor
     */
    public CasualAnnotationsHandler() {
    }

    /**
     * Alternative constructor that accepts the list of handlers to process to be used instead of handlers extracted from
     * the passed {@code Source}
     * @param handlers Pre-defined handlers list
     */
    public CasualAnnotationsHandler(List<Handler> handlers) {
        this.predefinedHandlers = handlers;
    }

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    @SuppressWarnings("deprecation") // DialogWidgetHandler processing is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    public void accept(Source source, Target target) {
        List<Handler> handlers = getEffectiveHandlers(source, target.getScope());

        // Extract legacy handlers that accept(Element, Field)
        List<Handler> legacyHandlers = handlers.stream().filter(CasualAnnotationsHandler::isLegacyHandler).collect(Collectors.toList());
        // Separate modern handlers that accept(Source, Target)
        Collection<Handler> modernHandlers = CollectionUtils.subtract(handlers, legacyHandlers);

        // Process modern handlers
        OrderingUtil.sortHandlers(new ArrayList<>(modernHandlers)).forEach(handler -> handler.accept(source, target));

        // Process legacy handlers after modern ones (because they must trigger after built-in modern handlers worked)
        if (!legacyHandlers.isEmpty()) {
            Field field = source.adaptTo(Field.class);
            Element element = target
                .adaptTo(DomAdapter.class)
                .composeElement(PluginRuntime.context().getXmlUtility().getDocument());
            if (element != null) {
                legacyHandlers.forEach(handler -> ((DialogWidgetHandler) handler).accept(element, field));
                ((TargetImpl) target).attributes(element);
            }
        }
    }

    /**
     * Retrieves handler instances valid for the given source and scope
     * @param source {@code Source} object to get data from
     * @param scope Non-null string representing the scope that the handlers must match
     * @return List of {@code Handler} instances
     */
    @SuppressWarnings("deprecation") // DialogWidgetHandler processing is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    private List<Handler> getEffectiveHandlers(Source source, String scope) {
        if (predefinedHandlers != null && !predefinedHandlers.isEmpty()) {
            return predefinedHandlers;
        }

        List<Handler> result;
        // Modern handlers mapping approach -- via @Handles annotation
        result = PluginRuntime.context().getReflection().getHandlers(scope, source.adaptTo(Annotation[].class));
        // Legacy handlers mapping approach -- via source<->name mapping
        List<DialogWidgetHandler> sourceToNameMappingHandlers = Arrays.stream(source.adaptTo(Annotation[].class))
            .map(Annotation::annotationType)
            .filter(a -> a.isAnnotationPresent(DialogWidgetAnnotation.class))
            .map(a -> a.getAnnotation(DialogWidgetAnnotation.class).source())
            .flatMap(CasualAnnotationsHandler::getMatchedHandlersByName)
            .collect(Collectors.toList());
        result.addAll(sourceToNameMappingHandlers);

        return result;
    }


    /**
     * Called by {@link CasualAnnotationsHandler#getEffectiveHandlers(Source, String)} to process {@code source<->name}
     * mapping for the legacy-style handlers
     * @param source String representing the {@code source} value per the legacy-style handler signature
     * @return Stream of {@link DialogWidgetHandler} instances
     */
    @SuppressWarnings("deprecation") // DialogWidgetHandler processing is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    private static Stream<DialogWidgetHandler> getMatchedHandlersByName(String source) {
        if (StringUtils.isEmpty(source)) {
            return Stream.empty();
        }
        return PluginRuntime.context()
            .getReflection()
            .getHandlers()
            .stream()
            .filter(handler -> handler instanceof DialogWidgetHandler)
            .filter(handler -> StringUtils.equals(source, ((DialogWidgetHandler) handler).getName()))
            .map(DialogWidgetHandler.class::cast);
    }

    /**
     * Gets whether the given handler is a legacy handler, that is, implements the {@code accept} method that takes the
     * {@link Element}-typed and {@link Field}-typed argument
     * @param handler The {@code Handler} to analyze
     * @return True or false
     */
    private static boolean isLegacyHandler(Handler handler) {
        try {
            Method legacyAcceptMethod = handler.getClass().getMethod("accept", Element.class, Field.class);
            return !legacyAcceptMethod.isDefault(); // If it's not default, it has been overridden which is true for actual handlers
        } catch (NoSuchMethodException | SecurityException e) {
            // This is a valid situation, no particular processing needed
            return false;
        }
    }
}
