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
package com.exadel.aem.toolkit.core.assistant.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.wrappers.ValueMapDecorator;

public class VersionableValueMap extends ValueMapDecorator {

    /**
     * Creates a {@link VersionableValueMap} instance based on an empty map
     */
    public VersionableValueMap() {
        this(new HashMap<>());
    }

    /**
     * Creates a {@link VersionableValueMap} instance based on the provided map
     * @param base A no-null {@code Map} instance
     */
    public VersionableValueMap(Map<String, Object> base) {
        super(base);
    }

    @Nonnull
    @Override
    public VersionableValueMap put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    @Nonnull
    public VersionableValueMap putIfMissing(String key, Object value) {
        Object existingValue = get(key);
        if (existingValue == null || (existingValue instanceof String && StringUtils.isBlank(existingValue.toString()))) {
            super.put(key, value);
        }
        return this;
    }

    @Nonnull
    public VersionableValueMap newVersion() {
        return new VersionableValueMap(this);
    }
}
