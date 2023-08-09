package com.exadel.aem.toolkit.core.injectors.utils;

public class CastResult {

    private final Object value;
    private final boolean isFallback;

    CastResult(Object value, boolean isFallback) {
        this.value = value;
        this.isFallback = isFallback;
    }

    public Object getValue() {
        return value;
    }

    public boolean isFallback() {
        return isFallback;
    }
}
