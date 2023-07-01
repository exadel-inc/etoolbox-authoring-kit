package com.exadel.aem.toolkit.plugin.metadata.scripting;

import org.mozilla.javascript.ScriptableObject;

abstract class AbstractAdapter extends ScriptableObject {

    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if (String.class.equals(typeHint)) {
            return getClassName();
        }
        return super.getDefaultValue(typeHint);
    }
}
