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
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;
import com.exadel.aem.toolkit.api.annotations.widgets.common.StatusVariant;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/alert/index.html">
 * Alert </a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.ALERT)
@AnnotationRendering(properties = "all")
public @interface Alert {

    /**
     * Maps to the {@code variant} attribute of this Granite UI component's node.
     * @return One of {@code StatusVariant} values
     * @see StatusVariant
     */
    @PropertyRendering(ignoreValues = "info")
    StatusVariant variant() default StatusVariant.INFO;

    /**
     * Maps to the {@code size} attribute of this Granite UI component's node. Used to define Alert size
     * <p><u>Note:</u> only {@code "small"} and {@code "large"} values are officially supported</p>
     * @return One of {@code Size} values
     * @see Size
     */
    Size size() default Size.SMALL;

    /**
     * When set to a non-blank string, maps to the {@code text} attribute of this Granite UI component's node.
     * Used to define content HTML for Alert component
     * @return String value
     */
    String text() default "";

    /**
     * When set to a non-blank string, maps to the {@code jcr:title} attribute of this Granite UI component's node.
     * Used to define header text for Alert component
     * @return String value
     */
    @PropertyRendering(name = "jcr:title")
    String title() default "";
}
