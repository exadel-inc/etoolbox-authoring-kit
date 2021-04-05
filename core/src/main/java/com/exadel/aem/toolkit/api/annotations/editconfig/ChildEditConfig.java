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
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;

/**
 * Defines the editing configuration for the Granite UI child components of the current component.
 * See <a href="https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/components-basics.html#component-basics">Adobe documentation</a>
 * for details
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(
    scope = Scopes.CQ_CHILD_EDIT_CONFIG,
    prefix = "cq:"
)
public @interface ChildEditConfig {

    /**
     * When set to a non-blank string, or to an array of strings, maps to the {@code cq:actions} property
     * of {@code cq:childEditConfig} node
     * @return String value, or an array of strings
     */
    String[] actions() default {};

    /**
     * Used to specify a collection of {@link DropTargetConfig} values for this child editing configuration
     * @return Single {@code DropTargetConfig} or an array of configs
     */
    DropTargetConfig[] dropTargets() default {};

    /**
     * Used to specify a collection of {@link Listener} configs for this child editing configuration
     * @return Single {@code Listener} or an array of Listeners
     */
    Listener[] listeners() default {};
}
