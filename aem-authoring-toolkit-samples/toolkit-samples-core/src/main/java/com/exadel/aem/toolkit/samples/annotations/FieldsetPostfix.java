package com.exadel.aem.toolkit.samples.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ValueRestriction(value = ValueRestrictions.NOT_BLANK)
@DialogWidgetAnnotation(source = "postfix")
public @interface FieldsetPostfix {
    String postfix() default "";
}
