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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.ElementSizeConstants;
import com.exadel.aem.toolkit.api.annotations.widgets.common.ElementVariantConstants;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/button/index.html">
 * Button element</a> in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
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
    @PropertyRendering(transform = StringTransformation.CAMELCASE)
    ButtonType type() default ButtonType.BUTTON;

    /**
     * When set to true, maps to the 'disabled' attribute of this TouchUI dialog component's node.
     * Defines the the current option is shown in disabled state
     * @return True or false
     */
    boolean disabled() default false;

    /**
     * When set to a non-blank string, maps to the 'autocomplete' attribute of this TouchUI dialog component's node
     * @return String value
     */
    String autocomplete() default "off";

    /**
     * When set to a non-blank string, maps to the 'formId' attribute of this TouchUI dialog component's node.
     * Used to identify the {@code form} this button is bound to
     * @return String value
     */
    String formId() default "";

    /**
     * When set to a non-blank string, maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define the text of the button
     * @return String value, non-blank
     */
    String text() default "";

    /**
     *  When set to a non-blank string, maps to the 'text_commentI18n' attribute of this TouchUI dialog component's node.
     *  Used to define I18n comment for the body text
     *  @return String value, non-blank
     */
    @PropertyRendering(name = "text_commentI18n")
    String textComment() default "";

    /**
     * Maps to the 'hideText' attribute of this TouchUI dialog component's node.
     * Used to define whether text is hidden
     * @return True or false
     */
    boolean hideText() default false;

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
     * When set to a non-blank string, maps to the 'iconSize' attribute of this TouchUI dialog component's node.
     * Used to specify icon size
     * @see ElementSizeConstants
     * @return String value, non-blank
     */
    String iconSize() default ElementSizeConstants.SMALL;

    /**
     * When set to a non-blank string, maps to the 'size' attribute of this TouchUI dialog component's node.
     * Used to define button size
     * @see ElementSizeConstants
     * @return String value, non-blank
     */
    String size() default ElementSizeConstants.MEDIUM;

    /**
     * Maps to the 'block' attribute of this TouchUI dialog component's node.
     * Used to ensure the button is rendered as a block element
     * @return True or false
     */
    boolean block() default false;

    /**
     * Maps to the 'variant' attribute of this TouchUI dialog component's node.
     * Used to define button variant
     * @see com.exadel.aem.toolkit.api.annotations.widgets.common.ElementVariantConstants
     * @return One of {@code ButtonVariant} values
     */
    String variant() default ElementVariantConstants.PRIMARY;

    /**
     *  When set to a non-blank string, maps to the 'command' attribute of this TouchUI dialog component's node.
     *  Used to define keyboard shortcut for the action. Overrides 'actionConfigName' value
     *  @return String value, non-blank
     */
    String command() default "";

    /**
     *  When set to a non-blank string, maps to the 'actionConfigName' attribute of this TouchUI dialog component's node.
     *  Used to define standard definitions of command, icon and text
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
