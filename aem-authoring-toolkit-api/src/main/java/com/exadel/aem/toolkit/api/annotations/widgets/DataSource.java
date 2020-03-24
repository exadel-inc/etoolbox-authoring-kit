package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;

/**
 * Used to set up {@link Select#datasource()} or {@link RadioGroup#datasource()} property
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping
public @interface DataSource {
    /**
     * When set to a non-blank string, allows to override {@code sling:resourceType} attribute of a {@code datasource node}
     * @return String value
     */
    String resourceType();
    /**
     * When set to a non-blank string, a {@code datasource} node is appended to the JCR buildup of this component
     * @return Valid JCR path, or an empty string
     */
    String path() default "";
    /**
     * Used to specify collection of {@link Property}s within this DataSource
     * @return Single {@code @Property} annotation, or an array of Properties
     */
    Property[] properties() default {};
}
