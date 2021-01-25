package com.exadel.aem.toolkit.api.annotations.lists;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent a value to be rendered as a TouchUI Dialog property
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping
@SuppressWarnings("unused")
public @interface ListItem {
}
