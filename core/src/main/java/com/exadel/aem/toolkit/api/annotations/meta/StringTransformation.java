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
package com.exadel.aem.toolkit.api.annotations.meta;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import com.google.common.base.CaseFormat;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Enumerates transformations of a string value that can be applied when rendering as an attribute of a Granite UI entity
 */
public enum StringTransformation {

    /**
     * No transformation is applied (default value)
     */
    NONE(null),

    /**
     * The string is converted to uppercase
     */
    UPPERCASE(String::toUpperCase),

    /**
     * The string is converted to lowercase
     */
    LOWERCASE(String::toLowerCase),

    /**
     * The string is split into words delimited by a space, a hyphen, or an underscore. The words are then merged
     * into a {@code camelCase} string
     */
    CAMELCASE(StringTransformation::toCamelCase),

    /**
     * The string is split into words delimited by a space, a hyphen, or an underscore. The first letter of each
     * word is capitalized. The words are then merged with a space between them
     */
    CAPITALIZE(StringTransformation::capitalize);

    private static final char CHAR_SPACE = ' ';
    private static final char CHAR_HYPHEN = '-';
    private static final char CHAR_UNDERSCORE = '_';

    private final UnaryOperator<String> transformation;

    /**
     * Initializes this enum entry with a stored transformation routine
     * @param transformation {@code UnaryOperator} value
     */
    StringTransformation(UnaryOperator<String> transformation) {
        this.transformation = transformation;
    }

    /**
     * Applies a transformation to the provided string
     * @param value The string to be transformed
     * @return Resulting string value
     */
    public String apply(String value) {
        if (StringUtils.isBlank(value) || transformation == null) {
            return value;
        }
        return transformation.apply(value);
    }

    /* ---------------
       Transformations
       --------------- */

    /**
     * Converts a string to camelCase
     * @param value The string to be transformed
     * @return Resulting string value
     */
    private static String toCamelCase(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        return value.contains(CoreConstants.SEPARATOR_HYPHEN)
            ? CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, value.toLowerCase().replace(CHAR_SPACE, CHAR_HYPHEN))
            : CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, value.toLowerCase().replace(CHAR_SPACE, CHAR_UNDERSCORE));
    }

    /**
     * Capitalizes the first letter of each word in the provided string
     * @param value The string to be transformed
     * @return Resulting string value
     */
    private static String capitalize(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String[] words = StringUtils.split(value, " -_");
        return Stream.of(words)
            .map(StringUtils::lowerCase)
            .map(StringUtils::capitalize)
            .collect(Collectors.joining(StringUtils.SPACE));
    }
}
