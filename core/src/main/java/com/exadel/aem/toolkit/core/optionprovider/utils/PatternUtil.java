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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

public class PatternUtil {

    private PatternUtil() {
    }

    public static boolean isPattern(String value) {
        return StringUtils.startsWith(value, CoreConstants.WILDCARD) || StringUtils.endsWith(value, CoreConstants.WILDCARD);
    }

    public static boolean isMatch(String value, String[] patterns) {
        if (patterns == null) {
            return false;
        }
        return Arrays.stream(patterns).anyMatch(pattern -> isMatch(value, pattern));
    }

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
