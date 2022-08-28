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
package com.exadel.aem.toolkit.core.authoring.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Represents the back-end part of the {@code AutocompleteOption} of the {@code Autocomplete} component for Granite UI dialogs. His Sling model
 * is responsible for injecting and retrieving the properties of component's node.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Option {

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String value;

    /**
     * Retrieves the specified text value of the autocomplete option
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * Retrieves the specified value of the autocomplete option
     * @return String
     */
    public String getValue() {
        return value;
    }
}
