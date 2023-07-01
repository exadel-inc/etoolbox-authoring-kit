package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.util.Map;

import org.mozilla.javascript.Scriptable;

class MapAdapter extends AbstractAdapter {

    private final Map<String, Object> data;

    public MapAdapter(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (name != null && data.containsKey(name)) {
            return data.get(name);
        }
        return super.get(name, start);
    }
}
