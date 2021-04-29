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
package com.exadel.aem.toolkit.api.annotations.widgets.button;

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
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/button/index.html">
 * Button</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.BUTTON)
@AnnotationRendering(properties = "all")
public @interface Button {

    /**
     * Maps to the {@code type} attribute of this Granite UI component's node.
     * Used to define button type
     * @return One of {@code ButtonType} values
     * @see ButtonType
     */
    @PropertyRendering(transform = StringTransformation.CAMELCASE)
    ButtonType type() default ButtonType.BUTTON;

    /**
     * When set to true, maps to the {@code disabled} attribute of this Granite UI component's node.
     * Defines that the current option is shown in the disabled state
     * @return True or false
     */
    boolean disabled() default false;

    /**
     * When set to a non-blank string, maps to the {@code autocomplete} attribute of this Granite UI component's node
     * @return String value
     */
    String autocomplete() default "off";

    /**
     * When set to a non-blank string, maps to the {@code formId} attribute of this Granite UI component's node.
     * Used to identify the {@code form} this button is bound to
     * @return String value
     */
    String formId() default "";

    /**
     * When set to a non-blank string, maps to the {@code text} attribute of this Granite UI component's node.
     * Used to define the text of the button
     * @return String value, non-blank
     */
    String text() default "";

    /**
     *  When set to a non-blank string, maps to the {@code text_commentI18n} attribute of this Granite UI component's node.
     *  Used to define the I18n comment for the body text
     *  @return String value, non-blank
     */
    @PropertyRendering(name = "text_commentI18n")
    String textComment() default "";

    /**
     * Maps to the {@code hideText} attribute of this Granite UI component's node.
     * Used to define whether text is hidden
     * @return True or false
     */
    boolean hideText() default false;

    /**
     * Maps to the {@code active} attribute of this Granite UI component's node.
     * Used to define the initial state of the button
     * @return True or false
     */
    boolean active() default false;

    /**
     * Maps to the {@code icon} attribute of this Granite UI component's node.
     * Used to define the icon of the component. When set, overrides {@code actionConfigName}
     * @return String value
     */
    String icon() default "";

    /**
     * When set to a non-blank string, maps to the {@code iconSize} attribute of this Granite UI component's node.
     * Used to specify icon size
     * @return One of {@code Size} values
     * @see Size
     */
    Size iconSize() default Size.SMALL;

    /**
     * When set to a non-blank string, maps to the {@code size} attribute of this Granite UI component's node.
     * Used to define button size
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
     * Maps to the {@code variant} attribute of this Granite UI dialog component's node.
     * Used to define button variant
     * @return One of {@code ElementVariant} values
     * @see ElementVariant
     */
    ElementVariant variant() default ElementVariant.PRIMARY;

    /**
     *  When set to a non-blank string, maps to the {@code command} attribute of this Granite UI dialog component's node.
     *  Used to define keyboard shortcut for the action. Overrides {@code actionConfigName} value
     *  @return String value, non-blank
     */
    String command() default "";

    /**
     *  When set to a non-blank string, maps to the {@code actionConfigName} attribute of this Granite UI dialog component's node.
     *  Used to define standard definitions of command, icon, and text
     *  @return String value, non-blank
     */
    String actionConfigName() default "";

    /**
     *  When set to a non-blank string, maps to the {@code trackingFeature} attribute of this Granite UI dialog component's node.
     *  Used to define the name of the feature that the interaction takes place
     *  @return String value, non-blank
     *  @see <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">foundation-tracking</a>
     */
    String trackingFeature() default "";

    /**
     *  When set to a non-blank string, maps to the {@code trackingElement} attribute of this Granite UI component's node.
     *  Used to determine the element this component represents for the purpose of tracking
     *  @return String value, non-blank
     *  @see <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">foundation-tracking</a>
     */
    String trackingElement() default "";
}
