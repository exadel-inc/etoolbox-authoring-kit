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

package com.exadel.aem.toolkit.api.annotations.editconfig;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines editing configuration for a TouchUI-ready child components.
 * See <a href="https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/components-basics.html#component-basics">Adobe documentation</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping
@SuppressWarnings("unused")
public @interface ChildEditConfig {

    /**
     * Expected {@link com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig} value
     * @return Single {@code EditConfig}
     */
    EditConfig value() default @EditConfig;
}