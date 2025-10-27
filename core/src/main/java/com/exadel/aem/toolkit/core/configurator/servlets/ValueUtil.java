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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import com.adobe.granite.ui.components.FormData;

import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigAttribute;
import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigDefinition;

/**
 * Provides utility methods to work with configuration attribute values
 */
class ValueUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private ValueUtil() {
    }

    /**
     * Processes a request to populate its {@code FormData} object with configuration attribute values
     * @param request The {@link SlingHttpServletRequest} instance
     * @param config  The {@link ConfigDefinition} instance that contains configuration attribute values
     */
    public static void processRequest(SlingHttpServletRequest request, ConfigDefinition config) {
        Map<String, Object> form = new HashMap<>();
        FormData existing = FormData.from(request);
        if (existing != null) {
            form.putAll(existing.getValueMap());
        }

        for (ConfigAttribute attribute : CollectionUtils.emptyIfNull(config.getAttributes())) {
            if (attribute.isMultiValue()) {
                Object[] values = toArray(attribute.getValue());
                if (values == null) {
                    values = toArray(attribute.getDefinition().getDefaultValue());
                }
                if (values != null) {
                    form.put("./data/" + attribute.getDefinition().getID(), values);
                }
            } else {
                Object value = toSingleValue(attribute.getValue());
                if (value == null) {
                    value = toSingleValue(attribute.getDefinition().getDefaultValue());
                }
                if (value != null) {
                    form.put("./data/" + attribute.getDefinition().getID(), value);
                }
            }
        }

        ValueMap formValueMap = new ValueMapDecorator(form);
        FormData.push(request, formValueMap, FormData.NameNotFoundMode.IGNORE_FRESHNESS);
    }

    /**
     * Converts a source object into a singular value. If the source is a {@code List} or an array, the first non-empty
     * item is returned; otherwise, the source itself is returned
     * @param source The source object
     * @return The singular value; or null if the source is empty or contains no non-empty items
     */
    private static Object toSingleValue(Object source) {
        if (isEmpty(source)) {
            return null;
        }
        if (source instanceof List<?>) {
            Object result = CollectionUtils.sizeIsEmpty(source) ? null : CollectionUtils.get(source, 0);
            return isEmpty(result) ? null : result;
        }
        if (source.getClass().isArray()) {
            Object result = Array.getLength(source) == 0 ? null : Array.get(source, 0);
            return isEmpty(result) ? null : result;
        }
        return source;
    }

    /**
     * Converts a source object into an array of objects. If the source is a {@code List} or an array, all non-empty
     * items are returned; otherwise, an array containing the source itself is returned
     * @param source The source object
     * @return The array of objects; or null if the source is empty or contains no non-empty items
     */
    private static Object[] toArray(Object source) {
        if (isEmpty(source)) {
            return null;
        }
        if (source instanceof List<?>) {
            Object[] result = CollectionUtils.sizeIsEmpty(source)
                ? null
                : ((List<?>) source).stream().filter(item -> !isEmpty(item)).toArray(Object[]::new);
            return ArrayUtils.isEmpty(result) ? null : result;
        }
        if (source.getClass().isArray()) {
            Object[] result = IntStream.range(0, Array.getLength(source))
                .mapToObj(index -> Array.get(source, index))
                .filter(item -> !isEmpty(item))
                .toArray(Object[]::new);
            return ArrayUtils.isEmpty(result) ? null : result;
        }
        return new Object[] {source};
    }

    /**
     * Determines whether the source object is empty. If the source is a {@code String}, it is considered empty if it is
     * null or blank; for other object types, null is considered empty
     * @param source The source object
     * @return True if the source is empty; false otherwise
     */
    private static boolean isEmpty(Object source) {
        return source instanceof String ? StringUtils.isEmpty((String) source) : source == null;
    }
}
