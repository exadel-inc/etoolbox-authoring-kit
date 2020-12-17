package com.exadel.aem.toolkit.api.annotations.widgets.hyperlink;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.EnumValue;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-3/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/hyperlink/index.html">
 * Hyperlink is a component to represent a HTML hyperlink (<a>) in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.HYPERLINK)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Hyperlink {

    /**
     * Maps to the href attribute of this TouchUI dialog component's node
     * @return String value
     */
    String href();

    /**
     * Maps to the body text of the element of this TouchUI dialog component's node
     * @return String value
     */
    String text();

    /**
     * Maps to the href attribute of this TouchUI dialog component's node.
     * This is usually used to produce different value based on locale
     * @return String value
     */
    String hrefI18n() default "";

    /**
     * Maps to the rel attribute of this TouchUI dialog component's node
     * @return String value
     */
    String rel() default "";

    /**
     * Maps to the target attribute of this TouchUI dialog component's node
     * @return String value
     */
    String target() default "";

    /**
     * Visually hide the text. It is RECOMMENDED that every button has a text for a11y purpose.
     * Use this property to hide it visually, while still making it available for a11y
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean hideText() default false;

    /**
     * Maps to the x-cq-linkchecker attribute of this TouchUI dialog component's node
     * @return One of {@code HyperlinkLinkchecker} values
     * @see HyperlinkLinkchecker
     */
    @PropertyRendering(ignoreValues = "NONE")
    HyperlinkLinkchecker xCqLinkchecker() default HyperlinkLinkchecker.NONE;

    /**
     * Maps to the icon name. e.g. “search” of this TouchUI dialog component's node
     * @return String value
     */
    String icon() default "";

    /**
     * Maps to the size of the icon of this TouchUI dialog component's node
     * @return One of {@code HyperlinkIconSize} values
     * @see HyperlinkIconSize
     */
    @EnumValue
    HyperlinkIconSize iconSize() default HyperlinkIconSize.S;
}
