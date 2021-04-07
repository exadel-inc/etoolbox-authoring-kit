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
package com.exadel.aem.toolkit.api.annotations.editconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;
import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;

/**
 * Defines editing configuration for a Granite UI component. Upon processing this annotation, a {@code cq:editConfig}
 * node within a component's configuration is created.
 * See <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/components-basics.html#EditBehavior">Adobe documentation</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(
    scope = Scopes.CQ_EDIT_CONFIG,
    prefix = "cq:",
    properties = {"actions", "emptyText", "inherit", "dialogLayout"}
)
public @interface EditConfig {

    /**
     * When set to a non-blank string or array of strings, maps to the {@code cq:actions} property
     * of {@code cq:editConfig} node
     * @return String / array value
     */
    @PropertyRendering(transform = StringTransformation.LOWERCASE)
    String[] actions() default {};

    /**
     * When set to a non-blank string, maps to the {@code emptyText} attribute of {@code cq:editConfig} node
     * @return String value
     */
    String emptyText() default "";

    /**
     * When set to true, renders as the {@code inherit} attribute of {@code cq:editConfig} node
     * @return True or false
     */
    boolean inherit() default false;

    /**
     * When set to a value other than {@code EditConfigLayout.DEFAULT}, renders as the {@code dialogLayout} attribute
     * of {@code cq:editConfig} node
     * @return One of {@link EditConfigLayout} constants
     */
    @PropertyRendering(ignoreValues = "default", transform = StringTransformation.LOWERCASE)
    EditConfigLayout dialogLayout() default EditConfigLayout.DEFAULT;

    /**
     * Used to specify a collection of {@link DropTargetConfig} values for this editing configuration
     * @return Single {@code DropTargetConfig} or an array of configs
     */
    DropTargetConfig[] dropTargets() default {};

    /**
     * Used to specify a collection of {@link FormParameter} values for this editing configuration
     * @return Single {@code FormParameter} or an array of parameters
     */
    FormParameter[] formParameters() default {};

    /**
     * Used to specify a collection of {@link InplaceEditingConfig} values for this editing configuration
     * @return Single {@code InplaceEditingConfig} or an array of configs
     */
    InplaceEditingConfig[] inplaceEditing() default @InplaceEditingConfig(propertyName = "");

    /**
     * Used to specify a collection of {@link Listener} configs for this editing configuration
     * @return Single {@code Listener} or an array of Listeners
     */
    Listener[] listeners() default {};
}
