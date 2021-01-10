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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

import org.w3c.dom.Element;

/**
 * Handler for storing properties coming from custom annotations and, optionally, processed by custom handlers
 * to a Granite UI widget node
 */
public class CustomHandler implements BiConsumer<Source, Target> {
    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {

        PluginRuntime.context().getReflectionUtility()
            .getCustomDialogWidgetHandlers(PluginReflectionUtility.getFieldAnnotations(source).collect(Collectors.toList()))
            .forEach(handler -> handler.accept(source, target));

        acceptLegacyHandlers(source, target);
    }

    private void acceptLegacyHandlers(Source source, Target target) {
        if (PluginReflectionUtility.getFieldAnnotations(source).anyMatch(a -> a.isAnnotationPresent(DialogWidgetAnnotation.class))) {
            target.setSource(source);
        }

        try {
            for (DialogWidgetHandler handler : PluginRuntime.context().getReflectionUtility()
                .getCustomDialogWidgetHandlers(PluginReflectionUtility.getFieldAnnotations(source).collect(Collectors.toList()))) {
                if (!handler.getClass().getMethod("accept", Element.class, Field.class).isDefault()) {
                    target.setSource(source);
                }
            }
        } catch (NoSuchMethodException ignored) {
            //ignored
        }

    }
}
