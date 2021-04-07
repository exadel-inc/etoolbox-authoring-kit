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
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/switch/index.html">
 * Switch</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.SWITCH)
@AnnotationRendering(properties = "all")
public @interface Switch {

    /**
     * Maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define the value for a Switch in {@code on} state
     * @return String {@code {Boolean}}-casted value
     */
    String value() default "{Boolean}true";

    /**
     * Maps to the {@code uncheckedValue} attribute of this Granite UI component's node.
     * Used to define the value for a Switch in {@code off} state
     * @return String {@code {Boolean}}-casted value
     */
    String uncheckedValue() default "{Boolean}false";

    /**
     * Maps to the {@code checked} attribute of this Granite UI component's node.
     * Used to define the checked state for a Switch
     * @return True or false
     */
    boolean checked() default false;

    /**
     * Maps to the {@code ignoreData} attribute of this Granite UI component's node.
     * @return True or false
     */
    boolean ignoreData() default false;

    /**
     * Maps to the {@code onText} attribute of this Granite UI component's node.
     * Used to define the text for a Switch in {@code on} state
     * @return String value, non-blank
     */
    String onText() default "On";

    /**
     * Maps to the {@code offText} attribute of this Granite UI component's node.
     * Used to define the text for a Switch in {@code off} state
     * @return String value, non-blank
     */
    String offText() default "Off";
}
