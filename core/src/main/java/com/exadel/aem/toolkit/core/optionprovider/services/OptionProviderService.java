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
package com.exadel.aem.toolkit.core.optionprovider.services;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;

/**
 * Prepares option sets for Granite-compliant custom data sources used in Granite UI.
 * Works with an accompanying Sling Http Servlet to serve option sets to the Granite UI frontend
 */
public interface OptionProviderService {

    /**
     * Prepares Adobe Granite datasource options in the form of a synthetic resource
     * @param request {@code SlingHttpServletRequest} object
     * @return List of {@link ValueMapDecorator} options, or an empty list if no options could be created
     */
    List<Resource> getOptions(SlingHttpServletRequest request);
}
