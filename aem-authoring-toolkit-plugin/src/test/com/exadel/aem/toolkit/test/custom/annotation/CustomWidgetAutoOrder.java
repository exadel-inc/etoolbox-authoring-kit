package com.exadel.aem.toolkit.test.custom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;

@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DialogWidgetAnnotation
@ResourceType("test")
@SuppressWarnings("unused")
public @interface CustomWidgetAutoOrder {
}
