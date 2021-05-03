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
import com.exadel.aem.toolkit.api.annotations.widgets.common.ElementVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.common.LinkCheckerVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/anchorbutton/index.html">
 * AnchorButton</a> component in Granite UI.
 * AnchorButton represents a standard HTML hyperlink ({@code <a>}) that is styled like a button
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.ANCHOR_BUTTON)
@AnnotationRendering(properties = "all")
public @interface AnchorButton {

    /**
     * Maps to the {@code href} attribute of this Granite UI component's node
     * @return String value
     */
    String href();

    /**
     * Maps to the {@code href} attribute of this Granite UI component's node.
     * This is usually used to produce different value based on locale
     * @return String value
     */
    String hrefI18n() default "";

    /**
     * Maps to the body text of the element of this Granite UI component's node
     * @return String value
     */
    String text();

    /**
     * Maps to the target attribute of this Granite UI component's node
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

    /**
     * Maps to the size of the button of this Granite UI component's node.
     * <p><u>Note:</u> only {@code "medium"} and {@code "large"} values are officially supported</p>
     * @return One of {@code Size} values
     * @see Size
     */
    Size size() default Size.MEDIUM;

    /**
     * Maps to the {@code block} attribute of this Granite UI component's node.
     * Used to ensure the button is rendered as a block element
     * @return True or false
     */
    boolean block() default false;

    /**
     * Maps to the {@code variant} attribute of this Granite UI component's node
     * @return One of {@code ElementVariant} values
     * @see ElementVariant
     */
    ElementVariant variant() default ElementVariant.PRIMARY;

    /**
     * When set to a non-blank string, maps to the {@code command} attribute of this Granite UI component's node.
     * Used to define keyboard shortcut for the action. Overrides {@code actionConfigName} value
     * @return String value, non-blank
     */
    String command() default "";

    /**
     * When set to a non-blank string, maps to the {@code actionConfigName} attribute of this Granite UI component's node.
     * Used to define standard definitions of command, icon and text
     * @return String value, non-blank
     */
    String actionConfigName() default "";
}
