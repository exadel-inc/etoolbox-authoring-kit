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
package com.exadel.aem.toolkit.core.optionprovider.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for manipulating string patterns (strings with wildcards)
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class PatternUtil {

    /**
     * Default (instantiation-preventing) constructor
     */
    private PatternUtil() {
    }

    /**
     * Gets whether the given string represents a pattern
     * @param value A nullable string value
     * @return True or false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPattern(String value) {
        return StringUtils.startsWith(value, CoreConstants.WILDCARD) || StringUtils.endsWith(value, CoreConstants.WILDCARD);
    }

    /**
     * Gets whether the given string matches at least one of the provided patterns
     * @param value    A nullable string value
     * @param patterns A nullable collection of patterns
     * @return True or false
     */
    public static boolean isMatch(String value, List<String> patterns) {
        if (patterns == null) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> isMatch(value, pattern));
    }

    /**
     * Gets whether the given string matches the provided pattern
     * @param value   A nullable string value
     * @param pattern A nullable string pattern
     * @return True or false
     */
    public static boolean isMatch(String value, String pattern) {
        if (StringUtils.isAnyEmpty(value, pattern)) {
            return false;
        }
        if (StringUtils.startsWith(pattern, CoreConstants.WILDCARD) && StringUtils.endsWith(pattern, CoreConstants.WILDCARD)) {
            return StringUtils.contains(value, pattern.substring(1, pattern.length() - 1));
        }
        if (StringUtils.startsWith(pattern, CoreConstants.WILDCARD)) {
            return StringUtils.endsWith(value, pattern.substring(1));
        }
        if (StringUtils.endsWith(pattern, CoreConstants.WILDCARD)) {
            return StringUtils.startsWith(value, pattern.substring(0, pattern.length() - 1));
        }
        return StringUtils.equals(value, pattern);
    }

    /**
     * Removes the provided pattern fragment from the original string
     * @param value   A nullable string value from which the pattern is to be removed
     * @param pattern A nullable string pattern
     * @return String value
     */
    public static String strip(String value, String pattern) {
        if (StringUtils.startsWith(pattern, CoreConstants.WILDCARD) && StringUtils.endsWith(pattern, CoreConstants.WILDCARD)) {
            return StringUtils.removeAll(value, pattern.substring(1, pattern.length() - 1));
        }
        if (StringUtils.startsWith(pattern, CoreConstants.WILDCARD)) {
            return StringUtils.removeEnd(value, pattern.substring(1));
        }
        if (StringUtils.endsWith(pattern, CoreConstants.WILDCARD)) {
            return StringUtils.removeStart(value, pattern.substring(0, pattern.length() - 1));
        }
        return StringUtils.equals(value, pattern) ? StringUtils.EMPTY : value;
    }
}
