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
package com.exadel.aem.toolkit.api.annotations.widgets.radio;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Used to define an option within the {@link RadioGroup#buttons()} set
 */
@AnnotationRendering(properties = "all")
public @interface RadioButton {

    /**
     * Maps to the {@code text} attribute of this Granite UI component's node.
     * Used to define the optional text displayed beside the option box
     * @return String value
     */
    String text();

    /**
     * Maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define the value to be stored when this option is checked
     * @return String value
     */
    @PropertyRendering(allowBlank = true)
    String value();

    /**
     * When set to true, maps to the {@code checked} attribute of this Granite UI component's node.
     * Defines that the current option is selected by default
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean checked() default false;

    /**
     * When set to true, maps to the {@code disabled} attribute of this Granite UI component's node.
     * Defines that the current option is shown in the disabled state
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean disabled() default false;
}
