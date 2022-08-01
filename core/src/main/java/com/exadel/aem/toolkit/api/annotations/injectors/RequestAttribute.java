package com.exadel.aem.toolkit.api.annotations.injectors;

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.core.injectors.RequestAttributeInjector;

/**
 * Used on either a field, a method, or a method parameter of a Sling model to inject a request attribute.
 * <p>Supported types: {@code Object}, {@code class extends Object}
 *                      List {@code List} of:
 *                           {@code class extends Object}, {@code String} ,{@code Integer},
 *                           {@code Boolean}, {@code Long}, {@code Calendar}
 *                      Array {@code Array} of:
 *                           {@code class extends Object}, {@code String} ,boxed/unboxed {@code Integer},
 *                           boxed/unboxed {@code Boolean}, boxed/unboxed {@code Long}, {@code Calendar}
 *                          </p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(RequestAttributeInjector.NAME)
public @interface RequestAttribute {
    /**
     * Used to specify the name of the attribute to inject if it differs from the name of the underlying Java class
     * member
     * @return Optional non-blank string
     */
    String name() default "";
}
