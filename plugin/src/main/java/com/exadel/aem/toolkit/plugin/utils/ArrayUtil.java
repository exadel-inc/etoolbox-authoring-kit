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

import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.plexus.util.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for managing array values and collections
 */
public class ArrayUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private ArrayUtil() {
    }

    /**
     * Flattens the provided string array according to the rule of processing array-typed annotation properties. If the
     * argument is a multi-value array, the method returns it as is. However, if it contains the single comma-separated
     * string, the latter is considered an inline multi-value entity and is split by comma
     * @param value Nullable array of strings
     * @return Non-null flattened string array
     */
    public static String[] flatten(String[] value) {
        if (ArrayUtils.isEmpty(value)) {
            return new String[0];
        }
        if (value.length > 1 || !StringUtils.contains(value[0], CoreConstants.SEPARATOR_COMMA)) {
            return value;
        }
        return Pattern.compile(CoreConstants.SEPARATOR_COMMA)
            .splitAsStream(value[0])
            .map(String::trim)
            .toArray(String[]::new);
    }
}
