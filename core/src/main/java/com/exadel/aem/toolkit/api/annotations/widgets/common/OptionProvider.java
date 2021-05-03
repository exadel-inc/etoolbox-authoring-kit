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
package com.exadel.aem.toolkit.api.annotations.widgets.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Represents a provider of options for option-selecting Granite UI components, such as {@link com.exadel.aem.toolkit.api.annotations.widgets.select.Select}
 * or {@link com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup}. An OptionProvider manages one or more
 * sources, such as EToolbox Lists, ACS Commons lists, tag folders, arbitrary JCR nodes with their children, etc., and
 * renders the cumulative set of options. Above all, options can be set or appended to the list indicatively, i.e.
 * via a string array, without the need to query for JCR values. For each of the options, specific title, value, and HTML
 * attributes can be set
 */
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "all")
public @interface OptionProvider {

    /**
     * Allows to define one or more {@link OptionSource}s referring options stored in JCR
     * @return Zero or more {@code OptionSource} objects
     */
    OptionSource[] value() default {};

    /**
     * Allows specifying options that will be prepended to the list provided by JCR source(-s). If no {@code OptionSource}s
     * are specified, the option set can as well consist exclusively of options declared here, or be the composition of
     * values stored in {@code OptionProvider#prepend} and {@code OptionProvider#append}.
     * <p>The specified values must follow the {@code "Title:value"} format</p>
     * @return Optional string value, or an array of strings
     */
    String[] prepend() default {};

    /**
     * Allows specifying options that will be appended to the list provided by JCR source(-s). If no {@code OptionSource}s
     * are specified, the option set can as well consist exclusively of options declared here, or be the composition of
     * values stored in {@code OptionProvider#prepend} and {@code OptionProvider#append}.
     * <p>The specified values must follow the {@code "Title:value"} format</p>
     * @return Optional string value, or an array of strings
     */
    String[] append() default {};

    /**
     * Allows specifying options coming from a JCR source(-s) that need to be excluded from the list. Strings passed
     * here are expected match values or else titles of unwanted options. The matching is case-insensitive. {@code *}
     * is used as the wildcard symbol
     * @return Optional string value, or an array of strings
     */
    String[] exclude() default {};

    /**
     * Specifies the value selected by default
     * @return Optional string value
     */
    @PropertyRendering(name = "selected")
    String selectedValue() default "";

    /**
     * Specifies whether the list of options needs to be alphabetically sorted
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean sorted() default false;
}
