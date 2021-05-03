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
package com.exadel.aem.toolkit.plugin.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for parsing and formatting string values
 */
public class StringUtil {
    private static final String TYPE_TOKEN_TEMPLATE = "{%s}";
    private static final String TYPED_VALUE_TEMPLATE = "{%s}%s";
    private static final String ARRAY_TEMPLATE = "[%s]";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final String ARRAY_OPENING = "[";
    private static final String ARRAY_CLOSING = "]";
    private static final String ESCAPE_STRING = "\\";
    private static final String SPLITTING_PATTERN = "\\s*,\\s*";

    private static final Pattern ATTRIBUTE_LIST_PATTERN = Pattern.compile("^\\[.+]$");

    private static final Function<Object, String> DEFAULT_CONVERSION = Object::toString;
    private static final Map<Class<?>, Function<Object, String>> CONVERSIONS = ImmutableMap.of(
        Date.class, v -> new SimpleDateFormat(DATE_FORMAT).format(v)
    );

    /**
     * Default (instantiation-restricting) constructor
     */
    private StringUtil() {
    }

    /* ----------------------
       Formatting plain types
       ---------------------- */

    /**
     * Converts an arbitrary value into a JCR-compliant attribute value string
     * @param value     The value to convert
     * @param valueType {@code Class<?>} reference representing the type of the value
     * @param <T>       Type of the value
     * @return String representing the value. If {@code null} reference was passed, an empty string is returned
     */
    public static <T> String format(T value, Class<?> valueType) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        if (String.class.equals(valueType)) {
            return value.toString();
        } else {
            String effectiveString = CONVERSIONS.getOrDefault(valueType, DEFAULT_CONVERSION).apply(value);
            return String.format(TYPED_VALUE_TEMPLATE, valueType.getSimpleName(), effectiveString);
        }
    }


    /* ---------------------------
       Formatting collection types
       --------------------------- */

    /**
     * Converts an array of arbitrary values into a JCR-compliant attribute value string
     * @param value     The array to convert
     * @param valueType {@code Class<?>} reference representing the type of the array
     * @param <T>       Type of the value
     * @return String representing the value. If {@code null} reference was passed, an empty string is returned
     */
    public static <T> String format(T[] value, Class<?> valueType) {
        if (value == null || value.length == 0) {
            return StringUtils.EMPTY;
        }
        return format(Arrays.stream(value).collect(Collectors.toList()), valueType);
    }

    /**
     * Converts a collection of arbitrary values into a JCR-compliant attribute value string
     * @param value     The array to convert
     * @param valueType {@code Class<?>} reference representing the underlying collection type
     * @param <T>       Type of the value
     * @return String representing the value. If {@code null} reference was passed, an empty string is returned
     */
    public static <T> String format(Collection<T> value, Class<?> valueType) {
        Function<Object, String> conversion = CONVERSIONS.getOrDefault(valueType, DEFAULT_CONVERSION);
        return format(value, valueType, conversion);
    }

    /**
     * Converts a collection of arbitrary values into a JCR-compliant attribute value string. Every member of the collection
     * is converted with the provided routine before being written to the resulting string
     * @param value      The array to convert
     * @param valueType  {@code Class<?>} reference representing the underlying collection type
     * @param conversion {@code Function<Object, String>} to convert every passed collection member before storing
     * @param <T>        Type of the value
     * @return String representing the value. If {@code null} reference was passed, empty string is returned
     */
    private static <T> String format(Collection<T> value, Class<?> valueType, Function<Object, String> conversion) {
        if (value == null || value.isEmpty()) {
            return StringUtils.EMPTY;
        }
        String collectionPart = value.stream()
            .filter(Objects::nonNull)
            .map(conversion)
            .map(s -> s.startsWith(ARRAY_OPENING) && s.endsWith(ARRAY_CLOSING) ? ESCAPE_STRING + s : s)
            .collect(Collectors.joining(CoreConstants.SEPARATOR_COMMA));
        if (String.class.equals(valueType)) {
            return String.format(ARRAY_TEMPLATE, collectionPart);
        } else {
            return String.format(TYPE_TOKEN_TEMPLATE, valueType.getSimpleName()) + String.format(ARRAY_TEMPLATE, collectionPart);
        }
    }


    /* -------------------
       Parsing collections
       ------------------- */

    /**
     * Gets whether the passed string contains a JCR-compliant array/collection of values
     * @param value The string to analyze
     * @return True or false
     */
    public static boolean isCollection(String value) {
        return ATTRIBUTE_LIST_PATTERN.matcher(value).matches();
    }

    /**
     * Parses the provided string as a JCR-compliant array/collection and returns a {@code Set} of stored values
     * @param value The string to analyze
     * @return {@code Set} instance. If {@code null} was passed, an empty set is returned
     */
    public static Set<String> parseSet(String value) {
        if (StringUtils.isEmpty(value)) {
            return new HashSet<>();
        }
        return new LinkedHashSet<>(Arrays.asList(StringUtils
            .strip(value, ARRAY_OPENING + ARRAY_CLOSING)
            .split(SPLITTING_PATTERN)));
    }

    /**
     * Parses the provided string as a JCR-compliant array/collection and returns a {@code List} of stored values
     * @param value The string to analyze
     * @return {@code List} instance. If {@code null} was passed, an empty list is returned
     */
    public static List<String> parseCollection(String value) {
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        return Arrays.asList(StringUtils
            .strip(value, ARRAY_OPENING + ARRAY_CLOSING)
            .split(SPLITTING_PATTERN));
    }
}
