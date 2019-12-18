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

import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidget;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidgets;

/**
 * Handler for storing {@link ResourceType} and like properties to a Granite UI widget XML node
 */
public class GenericPropertiesHandler implements BiConsumer<Element, Field> {
    private static final String RESTYPE_MISSING_EXCEPTION_MESSAGE = "@ResourceType is not present in ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element XML element
     * @param field Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {
        DialogWidget dialogWidget = DialogWidgets.fromField(field);
        if (dialogWidget == null || dialogWidget.getAnnotationClass() == null) {
            return;
        }
        element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, getResourceType(dialogWidget.getAnnotationClass()));
    }

    /**
     * Extracts {@link ResourceType} value from a Granite UI widget-defining annotation
     * @param value {@code Class} definition of the annotation
     * @return String representing the resource type
     */
    private String getResourceType(Class<?> value) {
        if(!value.isAnnotationPresent(ResourceType.class)){
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    RESTYPE_MISSING_EXCEPTION_MESSAGE + value.getName()));
            return null;
        }
        ResourceType resourceType = value.getDeclaredAnnotation(ResourceType.class);
        return resourceType.value();
    }
}
