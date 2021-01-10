package com.exadel.aem.toolkit.api.annotations.assets.dependson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a name-value pair to be rendered as a DependsOn parameter
 *
 * @see DependsOn
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOnParam {
    /**
     * Defines parameter name
     * The resulting attribute's name will be rendered as 'dependsOn-{action}-{name}'
     *
     * @return String value, non-blank
     */
    String name();

    /**
     * Defines parameter value
     *
     * @return String value, non-blank
     */
    String value();
}
