/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.core.handlers.widget.DialogComponent;

public class GenericPropertiesHandler implements BiConsumer<Element, Field> {
    private static final String RESTYPE_MISSING_EXCEPTION_MESSAGE = "@ResourceType is not present in ";

    @Override
    public void accept(Element element, Field field) {
        DialogComponent dialogComponent = DialogComponent.fromField(field).orElse(null);
        if (dialogComponent == null || dialogComponent.getAnnotationClass() == null) {
            return;
        }
        element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, this.getResourceType(dialogComponent.getAnnotationClass()));
    }

    private String getResourceType(Class<?> annotationClass){
        if(!annotationClass.isAnnotationPresent(ResourceType.class)){
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    RESTYPE_MISSING_EXCEPTION_MESSAGE + annotationClass.getName()));
            return null;
        }
        ResourceType resourceType = annotationClass.getDeclaredAnnotation(ResourceType.class);
        return resourceType.value();
    }
}
