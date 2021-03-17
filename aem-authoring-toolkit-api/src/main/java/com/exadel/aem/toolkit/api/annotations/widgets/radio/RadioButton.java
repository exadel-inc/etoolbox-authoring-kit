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
package com.exadel.aem.toolkit.api.annotations.widgets.radio;

import com.exadel.aem.toolkit.api.annotations.meta.MapProperties;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Used to define an option within {@link RadioGroup#buttons()} set
 */
@MapProperties
public @interface RadioButton {
    /**
     * Maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define optional text displayed beside the option box
     * @return String value
     */
    String text();

    /**
     * Maps to the 'value' attribute of this TouchUI dialog component's node.
     * Used to define value to be stored when this option is checked
     * @return String value
     */
    @PropertyRendering(allowBlank = true)
    String value();

    /**
     * When set to true, maps to the 'checked' attribute of this TouchUI dialog component's node.
     * Defines the current option is selected by default
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean checked() default false;

    /**
     * When set to true, maps to the 'disabled' attribute of this TouchUI dialog component's node.
     * Defines the current option is shown in disabled state
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean disabled() default false;
}
