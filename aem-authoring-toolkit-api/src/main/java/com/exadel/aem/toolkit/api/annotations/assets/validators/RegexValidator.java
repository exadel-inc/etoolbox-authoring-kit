package com.exadel.aem.toolkit.api.annotations.assets.validators;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.IgnoreValue;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shortcuts for regex validator
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DialogWidgetAnnotation(source = ValidatorConstants.REGEX)
@PropertyMapping(prefix = ValidatorConstants.GRANITE_DATA_PREFIX)
public @interface RegexValidator {

    @PropertyName(ValidatorConstants.REGEX_VALUE)
    String value();

    @PropertyName(ValidatorConstants.REGEX_MSG)
    @IgnoreValue(ValidatorConstants.REGEX_DEFAULT_MSG)
    String message() default ValidatorConstants.REGEX_DEFAULT_MSG;
}