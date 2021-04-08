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
package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

/**
 * Represents a Sling synthetic resource intended to be processed as a Granite datasource option with optional custom attributes
 */
class OptionResource extends ValueMapResource {

    private static final String NN_GRANITE_DATA = "granite:data";

    private Resource graniteDataChild;

    /**
     * Creates a new synthetic Sling resource intended to be processed as a Granite datasource
     * @param resourceResolver {@code ResourceResolver} associated with this synthetic resource
     * @param valueMap         {@code Map<String, Object>} decorated to a {@code ValueMap} that represents
     *                         mandatory attributes of this resource, namely <i>text</i> and <i>value</i>
     * @param customAttributes {@code Map<String, Object>} that represents custom attributes of this datasource option,
     *                         can be null or empty
     */
    OptionResource(ResourceResolver resourceResolver,
                   ValueMap valueMap,
                   Map<String, Object> customAttributes) {
        super(resourceResolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap);
        if (MapUtils.isNotEmpty(customAttributes)) {
            this.graniteDataChild = new ValueMapResource(getResourceResolver(),
                NN_GRANITE_DATA,
                JcrConstants.NT_UNSTRUCTURED,
                new ValueMapDecorator(customAttributes));
        }
    }

    /**
     * Returns a {@code granite:data} synthetic child resource if custom attributes are defined, otherwise returns null
     * @param relPath Name of a requested child node; only {@code granite:data} is handled
     * @return {@code Resource} object, or null
     */
    @Override
    public Resource getChild(String relPath) {
        if (NN_GRANITE_DATA.equals(relPath) && graniteDataChild != null) {
            return graniteDataChild;
        }
        return super.getChild(relPath);
    }
}
