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
package com.exadel.aem.toolkit.api.annotations.assets.dependson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define {@code dependsOnRef} and {@code dependsOnRefType} attributes of {@code granite:data} child node of
 * the current widget node to engage DependsOn frontend routines
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOnRef {

    /**
     * Defines the {@code dependsOnRef} attribute
     * If not set, {@code name} will be equal to the annotated field name
     * @return String value representing tag that would be specified in foreign {@link DependsOn} annotations, non-null
     */
    String name() default "";

    /**
     * Defines the {@code dependsOnRefType} attribute
     * @return One of the predefined {@link DependsOnRefTypes} constants
     */
    DependsOnRefTypes type() default DependsOnRefTypes.AUTO;

    /**
     * Defines the {@code dependsOnRefLazy} attribute
     * @return marker to make DependsOn reference lazy.
     */
    boolean lazy() default false;
}
