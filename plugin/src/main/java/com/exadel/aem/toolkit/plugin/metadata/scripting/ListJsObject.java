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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import jdk.nashorn.api.scripting.AbstractJSObject;

class ListJsObject<T> extends AbstractJSObject {

    private static final String METHOD_INCLUDES = "includes";

    private final List<T> items = new ArrayList<>();
    private final BiPredicate<T, String> matcher;

    public ListJsObject(List<T> items) {
        this(items, null);
    }

    public ListJsObject(List<T> items, BiPredicate<T, String> matcher) {
        this.items.addAll(items);
        this.matcher = matcher;
    }

    @Override
    public Object call(Object loopback, Object... args) {
        return this;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean hasMember(String name) {
        if (matcher == null) {
            return items.stream().anyMatch(item -> String.valueOf(item).equals(name));
        }
        return items.stream().anyMatch(item -> matcher.test(item, name));
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
    @SuppressWarnings("unchecked")
    public void setSlot(int index, Object value) {
        if (index >= 0 && index < items.size()) {
            items.set(index, (T) value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> values() {
        return (Collection<Object>) items;
    }
}
