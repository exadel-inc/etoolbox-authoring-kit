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
package com.exadel.aem.toolkit.api.annotations.widgets.buttongroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;

/**
 * Used to define an option within the {@link ButtonGroup#items()} set
 * See documentation on <a href="https://www.adobe.io/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/buttongroup/index.html">
 * ButtonGroup</a> component
 */
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "all")
public @interface ButtonGroupItem {

    /**
     * Maps to the {@code text} attribute of this Granite UI component's node.
     * Used to define the text displayed beside the option box
     * @return String value
     */
    String text();

    /**
     * Maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define the value to be stored when this item is checked
     * @return String value
     */
    @PropertyRendering(allowBlank = true)
    String value();

    /**
     * When set to true, maps to the {@code disabled} attribute of this Granite UI component's node.
     * Defines that the current option is shown in the disabled state
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean disabled() default false;

    /**
     * When set to true, maps to the {@code checked} attribute of this Granite UI component's node.
     * Defines that the current option is checked by default
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean checked() default false;

    /**
     * Maps to the {@code hideText} attribute of this Granite UI component's node.
     * Used to define whether the text is hidden
     * @return True or false
     */
    boolean hideText() default false;

    /**
     * When set to a non-blank string, maps to the {@code icon} attribute of this Granite UI component's node.
     * Used to define the icon of the button item
     * @return String value
     */
    String icon() default "";

    /**
     * When set to a non-blank string, maps to the {@code iconSize} attribute of this Granite UI component's node.
     * Used to specify icon size
     * @return One of {@code Size} values
     * @see Size
     */
    @PropertyRendering(ignoreValues = "S")
    Size size() default Size.SMALL;
}
