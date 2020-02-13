package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to refer a particular field of a class for a specific processing while building XML markup
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassField {

    /**
     * The Java class possessing the field
     * @return {@code Class<?>} instance
     */
    Class<?> value();

    /**
     * The name of the field
     * @return String value, non-empty
     */
    String field();
}