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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify that an {@link Annotation} defined in code is to be processed as the AEM Component / Granite UI annotation
 * @deprecated Since v. 2.0.2, there's no need to specially mark a custom annotation as soon as it is either automatically
 * processed with {@link AnnotationRendering} or handled with a {@link com.exadel.aem.toolkit.api.handlers.Handler}.
 * {@code DialogAnnotation} will be removed in a version after 2.0.2
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
@SuppressWarnings("squid:S1133")
public @interface DialogAnnotation {
    String source() default "";
}
