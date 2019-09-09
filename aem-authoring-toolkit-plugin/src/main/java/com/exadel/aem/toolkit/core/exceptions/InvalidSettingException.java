package com.exadel.aem.toolkit.core.exceptions;

/**
 * Represents the plugin-specific exception produced for a generic invalid setting of AEM Authoring Toolkit,
 * such as a reference to an unreachable component class, or an invalid resource type
 */
public class InvalidSettingException extends RuntimeException {
    public InvalidSettingException(String message) {
        super(message);
    }
    public InvalidSettingException(String message, Exception inner) {
        super(message, inner);
    }
}
