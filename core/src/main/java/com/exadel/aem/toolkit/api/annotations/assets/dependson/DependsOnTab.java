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
 * Used to define {@code dependsOn} and {@code dependsOnActon} attributes of {@code granite:data} child node of the current
 * tab node to engage DependsOn frontend routines
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DependsOnTabConfig.class)
public @interface DependsOnTab {

    /**
     * Defines the 'dependsOn' attribute
     * @return String representing value the current tab depends on, non-null
     */
    String query();

    /**
     * Specifies the tab by its title
     * @return String value, non-null
     */
    String tabTitle();
}
