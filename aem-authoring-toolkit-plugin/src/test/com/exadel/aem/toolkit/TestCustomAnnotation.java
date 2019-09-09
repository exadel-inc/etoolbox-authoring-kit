package com.exadel.aem.toolkit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DialogWidgetAnnotation(source = "testCustomAnnotation")
@SuppressWarnings("unused")
public @interface TestCustomAnnotation {
    String customField() default "Custom annotation's field!";
}