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
package com.exadel.aem.toolkit.api.annotations.widgets.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/colorfield/index.html">
 * ColorField</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.COLORFIELD)
@AnnotationRendering(properties = "!customColors")
public @interface ColorField {

    /**
     * Maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define default value of ColorField
     * @return String value in either HEX, RGB, RGBA, HSB, or CMYK format
     */
    String value() default "";

    /**
     * When set to a non-blank string, maps to the {@code emptyText} attribute of this Granite UI component's node.
     * Used to define the text hint for an empty ColorField
     * @return String value
     */
    String emptyText() default "";

    /**
     * Maps to the {@code variant} attribute of this Granite UI component's node.
     * Used to specify the behavior set of this ColorField
     * @see ColorVariant
     * @return One of {@code ColorVariant} values
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE)
    ColorVariant variant() default ColorVariant.DEFAULT;

    /**
     * Maps to the {@code autogenerateColors} attribute of this Granite UI component's node.
     * Used to specify the mode of auto color generation
     * @see GenerateColorsState
     * @return One of {@code GenerateColorsState} values
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE)
    GenerateColorsState autogenerateColors() default GenerateColorsState.OFF;

    /**
     * Maps to the {@code showSwatches} attribute of this Granite UI component's node.
     * Used to specify whether the swatches should be displayed
     * @return True or false
     */
    boolean showSwatches() default true;

    /**
     * Maps to the {@code showProperties} attribute of this Granite UI component's node.
     * Used to specify whether color properties should be displayed
     * @return True or false
     */
    boolean showProperties() default true;

    /**
     * Maps to the {@code showDefaultColors} attribute of this Granite UI component's node.
     * Used to specify whether default colors should be displayed
     * @return True or false
     */
    boolean showDefaultColors() default true;

    /**
     * Used to render a collection of user-set custom colors
     * @return An array of non-blank strings, or null
     */
    String[] customColors() default {};
}
