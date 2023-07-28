package com.exadel.aem.toolkit.core.injectors.utils;

public class CastResult {

    private Object value;
    private boolean isFallback;

    public CastResult(Object value, boolean isFallback) {
        this.value = value;
        this.isFallback = isFallback;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isFallback() {
        return isFallback;
    }

    public void setFallback(boolean fallback) {
        isFallback = fallback;
    }
}
