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

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.api.handlers.TargetFacade;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * Handler for storing properties coming from custom annotations and, optionally, processed by custom handlers
 * to a Granite UI widget XML node
 */
public class CustomHandler implements Handler, BiConsumer<SourceFacade, TargetFacade> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param targetFacade Current {@code TargetFacade} instance
     */
    @Override
    public void accept(SourceFacade sourceFacade, TargetFacade targetFacade) {
        PluginReflectionUtility.getFieldAnnotations(sourceFacade).filter(a -> a.isAnnotationPresent(DialogWidgetAnnotation.class))
                .map(a -> a.getAnnotation(DialogWidgetAnnotation.class).source())
                .flatMap(source -> PluginRuntime.context().getReflectionUtility().getCustomDialogWidgetHandlers().stream()
                        .filter(handler -> source.equals(handler.getName())))
                .forEach(handler -> handler.accept(sourceFacade, targetFacade));

        PluginRuntime.context().getReflectionUtility()
                .getCustomDialogWidgetHandlers(PluginReflectionUtility.getFieldAnnotations(sourceFacade).collect(Collectors.toList()))
                .forEach(handler -> handler.accept(sourceFacade, targetFacade));

        /*Arrays.stream(sourceFacade.adaptTo(Property[].class))
                .forEach(p -> targetFacade.setAttribute(getXmlUtil().getValidFieldName(p.name()), p.value()));*/
    }
}
