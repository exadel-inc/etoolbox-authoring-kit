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
package com.exadel.aem.toolkit.core.assistant.services.openai;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

class ArgumentsVersion {

    private final Map<String, Object> original;
    private ValueMap modified;

    public ArgumentsVersion(Map<String, Object> original) {
        this.original = original;
    }

    public ArgumentsVersion put(String key, Object value) {
        if (modified == null) {
            modified = newValueMap(original);
        }
        modified.put(key, value);
        return this;
    }

    public ArgumentsVersion putIfMissing(String key, Object value) {
        if (modified != null) {
            Object existingValue = modified.get(key);
            if (existingValue == null || (StringUtils.isBlank(existingValue.toString()))) {
                modified.put(key, value);
            }
        } else if (original.get(key) == null || StringUtils.isBlank(original.get(key).toString())) {
            modified = newValueMap(original);
            modified.put(key, value);
        }
        return this;
    }

    public ValueMap get() {
        if (modified != null) {
            return modified;
        }
        if (original instanceof ValueMap) {
            return (ValueMap) original;
        }
        return new ValueMapDecorator(original);
    }

    private static ValueMap newValueMap(Map<String, Object> original) {
        Map<String, Object> base = new HashMap<>(original);
        return new ValueMapDecorator(base);
    }
}
