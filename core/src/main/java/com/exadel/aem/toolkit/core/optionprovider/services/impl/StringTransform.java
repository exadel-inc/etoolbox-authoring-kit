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

package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import java.util.function.UnaryOperator;

import org.apache.commons.lang.WordUtils;

/**
 * Represents standard string transformation routines to adjust datasource options' labels and values
 */
enum StringTransform {

    NONE(string -> string),

    LOWERCASE(String::toLowerCase),

    UPPERCASE(String::toUpperCase),

    CAPITALIZE(string -> WordUtils.capitalizeFully(string, new char[]{' ', '-'}));

    private final UnaryOperator<String> transformation;

    /**
     * Default constructor
     * @param transformation {@code UnaryOperator<String>} value
     */
    StringTransform(UnaryOperator<String> transformation) {
        this.transformation = transformation;
    }

    /**
     * Gets the selected string transformation
     * @return {@code UnaryOperator<String>} value
     */
    UnaryOperator<String> getTransformation() {
        return transformation;
    }
}
