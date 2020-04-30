/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import com.exadel.aem.toolkit.api.annotations.meta.*;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.ButtonSize;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.IconSize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/button/index.html">
 * Button element</a> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.BUTTON)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Button {

    /**
     * Maps to the 'type' attribute of this TouchUI dialog component's node.
     * Used to define button type
     * @see ButtonType
     * @return One of {@code ButtonType} values
     */
    @PropertyRendering(name = "type")
    @EnumValue(transformation = StringTransformation.CAMELCASE)
    ButtonType buttonType() default ButtonType.BUTTON;

    /**
     * When set to true, maps to the 'disabled' attribute of this TouchUI dialog component's node.
     * Defines the the current option is shown in disabled state
     * @return True or false
     */
    boolean disabled() default false;

    /**
     * When set to a non-blank string, maps to the 'autocomplete' attribute of this TouchUI dialog component's node.
     * Used to indicate if the value can be automatically completed by the browser
     * @return String value
     */
    String autocomplete() default "off";

    /**
     * When set to a non-blank string, maps to the 'formId' attribute of this TouchUI dialog component's node.
     * Used to define formId for the button.
     * @return String value
     */
    String formId() default "";

    /**
     * Maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define the text of the button.
     * When set, override actionConfigName.
     * @return String value
     */
    @PropertyRendering(name = "text")
    String buttonText() default "";

    /**
     *  When set to a non-blank string, maps to the 'text_commentI18n' attribute of this TouchUI dialog component's node.
     *  Used to define I18n comment for the body text.
     *  @return String value, non-blank
     */
    @PropertyRendering(name = "text_commentI18n")
    String textComment() default "";

    /**
     * Maps to the 'hideText' attribute of this TouchUI dialog component's node.
     * Used to define whether text is hidden
     * @return True or false
     */
    @PropertyRendering(name = "hideText")
    boolean hideButtonText() default false;

    /**
     * Maps to the 'active' attribute of this TouchUI dialog component's node.
     * Used to define initial state of the button.
     * @return True or false
     */
    boolean active() default false;

    /**
     * When set to a non-blank string, maps to the 'icon' attribute of this TouchUI dialog component's node.
     * Used to define component's icon.
     * When set, override actionConfigName.
     * @return String value
     */
    String icon() default "";

    /**
     * Maps to the 'iconSize' attribute of this TouchUI dialog component's node.
     * Used to specify icon size
     * @see IconSize
     * @return One of {@code IconSize} values
     */
    @EnumValue()
    IconSize iconSize() default IconSize.SMALL;

    /**
     * Maps to the 'size' attribute of this TouchUI dialog component's node.
     * Used to define button size
     * @see ButtonSize
     * @return One of {@code ButtonSize} values
     */
    @PropertyRendering(name = "size")
    @EnumValue()
    ButtonSize buttonSize() default ButtonSize.MEDIUM;

    /**
     * Maps to the 'block' attribute of this TouchUI dialog component's node.
     * Used to render the button as a block element.
     * @return True or false
     */
    boolean block() default false;

    /**
     * Maps to the 'variant' attribute of this TouchUI dialog component's node.
     * Used to define button variant
     * @see ButtonVariant
     * @return One of {@code ButtonVariant} values
     */
    @PropertyRendering(name = "variant")
    @EnumValue(transformation = StringTransformation.CAMELCASE)
    ButtonVariant buttonVariant() default ButtonVariant.SECONDARY;

    /**
     *  When set to a non-blank string, maps to the 'command' attribute of this TouchUI dialog component's node.
     *  Used to define keyboard shortcut for the action.
     *  When set, override actionConfigName.
     *  @return String value, non-blank
     */
    String command() default "";

    /**
     *  When set to a non-blank string, maps to the 'actionConfigName' attribute of this TouchUI dialog component's node.
     *  Used to define standard definitions of command, icon and text.
     *  @return String value, non-blank
     */
    String actionConfigName() default "";

    /**
     *  When set to a non-blank string, maps to the 'trackingFeature' attribute of this TouchUI dialog component's node.
     *  Used to define name of the feature that the interaction takes place.
     *  @see <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">foundation-tracking</a>
     *  @return String value, non-blank
     */
    String trackingFeature() default "";

    /**
     *  When set to a non-blank string, maps to the 'trackingElement' attribute of this TouchUI dialog component's node.
     *  Used to element this component represents for the purpose of tracking.
     *  @see <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">foundation-tracking</a>
     *  @return String value, non-blank
     */
    String trackingElement() default "";
}