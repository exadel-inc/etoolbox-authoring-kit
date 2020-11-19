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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.NamingUtil;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * Handler for storing properties coming from custom annotations and, optionally, processed by custom handlers
 * to a Granite UI widget XML node
 */
public class CustomHandler implements BiConsumer<Source, Target> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@code Source} instance
     * @param target Current {@code Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        PluginReflectionUtility.getFieldAnnotations(source).filter(a -> a.isAnnotationPresent(DialogWidgetAnnotation.class))
                .map(a -> a.getAnnotation(DialogWidgetAnnotation.class).source())
                .flatMap(widgetSource -> PluginRuntime.context().getReflectionUtility().getCustomDialogWidgetHandlers().stream()
                        .filter(handler -> widgetSource.equals(handler.getName())))
                .forEach(handler -> handler.accept(source, target));

        PluginRuntime.context().getReflectionUtility()
                .getCustomDialogWidgetHandlers(PluginReflectionUtility.getFieldAnnotations(source).collect(Collectors.toList()))
                .forEach(handler -> handler.accept(source, target));

        Arrays.stream(source.adaptTo(Property[].class))
                .forEach(p -> target.attribute(NamingUtil.getValidFieldName(p.name()), p.value()));

        PluginReflectionUtility.getFieldAnnotations(source).filter(a -> a.isAnnotationPresent(DialogWidgetAnnotation.class))
                .map(a -> a.getAnnotation(DialogWidgetAnnotation.class).source())
                .flatMap(widgetSource -> PluginRuntime.context().getReflectionUtility().getCustomDialogWidgetHandlersLegacy().stream()
                        .filter(handler -> widgetSource.equals(handler.getName())))
                .forEach(handler -> {target.setLegacyHandlers(handler); target.setLegacyField((Field) source.getSource());});

        PluginRuntime.context().getReflectionUtility()
                .getCustomDialogWidgetHandlersLegacy(PluginReflectionUtility.getFieldAnnotations(source).collect(Collectors.toList()))
                .forEach(handler -> {target.setLegacyHandlers(handler); target.setLegacyField((Field) source.getSource());});
    }
}
