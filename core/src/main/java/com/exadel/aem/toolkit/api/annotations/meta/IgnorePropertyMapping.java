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
package com.exadel.aem.toolkit.api.annotations.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that a property (properties) of a specific annotation are not to be automatically mapped to Granite attributes.
 * If set to an entire annotation, this setting is legitimate to all of its properties unless explicitly marked with
 * {@link AnnotationRendering}
 * @deprecated This is deprecated and will be removed in a version after 2.0.2. Please use {@link AnnotationRendering} instead
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
@SuppressWarnings("squid:S1133")
public @interface IgnorePropertyMapping {
}
