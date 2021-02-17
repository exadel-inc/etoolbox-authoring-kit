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
package com.exadel.aem.toolkit.api.annotations.layouts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;

/**
 * Used to define a specific
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/tabs/index.html">
 *     Tab</a> item in multi-tab TouchUI dialog setup or within a {@code Tabs} widget
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tab {
    /**
     * Maps to the 'jcr:title' attribute of a {@code cq:dialog/content/items/tabs/items/<this_tab>} node
     * @return String value, required
     */
    String title();

    /**
     * Indicates the element this tab item represents for the purpose of tracking
     * @return String value (optional). If omitted, the value of {@code title} is used
     */
    String trackingElement() default "";

    /**
     * Indicates if the tab is active by default
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean active() default false;

    /**
     * Used to set up the icon of a tab
     * @return String value (optional)
     */
    String icon() default "";

    /**
     * When set to non-default, renders as set of specific attributes of a tab node
     * @see Attribute
     * @return {@code Attribute} value
     */
    Attribute attribute() default @Attribute;
}
