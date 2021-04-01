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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Presents a name-value pair of {@code granite:data} attributes to be rendered as a DependsOn parameter
 * @see DependsOn
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOnParam {

    /**
     * Defines parameter name
     * The resulting attribute's name will be rendered as 'dependsOn-{action}-{name}'
     * @return String value, non-blank
     */
    String name();

    /**
     * Defines parameter value
     * @return String value, non-blank
     */
    String value();
}
