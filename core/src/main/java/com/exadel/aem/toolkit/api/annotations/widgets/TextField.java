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
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textfield/index.html">
 * TextField</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TEXTFIELD)
@AnnotationRendering(properties = "!plugins")
public @interface TextField {

    /**
     * When set to a non-blank string, maps to the {@code value} attribute of this Granite UI component's node Used to
     * define the default value for a TextField
     * @return String value
     */
    String value() default "";

    /**
     * When set to a non-blank string, maps to the {@code emptyText} attribute of this Granite UI component's node. Used
     * to define the text hint for an empty TextField
     * @return String value
     */
    String emptyText() default "";

    /**
     * When set to a non-blank string, maps to the {@code autocomplete} attribute of this Granite UI component's node.
     * Indicates if the value can be automatically completed by the browser. See <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes/autocomplete">The HTML autocomplete
     * attribute</a> for the supported values
     * @return String value
     */
    String autocomplete() default "";

    /**
     * Allows specifying that the field should have input focus when the dialog loads. Only one element in a dialog
     * should have the autofocus turned one
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean autofocus() default false;

    /**
     * When set, defines the maximum number of characters (in Unicode code points) that the user can enter
     * @return A numeric value greater than zero
     */
    @PropertyRendering(ignoreValues = "0")
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    long maxLength() default 0;

    /**
     * When set, defines the custom plugins used with this widget, such as {@code writesonic}
     * @return Zero or more string values representing plugin IDs
     */
    String[] plugins() default {};
}
