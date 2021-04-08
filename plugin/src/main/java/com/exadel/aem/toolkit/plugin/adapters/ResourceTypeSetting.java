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
package com.exadel.aem.toolkit.plugin.adapters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;

/**
 * Implements {@link Adapts} for extracting appropriate {@code sling:resourceType} value from a {@link Source} object
 */
@Adapts(Source.class)
public class ResourceTypeSetting {
    private final Source wrappedSource;

    /**
     * Instance constructor per the {@link Adapts} contract
     * @param source {@code Source} object that will be used for extracting resource type
     */
    public ResourceTypeSetting(Source source) {
        this.wrappedSource = source;
    }

    /**
     * Retrieves {@code sling:resourceType} value if can be extracted or derived from the {@code Source} provided
     * @return String value, or an empty string
     */
    public String getValue() {
        if (wrappedSource == null) {
            return StringUtils.EMPTY;
        }
        Annotation[] annotations = wrappedSource.adaptTo(Annotation[].class);
        if (ArrayUtils.isEmpty(annotations)) {
            return StringUtils.EMPTY;
        }

        String resourceTypeValue = getValueByResourceTypeAnnotation(annotations);
        if (resourceTypeValue != null) {
            return resourceTypeValue;
        }
        resourceTypeValue = getValueByAnnotationProperty(annotations);

        return StringUtils.defaultString(resourceTypeValue);
    }

    /**
     * Called by {@link ResourceTypeSetting#getValue()} to try to retrieve the resource type value if declared
     * by {@link ResourceType}
     * @param annotations Collection of {@code Annotation} object to search for the resource type
     * @return String value if found; otherwise, null
     */
    private static String getValueByResourceTypeAnnotation(Annotation[] annotations) {
        return Arrays.stream(annotations)
            .map(annotation -> annotation.annotationType().getDeclaredAnnotation(ResourceType.class))
            .filter(Objects::nonNull)
            .map(ResourceType::value)
            .findFirst()
            .orElse(null);
    }

    /**
     * Called by {@link ResourceTypeSetting#getValue()} to try to retrieve the resource type value if produces
     * by any of the annotations' properties
     * @param annotations Collection of {@code Annotation} object to search for the resource type
     * @return String value if found; otherwise, null
     */
    private static String getValueByAnnotationProperty(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Method resourceTypeMethod = Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(method -> method.getDeclaredAnnotation(PropertyRendering.class) != null)
                .findFirst()
                .orElse(null);
            if (resourceTypeMethod != null) {
                return AnnotationUtil.getProperty(annotation, resourceTypeMethod, StringUtils.EMPTY).toString();
            }
        }
        return StringUtils.EMPTY;
    }
}
