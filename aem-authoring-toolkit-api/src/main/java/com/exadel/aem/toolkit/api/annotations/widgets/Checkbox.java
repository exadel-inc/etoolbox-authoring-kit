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
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/checkbox/index.html">
 * checkbox element</a> in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.CHECKBOX)
@PropertyMapping
public @interface Checkbox {
    /**
     * Maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define optional text displayed beside the Checkbox
     * @return String value
     */
    String text() default "";

    /**
     * Maps to the 'value' attribute of this TouchUI dialog component's node.
     * Used to define value for a Checkbox when it is checked
     * @return String {@code {Boolean}}-casted value
     */
    String value() default "{Boolean}true";

    /**
     * Maps to the 'value' attribute of this TouchUI dialog component's node.
     * Used to define value for a Checkbox when it is unchecked
     * @return String {@code {Boolean}}-casted value
     */
    String uncheckedValue() default "{Boolean}false";

    /**
     * Maps to the 'autosubmit' attribute of this TouchUI dialog component's node.
     * When set to true, commands to automatically submit the form when state of the Checkbox changes
     * @return True or false
     */
    boolean autosubmit() default false;

    /**
     * Maps to the 'tooltipPosition' attribute of this TouchUI dialog component's node.
     * When set to a non-blank string, defines the position of the tooltip relative to the field
     * @return String value
     */
    String tooltipPosition() default "";

    /**
     * Maps to the 'checked' attribute of this TouchUI dialog component's node.
     * Used to define default state for a Checkbox
     * @return True or false
     */
    boolean checked() default false;

    /**
     * When set, allows to define a sublist of TouchUI dialog components (other checkboxes).
     * Its visibility will depend on current Checkbox state
     * @return Reference to a class describing sublist
     */
    @IgnorePropertyMapping
    Class<?>[] sublist() default {};

    /**
     * When set, allows to define a sublist of TouchUI dialog components (other checkboxes).
     * The 'disconnected' attribute will be set to the node that declares the sublist
     * @return Reference to a class describing sublist
     */
    @IgnorePropertyMapping
    boolean disconnectedSublist() default false;
}
