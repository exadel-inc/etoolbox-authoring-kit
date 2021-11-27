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

import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up
 * a <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/tabs/index.html">
 * Fixed Columns</a> container within a Granite UI dialog or a console page
 * <p>Note that the annotation is acceptable but not necessary to set the layout of a Granite UI dialog (i.e. when put
 * on a dialog class) because the "Fixed Columns" layout is applied by default when neither {@link Tabs} nor {@link Accordion}
 * is present</p>
 * @see Column
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.FIXED_COLUMNS)
public @interface FixedColumns {

    /**
     * Enumerates the columns to be rendered within this container
     * @return One or more {@link Column} annotations
     */
    Column[] value();

    /**
     * Determines whether to put vertical margin to the root element.
     * <p>Note that the property is effective for <i>Tabs widget</i> and not the tabbed layout</p>
     * @return True or false
     */
    boolean margin() default false;

    /**
     * Make the element maximized to fill the available space.
     * <p>Note that the property is effective for the in-dialog <i>Fixed Columns</i> widget</p>
     * @return True or false
     */
    boolean maximized() default false;
}
