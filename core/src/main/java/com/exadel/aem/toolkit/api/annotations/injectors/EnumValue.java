package com.exadel.aem.toolkit.api.annotations.injectors;

import com.exadel.aem.toolkit.core.injectors.ValueMapEnumValueInjector;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(ValueMapEnumValueInjector.NAME)
public @interface EnumValue {
    String name() default StringUtils.EMPTY;
    String fieldName() default StringUtils.EMPTY;
}
