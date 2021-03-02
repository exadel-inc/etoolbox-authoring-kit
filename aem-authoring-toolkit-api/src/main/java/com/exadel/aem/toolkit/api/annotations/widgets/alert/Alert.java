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
package com.exadel.aem.toolkit.api.annotations.widgets.alert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.common.StatusVariantConstants;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/alert/index.html">
 * Alert element</a> in TouchUI dialog
 *
 * @deprecated This annotation is deprecated and will be removed in a version after 2.0.1.
 * Please use {@link com.exadel.aem.toolkit.api.annotations.widgets.Alert}
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.ALERT)
@PropertyMapping
@Deprecated
@SuppressWarnings("squid:S1133")
public @interface Alert {

    /**
     * Maps to the 'variant' attribute of this TouchUI dialog component's node.
     *
     * @return Alert style
     * @see StatusVariantConstants
     */
    String variant() default "";

    /**
     * Maps to the 'size' attribute of this TouchUI dialog component's node.
     * Used to define Alert size
     *
     * @return One of {@code AlertSize} values
     * @see AlertSize
     */
    AlertSize size() default AlertSize.SMALL;

    /**
     * When set to a non-blank string, maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define content HTML for Alert component
     *
     * @return String value
     */
    String text() default "";

    /**
     * When set to a non-blank string, maps to the 'jcr:title' attribute of this TouchUI dialog component's node.
     * Used to define header text for Alert component
     *
     * @return String value
     */
    @PropertyRendering(name = "jcr:title")
    String title() default "";
}
