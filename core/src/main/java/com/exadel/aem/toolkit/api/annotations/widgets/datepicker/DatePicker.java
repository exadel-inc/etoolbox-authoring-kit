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
package com.exadel.aem.toolkit.api.annotations.widgets.datepicker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.TypeHint;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/datepicker/index.html">
 * DatePicker</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.DATEPICKER)
@AnnotationRendering(properties = "!typeHint")
public @interface DatePicker {

    /**
     * When set to a non-blank string, maps to the {@code emptyText} attribute of this Granite UI component's node.
     * Used to define the text hint for an empty DatePicker
     * @return String value
     */
    String emptyText() default "";

    /**
     * Maps to the {@code type} attribute of this Granite UI component's node.
     * Used to specify whether this DatePicker allows picking date, time, or both
     * @see DatePickerType
     * @return One of {@code DatePickerType} values
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE)
    DatePickerType type() default DatePickerType.DATE;

    /**
     * Maps to the {@code displayedFormat} attribute of this Granite UI component's node.
     * Used to specify the date format for display.
     * See <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html">Java documentation</a>
     * on possible formats
     * @return String value
     */
    String displayedFormat() default "";

    /**
     * Maps to the {@code valueFormat} attribute of this Granite UI component's node.
     * Used to specify the date format for form submission and storage (when persisted as a string).
     * See <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html">Java documentation</a>
     * on possible formats
     * @return String value
     */
    String valueFormat() default "";

    /**
     * Maps to the {@code minDate} attribute of this Granite UI component's node.
     * Used to define the minimal date a user can specify
     * @see DateTimeValue
     * @return {@code DateTimeValue} instance
     */
    DateTimeValue minDate() default @DateTimeValue;

    /**
     * Maps to the {@code maxDate} attribute of this Granite UI component's node.
     * Used to define the maximal date a user can specify
     * @see DateTimeValue
     * @return {@code DateTimeValue} instance
     */
    DateTimeValue maxDate() default @DateTimeValue;

    /**
     * Maps to the {@code displayTimezoneMessage} attribute of this Granite UI component's node.
     * Defines whether an informative message should be displayed regarding timezone prevalence
     * @return True or false
     */
    boolean displayTimezoneMessage() default false;

    /**
     * Maps to the {@code typeHint} attribute of this Granite UI component's node.
     * Used to set that date values are persisted as strings formatted according to {@link DatePicker#valueFormat()};
     * default format otherwise
     * @return Value equal to "String", or none
     */
    TypeHint typeHint() default TypeHint.NONE;

    /**
     * When set to a non-blank string, maps to the {@code beforeSelector} attribute of this Granite UI component's node
     * @return String value
     */
    String beforeSelector() default "";

    /**
     * When set to a non-blank string, maps to the {@code afterSelector} attribute of this Granite UI component's node
     * @return String value
     */
    String afterSelector() default "";
}
