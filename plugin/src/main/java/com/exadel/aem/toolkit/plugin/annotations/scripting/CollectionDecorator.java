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
package com.exadel.aem.toolkit.plugin.annotations.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import jdk.nashorn.api.scripting.AbstractJSObject;

class CollectionDecorator extends AbstractJSObject {

    private static final String METHOD_INCLUDES = "includes";

    private final List<Object> items = new ArrayList<>();

    public CollectionDecorator(List<?> items) {
        this.items.addAll(items);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean hasMember(String name) {
        return this.items.contains(name);
    }

    @Override
    public Object getMember(String name) {
        if (METHOD_INCLUDES.equals(name)) {
            return (Predicate<Object>) obj -> hasMember(String.valueOf(obj));
        }
        return super.getMember(name);
    }

    @Override
    public Object getSlot(int index) {
        return index >= 0 && index < items.size() ? items.get(0) : null;
    }

    @Override
    public boolean hasSlot(int slot) {
        return slot >= 0 && slot < items.size();
    }

    @Override
    public void setSlot(int index, Object value) {
        if (index >= 0 && index < items.size()) {
            items.set(index, value);
        }
    }

    @Override
    public Collection<Object> values() {
        return items;
    }
}
