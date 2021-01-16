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
package com.exadel.aem.toolkit.plugin.handlers.widget.common;

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.plugin.handlers.widget.DialogWidget;
import com.exadel.aem.toolkit.plugin.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * Handler for storing {@link ResourceType} and like properties to a Granite UI widget node
 */
public class GenericPropertiesHandler implements BiConsumer<Source, Target> {
    private static final String RESTYPE_MISSING_EXCEPTION_MESSAGE = "@ResourceType is not present in ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        DialogWidget dialogWidget = DialogWidgets.fromSource(source);
        if (dialogWidget == null || dialogWidget.getAnnotationClass() == null) {
            return;
        }
        target.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, getResourceType(dialogWidget.getAnnotationClass()));
    }

    /**
     * Extracts {@link ResourceType} value from a Granite UI widget-defining annotation
     * @param value {@code Class} definition of the annotation
     * @return String representing the resource type
     */
    private String getResourceType(Class<?> value) {
        if(!value.isAnnotationPresent(ResourceType.class)) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    RESTYPE_MISSING_EXCEPTION_MESSAGE + value.getName()));
            return null;
        }
        ResourceType resourceType = value.getDeclaredAnnotation(ResourceType.class);
        return resourceType.value();
    }
}
