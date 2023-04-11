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
package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;

/**
 * Represents an entity responsible for converting a resource identifier into an option data source
 * @see OptionProviderService
 */
interface OptionSourceResolver {

    /**
     * Retrieves or produces a {@link Resource} object representing the option datasource
     * @param request Current {@link SlingHttpServletRequest}
     * @param params  {@link PathParameters} object containing the path to resolve as well as the
     *                values that affect the resolution routine
     * @return {@code Resource} instance, or else {@code null}
     */
    Resource resolve(SlingHttpServletRequest request, PathParameters params);
}
