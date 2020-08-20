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
package com.exadel.aem.toolkit.api.annotations.widgets.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.EnumValue;
import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/colorfield/index.html">
 * ColorField element</a> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.COLORFIELD)
@PropertyMapping
@SuppressWarnings("unused")
public @interface ColorField {
    /**
     * Maps to the 'value' attribute of this TouchUI dialog component's node.
     * Used to define default value of ColorField
     * @return String value in either HEX, RGB, RGBA, HSB, or CMYK format
     */
    String value() default "";

    /**
     * When set to a non-blank string, maps to the 'emptyText' attribute of this TouchUI dialog component's node.
     * Used to define text hint for an empty ColorField
     * @return String value
     */
    String emptyText() default "";

    /**
     * Maps to the 'variant' attribute of this TouchUI dialog component's node.
     * Used to specify the capabilities set of this ColorField
     * @see ColorVariant
     * @return One of {@code ColorVariant} values
     */
    @EnumValue(transformation = StringTransformation.LOWERCASE)
    ColorVariant variant() default ColorVariant.DEFAULT;

    /**
     * Maps to the 'autogenerateColors' attribute of this TouchUI dialog component's node.
     * Used to specify the mode of auto color generating
     * @see GenerateColorsState
     * @return One of {@code GenerateColorsState} values
     */
    @EnumValue(transformation = StringTransformation.LOWERCASE)
    GenerateColorsState autogenerateColors() default GenerateColorsState.OFF;
    /**
     * Maps to the 'showSwatches' attribute of this TouchUI dialog component's node.
     * Used to specify whether swatches view should be displayed
     * @return True or false
     */
    boolean showSwatches() default true;

    /**
     * Maps to the 'showProperties' attribute of this TouchUI dialog component's node.
     * Used to specify whether color properties view should be displayed
     * @return True or false
     */
    boolean showProperties() default true;

    /**
     * Maps to the 'showDefaultColors' attribute of this TouchUI dialog component's node.
     * Used to specify whether default colors should be displayed
     * @return True or false
     */
    boolean showDefaultColors() default true;

    /**
     * Used to render a collection of user-set custom colors
     * @return An array of non-blank strings, or null
     */
    @IgnorePropertyMapping
    String[] customColors() default {};
}
