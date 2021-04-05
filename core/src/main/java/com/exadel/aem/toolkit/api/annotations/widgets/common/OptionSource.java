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

import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Represents a single option source that can be defined for an {@link OptionProvider}. An option source points to
 * an option storage, such as a JCR path, and specifies the way to render the data from this particular storage
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionSource {

    /**
     * Used to specify the precise address the options can be retrieved from, such as a JCR path
     * @return String value, non-blank
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String value();

    /**
     * Used to specify the fallback address the options can be retrieved from when the main source ({@link OptionSource#value()})
     * is not valid or unreachable
     * @return Optional string value
     */
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
     * If set, specifies one or more names of attributes of the underlying option resource (such as a JCR node)
     * that are to be rendered as the selectable option's attributes
     * @return String value, or an array of strings
     */
    String[] attributeMembers() default {};

    /**
     * If set, specifies one or more string values that are to be rendered as the selectable option's attributes
     * @return String value, or an array of strings
     */
    String[] attributes() default {};

    /**
     * If set, specifies the way to transform option text as it is coming from a storage before rendering in UI
     * @return String value
     * @see com.exadel.aem.toolkit.api.annotations.meta.StringTransformation
     */
    StringTransformation textTransform() default StringTransformation.NONE;

    /**
     * If set, specifies the way to transform option value as it is coming from a storage before rendering in UI
     * @return String value
     * @see com.exadel.aem.toolkit.api.annotations.meta.StringTransformation
     */
    StringTransformation valueTransform() default StringTransformation.NONE;
}
