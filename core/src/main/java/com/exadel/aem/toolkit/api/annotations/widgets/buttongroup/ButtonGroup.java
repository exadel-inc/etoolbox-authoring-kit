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
package com.exadel.aem.toolkit.api.annotations.widgets.buttongroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.SelectionMode;

/**
 * Used to set up
 * <a href="https://www.adobe.io/experience-manager/reference-materials/6-5/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/buttongroup/index.html">
 * ButtonGroup</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.BUTTON_GROUP)
@AnnotationRendering(properties = "all")
public @interface ButtonGroup {

    /**
     * Used to specify the collection of {@link ButtonGroupItem}s within this ButtonGroup
     * @return Single {@code ButtonGroupItem} annotation, or an array of items
     */
    ButtonGroupItem[] items() default {};

    /**
     * Used to specify the source for this component's child items handled by the ToolKit's {@code OptionProvider}
     * mechanism
     * @return {@link OptionProvider} instance, or an empty {@code OptionProvider} if not needed
     */
    OptionProvider itemProvider() default @OptionProvider;

    /**
     * Maps to the {@code selectionMode} attribute of this {@code ButtonGroup}
     * @return One of {@code SelectionMode} values: "none" (default), "single", or "multiple"
     * @see SelectionMode
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE, ignoreValues = "none")
    SelectionMode selectionMode() default SelectionMode.NONE;

    /**
     * If set to true, the @Delete hidden input is added to the web form. Its value will be processed by the Sling HTTP
     * post servlet
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "true")
    boolean deleteHint() default true;

    /**
     * If true, the checked status of each item is based on its checked property. Otherwise, the status is based on
     * matching the form values by name and value properties
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean ignoreData() default false;
}
