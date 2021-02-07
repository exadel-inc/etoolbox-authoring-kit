package com.exadel.aem.toolkit.api.annotations.assets.dependson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define {@code dependsOn} and {@code dependsOnActon} attributes of {@code granite:data} child node of the current
 * tab node to engage DependsOn frontend routines. For this to work properly, the {@code aem-authoring-toolkit-assets}
 * package must be added to the AEM installation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DependsOnTabConfig.class)
@SuppressWarnings("unused")
public @interface DependsOnTab {
    /**
     * Defines the 'dependsOn' attribute
     * @return String representing value the current tab depends on, non-null
     */
    String query();

    /**
     * Specifies the tab by its title
     * @return String value, non-null
     */
    String tabTitle();
}
