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
