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
import com.exadel.aem.toolkit.api.annotations.meta.MapProperties;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Used to define common properties of TouchUI <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/field/index.html">
 * Dialog field</a>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@MapProperties
public @interface DialogField {

    /**
     * When set to a non-blank string, maps to the {@code fieldLabel} attribute of this dialog component's node.
     * Used to define a label displayed beside this dialog component
     * @return String value (optional)
     */
    @PropertyRendering(name = "fieldLabel")
    String label() default "";

    /**
     * When set to a non-blank string, maps to the 'fieldDescription' attribute of this dialog component's node.
     * Used to define helper text for this dialog component
     * @return String value (optional)
     */
    @PropertyRendering(name = "fieldDescription")
    String description() default "";

    /**
     * Maps to the {@code renderHidden} attribute of this dialog component's node.
     * If set to true, the {@code hidden} attribute added to the HTML element that wraps up the current component, so that
     * the component is hidden together with its label and description. Has no effect if a wrapper element is not
     * rendered (e.g. when neither label nor description specified)
     * @return True or false
     */
    boolean renderHidden() default false;

    /**
     * When set to a non-blank string, maps to the {@code wrapperClass} attribute of this dialog component's node.
     * Used to specify a CSS class for the wrapper element
     * @return String value (optional)
     */
    String wrapperClass() default "";

    /**
     * Used to define {@code name} attribute value for this TouchUI dialog component.
     * If not set, the name of the annotated field or method will be stored
     * @return String value (optional)
     */
    @IgnorePropertyMapping
    String name() default "";

    /**
     * Used to order dialog components in the TouchUI interface. If none of the components has non-zero ranking, they
     * will be rendered as they appear in Java class. Otherwise, components with smaller rankings will have precedence,
     * @return Integer value. Both positive and negative values allowed
     */
    int ranking() default Integer.MIN_VALUE;

    /**
     * Maps to the {@code required} attribute of this TouchUI dialog component's node.
     * @return True or false
     */
    boolean required() default false;

    /**
     * Maps to the {@code disabled} attribute of this TouchUI dialog component's node.
     * @return True or false
     */
    boolean disabled() default false;

    /**
     * Maps to the {@code validation} attribute of this TouchUI dialog component's node.
     * @return String value, or an array of non-blank strings
     */
    String[] validation() default {};
}
