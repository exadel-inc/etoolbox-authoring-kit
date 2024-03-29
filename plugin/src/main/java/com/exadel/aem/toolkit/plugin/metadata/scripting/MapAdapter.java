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

import java.util.Map;

import org.mozilla.javascript.Scriptable;

/**
 * Extends {@link AbstractAdapter} to expose {@code Map} objects to the {@code Rhino} engine
 */
class MapAdapter extends AbstractAdapter {

    private final Map<String, Object> data;

    /**
     * Initializes a class instance storing a reference to the {@code Map} that serves as the data source for an inline
     * script
     * @param data {@code Map} instance
     */
    MapAdapter(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String name, Scriptable start) {
        if (name != null && data.containsKey(name)) {
            return data.get(name);
        }
        return super.get(name, start);
    }
}
