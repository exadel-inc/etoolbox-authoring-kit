package com.exadel.aem.toolkit.api.annotations.main;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define decoration tag of AEM component according to the
 * <a href="https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/decoration-tag.html"> Adobe specification</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface HtmlTag {

    /**
     * Maps to the 'class' attribute of the cq:htmlTag node
     * @return String value
     */
    String className();

    /**
     * Maps to the 'cq:tagName' attribute of the cq:htmlTag node
     * @return String value
     */
    String tagName() default "div";
}
