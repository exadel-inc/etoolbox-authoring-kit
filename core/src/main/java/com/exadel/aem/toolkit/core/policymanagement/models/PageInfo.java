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
package com.exadel.aem.toolkit.core.policymanagement.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Represents an AEM page resource that is used to determine templates' policies (designs) for the top level containers
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@Model(adaptables = Resource.class)
public class PageInfo {

    @ValueMapValue(name = "cq:template")
    private String template;

    @ValueMapValue(name = JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY)
    private String resourceType;

    /**
     * Gets the {@code cq:template} value associated with the current page
     * @return String value, nullable
     */
    public String getTemplate() {
        return this.template;
    }

    /**
     * Gets the {@code sling:resourceType} of the current page
     * @return String value, nullable
     */
    public String getResourceType() {
        return this.resourceType;
    }
}
