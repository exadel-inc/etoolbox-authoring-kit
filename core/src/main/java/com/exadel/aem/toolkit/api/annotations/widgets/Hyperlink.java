/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.LinkCheckerVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/hyperlink/index.html">
 * Hyperlink</a> component in Granite UI. It represents an HTML hyperlink ({@code <a>}) in the user interface
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.HYPERLINK)
@AnnotationRendering(properties = "all")
public @interface Hyperlink {

    /**
     * Maps to the {@code href} attribute of this Granite UI component's node
     * @return String value
     */
    String href();

    /**
     * Maps to the body text of the element of this Granite UI component's node
     * @return String value
     */
    String text();

    /**
     * Effectively maps to the {@code href} attribute of this Granite UI component's node.
     * This is commonly used to produce different value based on locale
     * @return String value
     * @see Hyperlink#href()
     */
    String hrefI18n() default "";

    /**
     * Maps to the {@code rel} attribute of this Granite UI component's node
     * @return String value
     */
    String rel() default "";

    /**
     * Maps to the {@code target} attribute of this Granite UI component's node
     * @return String value
     */
    String target() default "";

    /**
     * When set to true, visually hides the text. It is recommended that every button has a text for a11y purpose.
     * This property is used to make it not visible on the screen while being still available for a11y
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean hideText() default false;

    /**
     * Maps to the {@code x-cq-linkchecker} attribute of this Granite UI component's node
     * @return One of {@link LinkCheckerVariant} values
     * @see LinkCheckerVariant
     */
    @PropertyRendering(
        name = "x-cq-linkchecker",
        ignoreValues = "none",
        transform = StringTransformation.LOWERCASE
    )
    LinkCheckerVariant linkChecker() default LinkCheckerVariant.NONE;

    /**
     * Maps to the icon name of this Granite UI component's node
     * @return String value
     */
    String icon() default "";

    /**
     * Maps to the size of the icon of this Granite UI component's node
     * @return String value, non-blank
     * @see Size
     */
    Size iconSize() default Size.SMALL;
}
