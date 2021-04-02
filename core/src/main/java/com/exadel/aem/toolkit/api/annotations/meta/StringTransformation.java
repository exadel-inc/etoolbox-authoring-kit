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

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.CaseFormat;

/**
 * Enumerates transformations of a string value that can be applied when rendering as an attribute of a Granite UI entity
 */
public enum StringTransformation {

    NONE(null),
    UPPERCASE(String::toUpperCase),
    LOWERCASE(String::toLowerCase),
    CAMELCASE(string -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, string.toLowerCase())),
    CAPITALIZE(string -> WordUtils.capitalizeFully(string, new char[] {' ', '-'}));

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

}
