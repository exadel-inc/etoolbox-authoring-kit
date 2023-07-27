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
package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.main.Setting;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;

/**
 * Accumulates settings that can modify the way Granite UI components are rendered by the ToolKit via inline scripting
 * templates
 */
public class DataStack {

    private final Map<String, Object> data;

    /**
     * Initializes an empty {@link DataStack} object
     */
    public DataStack() {
        data = new HashMap<>();
    }

    /**
     * Retrieves the accumulated data as a key-value map
     * @return {@code Map} instance
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Appends the given list of settings to the map
     * @param entries An array of {@link Setting} objects to be added
     */
    public void append(Setting[] entries) {
        if (ArrayUtils.isEmpty(entries)) {
            return;
        }
        Arrays.stream(entries).forEach(entry -> this.data.put(entry.name(), convert(entry.value())));
    }

    /**
     * Converts the value of a setting object into the appropriate type. Parses boolean-line and list-like string values
     * @param value Raw setting value
     * @return Converted value
     */
    private static Object convert(String value) {
        if (Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value)) {
            return Boolean.parseBoolean(value);
        }
        return StringUtil.isCollection(value)
            ? new ListAdapter<>(StringUtil.parseCollection(value))
            : value;
    }
}
