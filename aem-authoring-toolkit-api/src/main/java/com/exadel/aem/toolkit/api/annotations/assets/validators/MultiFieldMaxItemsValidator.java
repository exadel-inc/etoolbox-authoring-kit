package com.exadel.aem.toolkit.api.annotations.assets.validators;


import com.exadel.aem.toolkit.api.annotations.meta.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shortcuts for maxItems multifield validator
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DialogWidgetAnnotation(source = ValidatorConstants.MAX_ITEMS)
@PropertyMapping(prefix = ValidatorConstants.GRANITE_DATA_PREFIX)
public @interface MultiFieldMaxItemsValidator {

    @PropertyName(ValidatorConstants.MAX_ITEMS_VALUE)
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    long value();

    @PropertyName(ValidatorConstants.MAX_ITEMS_MSG)
    @IgnoreValue(ValidatorConstants.MAX_ITEMS_DEFAULT_MSG)
    String message() default ValidatorConstants.MAX_ITEMS_DEFAULT_MSG;
}
