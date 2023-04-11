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
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Represents a single option source that can be defined for an {@link OptionProvider}. An option source points to an
 * option supplier, such as a JCR node, an HTTP endpoint, or a Java enumeration, and specifies the way to render the
 * data
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionSource {

    /**
     * Specifies an address from which the options can be retrieved. The address can be, e.g., a JCR path, an HTTP
     * endpoint, or a fully qualified name of a Java class. One is expected to specify this or
     * {@link OptionSource#enumeration()}. If both are specified, only {@code value} is used. If none is specified, an
     * exception is thrown
     * @return String value
     */
    String value() default "";

    /**
     * Specifies a Java class that will be used as the source of options. The class can be an Enum or a collection of
     * constants. One is expected to specify either this or {@link OptionSource#value()}. If both are specified, only
     * {@code value} is used. If none is specified, an exception is thrown
     * @return {@link Class} value
     */
    Class<?> enumeration() default _Default.class;

    /**
     * Sets the fallback address. From this address, the options can be retrieved when the source (determined by
     * {@link OptionSource#value()} or {@link OptionSource#enumeration()}) is not valid or unreachable
     * @return Optional string value
     * @deprecated This property is deprecated and will be removed in a version after 2.3.0. Please use
     * {@link OptionSource#isFallback()}
     */
    @Deprecated
    String fallback() default "";

    /**
     * If set, specifies the name of an attribute of the underlying option resource (such as a JCR node) to be used as
     * the selectable option's text
     * @return String value
     */
    String textMember() default "";

    /**
     * If set, specifies the name of an attribute of the underlying option resource (such as a JCR node) to be used as
     * the selectable option's text
     * @return String value
     */
    String valueMember() default "";

    /**
     * If set, specifies one or more names of attributes of the underlying option resource (such as a JCR node) that are
     * to be rendered as the selectable option's attributes
     * @return String value, or an array of strings
     */
    String[] attributeMembers() default {};

    /**
     * If set, specifies one or more string values that are to be rendered as the selectable option's attributes
     * @return String value, or an array of strings
     */
    String[] attributes() default {};

    /**
     * If set, specifies the way to transform option text before rendering in UI
     * @return String value
     * @see com.exadel.aem.toolkit.api.annotations.meta.StringTransformation
     */
    StringTransformation textTransform() default StringTransformation.NONE;

    /**
     * If set, specifies the way to transform option value before rendering in UI
     * @return String value
     * @see com.exadel.aem.toolkit.api.annotations.meta.StringTransformation
     */
    StringTransformation valueTransform() default StringTransformation.NONE;

    /**
     * If set to {@code true}, specifies that the current {@link OptionSource} is to be used as the fallback within the
     * current {@link OptionProvider}. This property does not have an effect if there is only one option source set
     * @return True or false
     */
    boolean isFallback() default false;
}
