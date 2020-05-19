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
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html">
 * Select element</a> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.SELECT)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Select {
    /**
     * Used to specify collection of {@link Option}s within this Select
     * @return Single {@code Option} annotation, or an array of Options
     */
    @IgnorePropertyMapping
    Option[] options() default {};

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
     * @deprecated Use {@code datasource:resourceType} instead
     * When set to a non-blank string, allows to override {@code sling:resourceType} attribute of a {@code datasource node}
     * pointing to a ACS Commons list
     * @return String value
     */
    @Deprecated
    @IgnorePropertyMapping
    @SuppressWarnings("squid:S1133")
    String acsListResourceType() default "";

    /**
     * @deprecated Use {@code datasource:path} instead
     * When set to a non-blank string, a {@code datasource} node is appended to the JCR buildup of this component
     * pointing to a ACS Commons list
     * @return Valid JCR path, or an empty string
     */
    @Deprecated
    @IgnorePropertyMapping
    @SuppressWarnings("squid:S1133")
    String acsListPath() default "";

    /**
     * When this option is to true, and also {@link Select#acsListPath()} is specified, renders the {@code addNone} attribute
     * to the {@code datasource} node of this TouchUI dialog component's node so that "none" option is added to the
     * list of selectable options.
     * This option has no effect unless valid {@code acsListPath} is set.
     * @return True or false
     *
     * @deprecated This will be removed starting from version 2.0.0
     */
    @IgnorePropertyMapping
    @Deprecated
    @SuppressWarnings("squid:S1133")
    boolean addNoneOption() default false;

    /**
     * Indicates if the user is able to select multiple selections.
     *
     * @return true or false
     */
    boolean multiple() default false;

    /**
     * Returns true to translate the options, false otherwise.
     *
     * @return true or false
     */
    boolean translateOptions() default false;

    /**
     * Returns true to sort the options based on the text, false otherwise.
     * <p>
     * It is assumed that the options don’t contain option group.
     *
     * @return true or false
     */
    boolean ordered() default false;

    /**
     * Returns true to also add an empty option; false otherwise.
     * <p>
     * Empty option is an option having both value and text equal to empty string.
     *
     * @return true or false
     */
    boolean emptyOption() default false;

    /**
     * Return default or quite value.
     *
     * @return String value
     */
    String variant() default "";

    /**
     * Return true to generate the SlingPostServlet @Delete hidden input based on the
     * field name.
     *
     * @return true or false
     */
    boolean deleteHint() default false;

    /**
     * Return true to generate the SlingPostServlet @Delete hidden input based on the
     * field name.
     *
     * @return true or false
     */
    boolean forceIgnoreFreshness() default false;
}
