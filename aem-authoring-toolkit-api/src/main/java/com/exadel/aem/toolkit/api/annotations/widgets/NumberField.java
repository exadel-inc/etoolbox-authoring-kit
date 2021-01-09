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
package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/numberfield/index.html">
 * NumberField element</a> in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.NUMBERFIELD)
@PropertyMapping
@SuppressWarnings("unused")
public @interface NumberField {
    /**
     * When set to a non-blank string, maps to the 'value' attribute of this TouchUI dialog component's node.
     * Used to define default value for a NumberField
     * @return String value, castable to number
     */
    @ValueRestriction(ValueRestrictions.NUMBER)
    String value() default "0";
    /**
     * Maps to the 'min' attribute of this TouchUI dialog component's node.
     * Used to define minimal number that may be stored in a NumberField
     * @return Double value
     */
    double min() default -Double.MAX_VALUE;
    /**
     * Maps to the 'max' attribute of this TouchUI dialog component's node.
     * Used to define maximal number that may be stored in a NumberField
     * @return Double value
     */
    double max() default Double.MAX_VALUE;
    /**
     * Maps to the 'step' attribute of this TouchUI dialog component's node.
     * Used to define delta between two sequential numbers when a user spins this NumberField via the UI
     * @return Double value
     */
    double step() default 1;
}
