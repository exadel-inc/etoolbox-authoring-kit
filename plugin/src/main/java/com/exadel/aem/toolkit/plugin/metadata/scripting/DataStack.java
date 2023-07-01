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

import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;

public class DataStack {

    private final Map<String, Object> data;

    public DataStack() {
        data = new HashMap<>();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void append(Data[] entries) {
        if (ArrayUtils.isEmpty(entries)) {
            return;
        }
        Arrays.stream(entries).forEach(data -> this.data.put(data.name(), convert(data.value())));
    }

    private static Object convert(String value) {
        return StringUtil.isCollection(value)
            ? new ListAdapter<>(StringUtil.parseCollection(value))
            : value;
    }
}
