package com.exadel.aem.toolkit.api.annotations.assets.dependson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define set of {@link DependsOnTab} actions for the current component. For this to work properly,
 * the {@code aem-authoring-toolkit-assets} package must be added to the AEM installation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOnTabConfig {
    DependsOnTab[] value();
}
