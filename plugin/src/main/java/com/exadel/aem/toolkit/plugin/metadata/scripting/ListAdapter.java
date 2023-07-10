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
import java.util.List;
import java.util.function.BiPredicate;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

class ListAdapter<T> extends AbstractAdapter {

    private static final String METHOD_INCLUDES = "includes";

    private final List<T> items = new ArrayList<>();
    private final BiPredicate<T, Object> matcher;

    ListAdapter(List<T> items) {
        this(items, null);
    }

    ListAdapter(List<T> items, BiPredicate<T, Object> matcher) {
        this.items.addAll(items);
        this.matcher = matcher;
    }

    @Override
    public String getClassName() {
        return ListAdapter.class.getSimpleName();
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (METHOD_INCLUDES.equals(name)) {
            return (Callable) this::includes;
        }
        return super.get(name, start);
    }

    @Override
    public Object get(int index, Scriptable start) {
        return index >= 0 && index < items.size() ? items.get(index) : Undefined.SCRIPTABLE_UNDEFINED;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void put(int index, Scriptable start, Object value) {
        if (index >= 0 && index < items.size()) {
            items.set(index, (T) value);
        }
    }

    @SuppressWarnings("unchecked")
    public Object includes(Context context, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (args == null || args.length < 1) {
            return false;
        }
        if (matcher == null) {
            return items.contains((T) args[0]);
        }
        return items.stream().anyMatch(item -> matcher.test(item, args[0]));
    }
}
