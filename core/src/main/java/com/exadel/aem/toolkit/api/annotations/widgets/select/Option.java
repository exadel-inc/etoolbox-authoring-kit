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
package com.exadel.aem.toolkit.api.annotations.widgets.select;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.common.StatusVariant;

/**
 * Used to define an option within the {@link Select#options()} set
 * See documentation on <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html">
 * Select</a> component
 */
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "all")
public @interface Option {

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
     * When set to a non-blank string, maps to the {@code icon} attribute of this Granite UI component's node
     * @return String value
     */
    String icon() default "";

    /**
     * When set to a non-blank string, maps to the {@code statusIcon} attribute of this Granite UI component's node
     * @return String value
     */
    String statusIcon() default "";

    /**
     * When set to a non-blank string, maps to the {@code statusText} attribute of this Granite UI component's node.
     * @return String value
     */
    String statusText() default "";

    /**
     * When set to a non-blank string, maps to the {@code statusVariant} attribute of this Granite UI component's node
     * @return String value
     * @see StatusVariant
     */
    @PropertyRendering(ignoreValues = "info")
    StatusVariant statusVariant() default StatusVariant.INFO;

    /**
     * When set to true, maps to the {@code selected} attribute of this Granite UI component's node.
     * Defines that the current option is selected by default
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean selected() default false;

    /**
     * When set to true, maps to the {@code disabled} attribute of this Granite UI component's node.
     * Defines that the current option is shown in the disabled state
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean disabled() default false;
}
