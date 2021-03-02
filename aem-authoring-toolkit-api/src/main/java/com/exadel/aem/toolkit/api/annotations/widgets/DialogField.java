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

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Used to define common properties of TouchUI <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/field/index.html">
 * Dialog field</a>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping
public @interface DialogField {
    /**
     * When set to a non-blank string, maps to the 'fieldLabel' attribute of this TouchUI dialog component's node.
     * Used to define a label displayed beside this TouchUI dialog component
     * @return String value
     */
    @PropertyRendering(name = "fieldLabel")
    String label() default "";

    /**
     * When set to a non-blank string, maps to the 'fieldDescription' attribute of this TouchUI dialog component's node.
     * Used to define helper text for this TouchUI dialog component
     * @return String value
     */
    @PropertyRendering(name = "fieldDescription")
    String description() default "";

    /**
     * Maps to the 'renderHidden' attribute of this TouchUI dialog component's node.
     * If set to true, the whole TouchUI dialog component is hidden from user
     * @return True or false
     */
    boolean renderHidden() default false;

    /**
     * When set to a non-blank string, maps to the 'wrapperClass' attribute of this TouchUI dialog component's node.
     * Used to specify a CSS class for the wrapper element
     * @return String value
     */
    String wrapperClass() default "";

    /**
     * Used to override 'name' attribute value for this TouchUI dialog component.
     * If not set, 'name' will be equal to the annotated field name
     * @return String value
     */
    @IgnorePropertyMapping
    String name() default "";

    /**
     * Used to sort dialog components in the TouchUI interface. If none of the components has non-zero ranking, they
     * will be rendered as they appear in Java class. Otherwise, components with smaller rankings will have precedence,
     * Both positive and negative values allowed
     * @return Integer value
     */
    int ranking() default Integer.MIN_VALUE;

    /**
     * Maps to the 'required' attribute of this TouchUI dialog component's node.
     * @return True or false
     */
    boolean required() default false;

    /**
     * Maps to the 'disabled' attribute of this TouchUI dialog component's node.
     * @return True or false
     */
    boolean disabled() default false;

    /**
     * Maps to the 'validation' attribute of this TouchUI dialog component's node.
     * @return String value, or an array of non-blank strings
     */
    String[] validation() default {};
}
