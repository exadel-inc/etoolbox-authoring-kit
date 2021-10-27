package com.exadel.aem.toolkit.api.annotations.injectors;


import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(ChildrenInjector.NAME)
public @interface Children {

    String value();

    FilterStrategy filterStrategy() default FilterStrategy.RESOURCE_TYPE;

    InjectionStrategy injectionStrategy() default InjectionStrategy.DEFAULT;
}
