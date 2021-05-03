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
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Position;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/checkbox/index.html">
 * Checkbox</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.CHECKBOX)
@AnnotationRendering(properties = "!disconnectedSublist")
public @interface Checkbox {

    /**
     * Maps to the {@code text} attribute of this Granite UI component's node.
     * Used to define optional text displayed beside the Checkbox
     * @return String value
     */
    String text() default "";

    /**
     * Maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define the value for a Checkbox when it is checked
     * @return String value, {@code {Boolean}}-casted
     */
    String value() default "{Boolean}true";

    /**
     * Maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define the value for a Checkbox when it is unchecked
     * @return String value, {@code {Boolean}}-casted
     */
    String uncheckedValue() default "{Boolean}false";

    /**
     * Maps to the {@code autosubmit} attribute of this Granite UI component's node.
     * When set to true, commands to automatically submit the form when the state of the Checkbox changes
     * @return True or false
     */
    boolean autosubmit() default false;

    /**
     * Maps to the {@code tooltipPosition} attribute of this Granite UI component's node.
     * Defines the position of the tooltip relative to the field. Effective only if {@code fieldDescription} is set
     * @return String value
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE, ignoreValues = "left")
    Position tooltipPosition() default Position.LEFT;

    /**
     * Maps to the {@code checked} attribute of this Granite UI component's node.
     * Used to define default state for a Checkbox
     * @return True or false
     */
    boolean checked() default false;

    /**
     * When set, allows defining a sublist of checkboxes. Its visibility will depend on the current checkbox state
     * @return Reference to a class describing the sublist
     */
    Class<?>[] sublist() default {};

    /**
     * Defines whether the {@code sublist} of checkboxes is a disconnected one. The {@code disconnected} attribute will
     * be set to the node that declares the list
     * @return True or false
     */
    boolean disconnectedSublist() default false;
}
