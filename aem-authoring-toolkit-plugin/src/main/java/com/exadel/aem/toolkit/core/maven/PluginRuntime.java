package com.exadel.aem.toolkit.core.maven;

import java.util.List;

public class PluginRuntime {
    private static final ThreadLocal<PluginRuntimeContext> INSTANCE = ThreadLocal.withInitial(EmptyRuntimeContext::new);

    private PluginRuntime() {
    }

    public static PluginRuntimeContext context() {
        return INSTANCE.get();
    }

    static void initialize(List<String> classPathElements, String packageBase, String criticalExceptions) {
        INSTANCE.set(new LoadedRuntimeContext(classPathElements, packageBase, criticalExceptions));
    }

    static void close() {
        INSTANCE.remove();
    }
}