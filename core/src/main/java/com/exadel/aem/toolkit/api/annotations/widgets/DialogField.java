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
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Used to define common properties of Granite UI <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/field/index.html">
 * dialog field</a>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "!name")
public @interface DialogField {

    /**
     * When set to a non-blank string, maps to the {@code fieldLabel} attribute of this dialog component's node.
     * Used to define a label displayed beside this dialog component
     * @return String value (optional)
     */
    @PropertyRendering(name = "fieldLabel")
    String label() default "";

    /**
     * When set to a non-blank string, maps to the {@code fieldDescription} attribute of this dialog component's node.
     * Used to define helper text for this dialog component
     * @return String value (optional)
     */
    @PropertyRendering(name = "fieldDescription")
    String description() default "";

    /**
     * Maps to the {@code renderHidden} attribute of this dialog component's node.
     * If set to true, the {@code hidden} attribute is added to the HTML element that wraps up the current component,
     * so that the component is hidden together with its label and description. Has no effect if a wrapper element is not
     * rendered (e.g. when neither label nor description is specified)
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
     * Used to define {@code name} attribute value for this Granite UI dialog component.
     * If not set, the name of the annotated field or method will be used
     * @return String value (optional)
     */
    String name() default "";

    /**
     * Used to order dialog components in the Granite UI interface. Components with smaller rankings appear before those
     * with greater rankings
     * @return Integer value. Both positive and negative values allowed
     */
    int ranking() default Integer.MIN_VALUE;

    /**
     * Maps to the {@code required} attribute of this Granite UI component's node
     * @return True or false
     */
    boolean required() default false;

    /**
     * Maps to the {@code disabled} attribute of this Granite UI component's node
     * @return True or false
     */
    boolean disabled() default false;

    /**
     * Maps to the {@code validation} attribute of this Granite UI component's node
     * @return String value, or an array of non-blank strings
     */
    String[] validation() default {};
}
