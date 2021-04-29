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
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/numberfield/index.html">
 * NumberField</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.NUMBERFIELD)
@AnnotationRendering(properties = "all")
public @interface NumberField {

    /**
     * When set to a non-blank string, maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define a default value for a NumberField
     * @return String value, castable to a number
     */
    @ValueRestriction(ValueRestrictions.NUMBER)
    String value() default "0";

    /**
     * Maps to the {@code min} attribute of this Granite UI component's node.
     * Used to define a minimal number that can be stored in a NumberField
     * @return Double value
     */
    double min() default -Double.MAX_VALUE;

    /**
     * Maps to the {@code max} attribute of this Granite UI component's node.
     * Used to define a maximal number that can be stored in a NumberField
     * @return Double value
     */
    double max() default Double.MAX_VALUE;

    /**
     * Maps to the {@code step} attribute of this Granite UI component's node.
     * Used to define the delta between two sequential numbers when a user spins this NumberField
     * @return Double value
     */
    double step() default 1;
}
