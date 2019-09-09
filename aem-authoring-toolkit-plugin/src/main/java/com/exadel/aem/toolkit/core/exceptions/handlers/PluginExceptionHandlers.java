package com.exadel.aem.toolkit.core.exceptions.handlers;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;

public class PluginExceptionHandlers {
    private static final String ALL_EXCEPTIONS = "all";
    private static final String NONE_EXCEPTIONS = "none";

    private PluginExceptionHandlers() {
    }

    public static ExceptionHandler getHandler(String value) {
        if (StringUtils.isBlank(value) || NONE_EXCEPTIONS.equalsIgnoreCase(value)) {
            return new PermissiveExceptionHandler();
        }
        if (ALL_EXCEPTIONS.equalsIgnoreCase(value)) {
            return new StrictExceptionHandler();
        }
        return new SelectiveExceptionHandler(Arrays.stream(StringUtils.split(value, ','))
                .map(String::trim).collect(Collectors.toList()));
    }
}
