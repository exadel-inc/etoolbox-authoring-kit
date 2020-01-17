package com.exadel.aem.toolkit.api.annotations.assets.validators;

import com.exadel.aem.toolkit.api.annotations.meta.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shortcuts for minItems multifield validator
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DialogWidgetAnnotation(source = ValidatorConstants.MIN_ITEMS)
@PropertyMapping(prefix = ValidatorConstants.GRANITE_DATA_PREFIX)
public @interface MultiFieldMinItemsValidator {

    @PropertyName(ValidatorConstants.MIN_ITEMS_VALUE)
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    long value();

    @PropertyName(ValidatorConstants.MIN_ITEMS_MSG)
    @IgnoreValue(ValidatorConstants.MIN_ITEMS_DEFAULT_MSG)
    String message() default ValidatorConstants.MIN_ITEMS_DEFAULT_MSG;
}