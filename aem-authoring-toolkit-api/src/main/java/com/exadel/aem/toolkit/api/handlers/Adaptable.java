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

package com.exadel.aem.toolkit.api.handlers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a custom class that may be passed as an argument in an {@code adaptTo()} method of an AEM Authoring
 * Toolkit entity, such as {@code Source} or {@code Target}.
 * The annotated class must expose a public constructor that accepts a single argument - an instance of matched entity,
 * e.g. {@code public MyAdapter(Source source) {...}}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Adaptable {

    /**
     * Specifies an entity, such as {@code Source} or {@code Target}, that can be adapted to the underlying custom class
     * @return {@code Class<?>} value, non-null
     */
    Class<?> value();
}
