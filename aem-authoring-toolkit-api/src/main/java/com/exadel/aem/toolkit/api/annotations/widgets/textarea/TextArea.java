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
package com.exadel.aem.toolkit.api.annotations.widgets.textarea;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.EnumValue;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textarea/index.html">
 * TextArea element</a> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TEXTAREA)
@PropertyMapping
@SuppressWarnings("unused")
public @interface TextArea {
    /**
     * When set to a non-blank string, maps to the 'value' attribute of this TouchUI dialog component's node
     * Used to define default value for a TextField
     * @return String value
     */
    String value() default "";
    /**
     * When set to a non-blank string, maps to the 'emptyText' attribute of this TouchUI dialog component's node.
     * Used to define text hint for an empty TextArea
     * @return String value
     */
    String emptyText() default "";
    /**
     * When set to true, maps to the 'autofocus' attribute of this TouchUI dialog component's node.
     * Used to specify that this component will have focus after page load/refresh
     * @return True or false
     */
    boolean autofocus() default false;
    /**
     * Maps to the 'maxlength' attribute of this TouchUI dialog component's node.
     * Sets the maximal number of characters to be stored via this TextArea
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.POSITIVE)
    @PropertyRendering(ignoreValues = "0")
    long maxlength() default 0;
    /**
     * Maps to the 'cols' attribute of this TouchUI dialog component's node.
     * Sets the visible width of the text control, in average character widths
     * @return Long values
     */
    @ValueRestriction(ValueRestrictions.POSITIVE)
    @PropertyRendering(ignoreValues = "1")
    long cols() default 1;
    /**
     * Maps to the 'rows' attribute of this TouchUI dialog component's node.
     * Sets number of text rows
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.POSITIVE)
    long rows() default 5;
    /**
     * Maps to the 'resize' attribute of this TouchUI dialog component's node.
     * Sets the resizing type of this TextArea
     * @see TextAreaResizeType
     * @return One of {@code TextAreaResizeType} values
     */
    @EnumValue(transformation = StringTransformation.LOWERCASE)
    TextAreaResizeType resize() default TextAreaResizeType.NONE;
}
