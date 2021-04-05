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
package com.exadel.aem.toolkit.api.annotations.widgets.radio;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/radiogroup/index.html">
 * RadioGroup</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.RADIOGROUP)
@AnnotationRendering(properties = "vertical")
public @interface RadioGroup {

    /**
     * Maps to the {@code vertical} attribute of this Granite UI component's node.
     * Sets whether this RadioGroup is displayed as a vertical stack
     * @return True or false
     */
    boolean vertical() default true;

    /**
     * Used to specify the collection of {@link RadioButton}s within this RadioGroup
     * @return Single {@code @RadioButton} annotation, or an array of RadioButtons
     */
    RadioButton[] buttons() default {};

    /**
     * Used to specify the source for options handled by the ToolKit's {@code OptionProvider} mechanism
     * @return {@link OptionProvider} instance, or an empty {@code OptionProvider} if not needed
     */
    OptionProvider buttonProvider() default @OptionProvider;

    /**
     * When set, the {@code datasource} node is appended to the JCR buildup of this component
     * and populated with values of the provided {@link DataSource} annotation
     * @return {@code @DataSource} instance
     */
    DataSource datasource() default @DataSource;
}
