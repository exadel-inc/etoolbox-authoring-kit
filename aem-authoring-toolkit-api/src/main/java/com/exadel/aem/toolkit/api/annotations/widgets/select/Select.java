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
package com.exadel.aem.toolkit.api.annotations.widgets.select;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.MapProperties;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html">
 * Select element</a> in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.SELECT)
@MapProperties
public @interface Select {

    /**
     * Used to specify collection of {@link Option}s within this Select
     * @return Single {@code Option} annotation, or an array of Options
     */
    @IgnorePropertyMapping
    Option[] options() default {};

    /**
     * Used to specify the source for options handled by the AEM Authoring Toolkit's OptionProvider mechanism
     * @return {@link OptionProvider} instance, or an empty {@code }OptionProvider} if not needed
     */
    @IgnorePropertyMapping
    OptionProvider optionProvider() default @OptionProvider;

    /**
     * When set to a non-blank string, maps to the 'emptyText' attribute of this TouchUI dialog component's node.
     * @return String value
     */
    String emptyText() default "";

    /**
     * When set, the {@code datasource} node is appended to the JCR buildup of this component
     * and populated with values of provided {@link DataSource} annotation
     * @return {@code @DataSource} instance
     */
    @IgnorePropertyMapping
    DataSource datasource() default @DataSource;

    /**
     * Indicates if the user is able to select multiple options
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean multiple() default false;

    /**
     * If set to true, labels of the options are translated by AEM
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "true")
    boolean translateOptions() default true;

    /**
     * If set to true, the options are sorted according to their labels' alphabetical order
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean ordered() default false;

    /**
     * It set to true, an empty option added to this {@code Select} widget.
     * Empty option is an option having both value and text equal to empty string
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean emptyOption() default false;

    /**
     * Maps to the 'variant' attribute of this {@code Select} widget
     * @see SelectVariant
     * @return One of {@code SelectVariant} values
     */
    @PropertyRendering(
        ignoreValues = "default",
        transform = StringTransformation.LOWERCASE
    )
    SelectVariant variant() default SelectVariant.DEFAULT;

    /**
     * If set to true, the SlingPostServlet @Delete hidden input is added to the HTTP form based on the field name
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "true")
    boolean deleteHint() default true;

    /**
     * Used to set 'ignore freshness' flag for this TouchUI component. This property is useful when having
     * a newly introduced field in the form, and there is a need to specifically
     * set the default selected item
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean forceIgnoreFreshness() default false;
}
