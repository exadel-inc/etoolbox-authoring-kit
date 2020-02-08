package com.exadel.aem.toolkit.api.annotations.widgets;

import com.exadel.aem.toolkit.api.annotations.container.Tab;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define specific fields
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fields {

    /**
     * Used to specify subclasses fields that are ignored in the process of TouchUI XML markup rendering
     * @see ClassField
     * @return One or more {@code ClassField} annotations
     */
    ClassField[] ignoreFields() default {};
}
