package com.exadel.aem.toolkit.api.annotations.widgets.autocompletecoral3;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.AUTOCOMPLETE_CORAL_3)
@AnnotationRendering(properties = "all")
public @interface Autocomplete {

    AutocompleteOption[] options() default {};

    OptionProvider optionProvider() default @OptionProvider;

    String placeholder() default StringUtils.EMPTY;

    boolean matchStartsWith() default false;

    String icon() default StringUtils.EMPTY;

    boolean disabled() default false;

    boolean invalid() default false;

    boolean loading() default false;

    boolean multiple() default false;
}
