package com.exadel.aem.toolkit.api.annotations.main;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent a value to be rendered as a TouchUI Dialog element attribute
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping
@SuppressWarnings("unused")
public @interface ListItem {
}
