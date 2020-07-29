package com.exadel.aem.toolkit.api.annotations.assets.dependson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define {@code dependsOnRef} and {@code dependsOnRefType} attributes of {@code granite:data} child node of
 * the current widget node to engage DependsOn frontend routines. For this to work properly, the {@code aem-authoring-toolkit-assets}
 * package must be added to the AEM installation
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface DependsOnRef {
    /**
     * Defines the 'dependsOnRef' attribute
     * If not set, 'name' will be equal to the annotated field name
     * @return String value representing tag that would be specified in foreign {@link DependsOn} annotations, non-null
     */
    String name() default "";

    /**
     * Defines the 'dependsOnRefType' attribute
     * @return One of the predefined {@link DependsOnRefTypes} constants
     */
    DependsOnRefTypes type() default DependsOnRefTypes.AUTO;

    /**
     * Defines the 'dependsOnRefLazy' attribute
     * @return marker to make DependsOn reference lazy.
     */
    boolean lazy() default false;
}
