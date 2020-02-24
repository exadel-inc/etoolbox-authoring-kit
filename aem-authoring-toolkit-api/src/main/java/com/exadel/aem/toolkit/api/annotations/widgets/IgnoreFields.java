package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify fields that are ignored while rendering XML markup for the current dialog. Typically used
 * for the case when current dialog class extends another class  exposing one or more {@code DialogField}s that are
 * not needed
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreFields {
    /**
     * For the child classes, enumerates the fields to be skipped from rendering XML for the current dialog.
     * Each field is specified by a reference to a {@code Class} and a file name
     * @see ClassField
     * @return One or more {@code ClassField} annotations
     */
    ClassField[] value();
}
