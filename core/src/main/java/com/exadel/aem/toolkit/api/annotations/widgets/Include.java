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
package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/include/index.html">
 * Include</a> component in Granite UI. It provides the possibility to embed a JCR resource reachable by a Sling
 * {@code ResourceResolver} into a dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.INCLUDE)
@AnnotationRendering(properties = "all")
public @interface Include {

    /**
     * Maps to the {@code path} attribute of this Granite UI component's node. Represents the path of the included resource
     * @return String value, non-blank
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String path();

    /**
     * When set to a non-blank value, maps to the {@code resourceType} attribute of this Granite UI component's node.
     * Represents the resource type of the included resource (if not set, the type specified in resource itself is used)
     * @return String value
     */
    String resourceType() default "";
}
