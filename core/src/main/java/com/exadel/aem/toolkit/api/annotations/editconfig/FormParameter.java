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

/**
 * Used to populate {@link EditConfig#formParameters()} setting. If one or more {@code FormParameter}s are set for this
 * editing configuration, a {@code cq:editConfig/cq:FormParameters} node is created and filled in the current component's
 * configuration. See <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/components-basics.html#main-pars_title_8_waqwbc_refd_">
 *     AEM Components documentation</a>
 */
public @interface FormParameter {

    /**
     * Stores attribute name
     * @return String value, non-blank
     */
    String name();

    /**
     * Stores arbitrary attribute value
     * @return String value
     */
    String value();
}
