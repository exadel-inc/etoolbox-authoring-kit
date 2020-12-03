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
package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.container.enums.TabsOrientation;
import com.exadel.aem.toolkit.api.annotations.container.enums.TabsSize;
import com.exadel.aem.toolkit.api.annotations.meta.EnumValue;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;

/**
 * Used to set up tabs container widget inside dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TABS)
@PropertyMapping
@SuppressWarnings("unused")
public @interface TabsWidget {

    /**
     * Name of current tabs widget
     * @return String value, required
     */
    String name();

    /**
     * The orientation of the tabs
     * @return One of {@code TabsOrientation} values
     * @see TabsOrientation
     */
    @EnumValue(transformation = StringTransformation.LOWERCASE)
    TabsOrientation variant() default TabsOrientation.HORIZONTAL;

    /**
     * The size of the tabs
     * @return One of {@code TabsSize} values
     * @see TabsSize
     */
    @EnumValue
    TabsSize size() default TabsSize.M;

    /**
     * Put vertical margin to the root element
     * @return True or false
     */
    boolean margin() default false;

    /**
     * Make the element maximized to fill the available space
     * @return True or false
     */
    boolean maximized() default false;

    /**
     * The name of the feature that the interaction takes place
     * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">
     * @return String value
     */
    String trackingFeature() default "";

    /**
     * The name of the widget
     * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/tracking/index.html">
     * @return String value
     */
    String trackingWidgetName() default "";

    /**
     * Enumerates the tabs to be rendered within this container
     * @return One or more {@code Tab} annotations
     * @see Tab
     */
    Tab[] tabs();
}
