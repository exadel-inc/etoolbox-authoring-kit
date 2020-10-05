package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;

/**
 * Allows to define a {@code datasource} node for a {@link @Select or a @RadioGroup} annotation
 * Use this to specify either an ACS Commons list source or a custom datasource for your selection or radioGroup
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping
public @interface DataSource {
    /**
     * When set to a non-blank string, stores as the {@code sling:resourceType} attribute of a {@code datasource node}
     * of the current widget
     * @return String value
     */
    String resourceType() default "";
    /**
     * When set to a non-blank string, stores as the {@code path} attribute of a {@code datasource node}
     * of the current widget
     * @return Valid JCR path, or an empty string
     */
    String path() default "";
    /**
     * Used to specify collection of {@link Property}s within this DataSource
     * @return Single {@code @Property} annotation, or an array of Properties
     */
    Property[] properties() default {};
}
