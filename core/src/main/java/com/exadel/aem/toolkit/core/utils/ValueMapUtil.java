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
package com.exadel.aem.toolkit.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for working with value maps as retrieved from JCR resources
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class ValueMapUtil {

    private static final List<String> IGNORABLES = Arrays.asList(
        JcrConstants.JCR_PRIMARYTYPE,
        JcrConstants.JCR_MIXINTYPES,
        "jcr:created",
        "jcr:createdBy",
        "jcr:lastModified",
        "jcr:lastModifiedBy",
        "cq:*",
        "rep:*",
        "sling:resourceType"
    );

    /**
     * Default (instantiation-restricting) constructor
     */
    public ValueMapUtil() {
    }

    /**
     * Filters the provided map of properties excluding the "system" properties not relevant for business logic
     * @param properties {@code Map} instance
     * @return Filtered map
     */
    public static Map<String, Object> excludeSystemProperties(Map<String, Object> properties) {
        return MapUtils.emptyIfNull(properties)
            .entrySet()
            .stream()
            .filter(entry -> !isIgnorable(entry.getKey()) && entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Determines whether the provided key is to be considered "ignorable" (i.e. a system property)
     * @param key Property name
     * @return True or false
     */
    private static boolean isIgnorable(String key) {
        if (StringUtils.isBlank(key)) {
            return true;
        }
        for (String pattern : IGNORABLES) {
            if (pattern.endsWith(CoreConstants.WILDCARD)) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                if (key.startsWith(prefix)) {
                    return true;
                }
            } else if (pattern.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
