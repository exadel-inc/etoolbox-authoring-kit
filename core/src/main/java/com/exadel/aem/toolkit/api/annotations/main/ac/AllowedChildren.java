package com.exadel.aem.toolkit.api.annotations.main.ac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a set of allowed components and rules for your list of insertable components.
 * @see AllowedChildrenConfig
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllowedChildrenConfig.class)
public @interface AllowedChildren {

    /**
     * Represents the result of allowed components that will be insertable if current rule is applicable.
     * Note: you can specify groups -- 'group: Your group name'
     * @return One or more {@code String} values, non-blank
     */
    String[] value();

    /**
     * Represents template resource types where current rule is applicable.
     * Note: wildcards is acceptable -- '*part/of/resType*', '*ending/of/resType', 'beginning/of/resType*'
     * @return {@code String} value, or an array of strings
     */
    String[] templates() default {};

    /**
     * Represents page resource types where current rule is applicable.
     * Note: wildcards is acceptable -- '*part/of/resType*', '*ending/of/resType', 'beginning/of/resType*'
     * @return {@code String} value, or an array of strings
     */
    String[] pageResourceTypes() default {};

    /**
     * Represents parent container resource types where current rule is applicable.
     * Note: chain of parents is acceptable -- '*resType1;group: Group name;resType2*'
     * @return {@code String} value, or an array of strings
     */
    String[] parentsResourceTypes() default {};

    /**
     * Represents page paths where current rule is applicable.
     * Note: wildcards is acceptable -- '*part/of/resType*', '*ending/of/resType', 'beginning/of/resType*'
     * @return {@code String} value, or an array of strings
     */
    String[] pagePaths() default {};

    /**
     * Represents resource names where current rule is applicable.
     * Note: wildcards is acceptable -- '*part/of/resType*', '*ending/of/resType', 'beginning/of/resType*'
     * @return {@code String} value, or an array of strings
     */
    String[] resourceNames() default {};

    boolean applyToCurrent() default false;
}
