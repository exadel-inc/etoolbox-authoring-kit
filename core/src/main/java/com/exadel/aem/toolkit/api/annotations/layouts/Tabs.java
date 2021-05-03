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
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Orientation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;

/**
 * Used to define the tabbed layout for a Granite UI dialog and/or to set up
 * a <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/tabs/index.html">
 * Tabs</a> widget
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TABS)
public @interface Tabs {

    /**
     * Enumerates the tabs to be rendered within this container
     * @return One or more {@link Tab} annotations
     */
    Tab[] value();

    /**
     * Determines the orientation of {@code tabs} when used as a dialog widget.
     * <p>Note that the property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * @return One of {@link Orientation} values
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE)
    Orientation orientation() default Orientation.HORIZONTAL;

    /**
     * Determines the size of the tabs.
     * <p>The property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * <p><u>Note:</u> only {@code "medium"} and {@code "large"} values are officially supported</p>
     * @return One of {@code Size} values
     * @see Size
     */
    Size size() default Size.MEDIUM;

    /**
     * Determines whether to put vertical margin to the root element.
     * <p>Note that the property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * @return True or false
     */
    boolean margin() default false;

    /**
     * Make the element maximized to fill the available space.
     * <p>Note that the property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * @return True or false
     */
    boolean maximized() default false;

    /**
     * Determines the name of the feature responsible for the interaction. See
     * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">
     * Foundation tracking</a> for details.
     * <p>Note that the property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * @return String value (optional)
     */
    String trackingFeature() default "";

    /**
     * Determines the name of the widget responsible for the interaction. See
     * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">
     * Foundation tracking</a> for details.
     * <p>Note that the property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * @return String value (optional)
     */
    String trackingWidgetName() default "";

    /**
     * Determines the layout mode of this tabs collection.
     * <p>Note that the property is effective for <i>Tabs layout</i> and not the widget</p>
     * @return One of {@link LayoutType} values
     */
    @PropertyRendering(
        transform = StringTransformation.LOWERCASE,
        ignoreValues = "default"
    )
    LayoutType type() default LayoutType.DEFAULT;

    /**
     * Determines whether to add padding to each panel.
     * <p>Note that the property is effective for <i>Tabs layout</i> and not the widget</p>
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "true")
    boolean padding() default true;
}
