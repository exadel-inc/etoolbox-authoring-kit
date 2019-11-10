package com.exadel.aem.toolkit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping(prefix = "granite:data/cq:")
@SuppressWarnings("unused")
public @interface CustomAnnotationAutomapping {
    @PropertyName("custom")
    String customField() default "Custom annotation's field!";
}