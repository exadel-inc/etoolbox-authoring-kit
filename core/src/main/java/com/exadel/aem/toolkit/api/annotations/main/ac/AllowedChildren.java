package com.exadel.aem.toolkit.api.annotations.main.ac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllowedChildrenConfig.class)
public @interface AllowedChildren {

    String[] value();

    String[] templates() default {};

    String[] pageResourceTypes() default {};

    String[] parentsResourceTypes() default {};

    String[] pagePaths() default {};

    String[] resourceNames() default {};

    boolean applyToCurrent() default false;
}
