package com.exadel.aem.toolkit.api.annotations.assets.dependson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define {@code dependsOn},  {@code dependsOnActon} attributes and {@code dependsOn} parameters of {@code granite:data} child node of the current
 * widget node to engage DependsOn frontend routines. For this to work properly, the {@code aem-authoring-toolkit-assets}
 * package must be added to the AEM installation
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DependsOnConfig.class)
@SuppressWarnings("unused")
public @interface DependsOn {
    /**
     * Defines the 'dependsOn' attribute
     * @return String representing value the current widget is depending on, non-null
     */
    String query();

    /**
     * Defines the 'dependsOnAction' attribute
     * @return String value, one of {@link DependsOnActions} items or a custom-defined action name
     * @see DependsOnActions
     */
    String action() default DependsOnActions.VISIBILITY;

    /**
     * Defines custom static parameters to be used by dependsOn actions
     * @return Single {@code DependsOnParam} annotation, or an array of DependsOnParams
     */
    DependsOnParam[] params() default {};
}
