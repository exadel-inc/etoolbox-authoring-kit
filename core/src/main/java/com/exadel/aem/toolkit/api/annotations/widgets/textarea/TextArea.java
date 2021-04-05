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
package com.exadel.aem.toolkit.api.annotations.widgets.textarea;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/textarea/index.html">
 * TextArea</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TEXTAREA)
@AnnotationRendering(properties = "all")
public @interface TextArea {

    /**
     * When set to a non-blank string, maps to the {@code value} attribute of this Granite UI component's node
     * Used to define the default value for a TextField
     * @return String value
     */
    String value() default "";

    /**
     * When set to a non-blank string, maps to the {@code emptyText} attribute of this Granite UI component's node.
     * Used to define the text hint for an empty TextArea
     * @return String value
     */
    String emptyText() default "";

    /**
     * When set to true, maps to the {@code autofocus} attribute of this Granite UI component's node.
     * Used to specify that this component will have focus after page load/refresh
     * @return True or false
     */
    boolean autofocus() default false;

    /**
     * Maps to the {@code maxlength} attribute of this Granite UI component's node.
     * Sets the maximal number of characters to be stored via this TextArea
     * @return Long value
     */
    @PropertyRendering(ignoreValues = "0")
    @ValueRestriction(ValueRestrictions.POSITIVE)
    long maxlength() default 0;

    /**
     * Maps to the {@code cols} attribute of this Granite UI component's node.
     * Sets the visible width of the text control, in average character widths
     * @return Long values
     */
    @PropertyRendering(ignoreValues = "1")
    @ValueRestriction(ValueRestrictions.POSITIVE)
    long cols() default 1;

    /**
     * Maps to the {@code rows} attribute of this Granite UI component's node.
     * Sets number of text rows
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.POSITIVE)
    long rows() default 5;

    /**
     * Maps to the {@code resize} attribute of this Granite UI component's node.
     * Sets the resizing type of this TextArea
     * @see TextAreaResizeType
     * @return One of {@code TextAreaResizeType} values
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE)
    TextAreaResizeType resize() default TextAreaResizeType.NONE;
}
