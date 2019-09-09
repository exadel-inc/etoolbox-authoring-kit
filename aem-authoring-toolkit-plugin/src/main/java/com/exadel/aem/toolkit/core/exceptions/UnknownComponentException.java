package com.exadel.aem.toolkit.core.exceptions;

import java.nio.file.Path;

/**
 * Represents the plugin-specific exception thrown when a processable (not out-filtered) Java component
 * doesn't meet a component folder
 * in the package
 */
public class UnknownComponentException extends RuntimeException {
    public UnknownComponentException(Path path) {
        super(String.format("Component at %s not present in the package", path));
    }
}
