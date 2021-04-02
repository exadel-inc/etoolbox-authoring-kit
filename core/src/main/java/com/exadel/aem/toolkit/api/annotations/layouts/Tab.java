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

/**
 * Used to define a specific
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/tabs/index.html">
 *     Tab</a> item in a multi-tab Granite UI dialog or within a {@code Tabs} widget
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tab {

    /**
     * Defines the tab title
     * @return String value (required)
     */
    @PropertyRendering(name = "jcr:title")
    String title();

    /**
     * Determines the element this component represents for the purpose of tracking. See
     * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">
     * Foundation tracking</a> for details.
     * <p>Note that the property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * @return String value (optional). If omitted, the value of {@code title} property is used
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
     * Determines whether to add padding to each panel.
     * <p>Note that the property is effective for <i>Tabs layout</i> and not the widget. Also, setting this property only
     * makes sense if {@code padding} at layout level is set to {@code false}</p>
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean padding() default false;
}
