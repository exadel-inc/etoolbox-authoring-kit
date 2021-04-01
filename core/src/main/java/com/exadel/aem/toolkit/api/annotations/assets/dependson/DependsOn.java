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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define {@code dependsOn} and {@code dependsOnActon} attributes as well as {@code dependsOn}-related parameters
 * of the {@code granite:data} child node of the current widget node to engage DependsOn frontend routines
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DependsOnConfig.class)
public @interface DependsOn {

    /**
     * Defines the {@code dependsOn} attribute value
     * @return String value, non-null (see DependsOn documentation for details)
     */
    String query();

    /**
     * Defines the {@code dependsOnAction} attribute value
     * @return String value, one of {@link DependsOnActions} items or a custom-defined action name
     * @see DependsOnActions
     */
    String action() default DependsOnActions.VISIBILITY;

    /**
     * Defines custom static parameters to be used by DependsOn actions
     * @return Single {@link DependsOnParam} annotation, or an array of {@code DependsOnParam}s
     */
    DependsOnParam[] params() default {};
}
