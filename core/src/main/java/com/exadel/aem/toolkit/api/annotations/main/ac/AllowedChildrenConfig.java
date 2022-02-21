package com.exadel.aem.toolkit.api.annotations.main.ac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a collection of rules to specify own list of allowed components for parsys-like components
 * @see AllowedChildren
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedChildrenConfig {

    /**
     * Array of {@link AllowedChildren} objects
     * @return One or more {@code AllowedChildren} annotations
     */
    AllowedChildren[] value();
}
