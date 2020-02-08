package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define certain class Field
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassField {

    /**
     * Class field from
     * @return {@code Class<?>} object
     */
    Class<?> value();

    /**
     * Field name
     * @return String field name
     */
    String field();
}