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
package com.exadel.aem.toolkit.core.lists.models.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;

import com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Adapter class for a Sling resource representing a generic list item
 */
@AemComponent(
    path = "content/listItem",
    title = "List Item",
    componentGroup = "EToolbox Lists"
)
@EditConfig(
    actions = {ActionConstants.INSERT, ActionConstants.COPYMOVE, ActionConstants.DELETE}
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ListItemModel {
    private static final List<String> SYSTEM_PROPERTIES = Arrays.asList(
        JcrConstants.JCR_CREATED,
        JcrConstants.JCR_CREATED_BY,
        JcrConstants.JCR_LASTMODIFIED,
        JcrConstants.JCR_LAST_MODIFIED_BY,
        JcrConstants.JCR_PRIMARYTYPE,
        NameConstants.PN_PAGE_LAST_REPLICATED,
        NameConstants.PN_PAGE_LAST_REPLICATED_BY,
        NameConstants.PN_PAGE_LAST_REPLICATION_ACTION,
        ResourceResolver.PROPERTY_RESOURCE_TYPE
    );

    @Self
    private Resource currentResource;

    private String itemResType;

    private Map<String, Object> properties;

    /**
     * Initializes this model per Sling Model standard
     */
    @PostConstruct
    private void init() {
        properties = currentResource.getValueMap()
            .entrySet()
            .stream()
            .filter(entry -> !SYSTEM_PROPERTIES.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Resource pageRes = getPageResource();
        if (pageRes != null) {
            itemResType = pageRes.getValueMap().get(CoreConstants.PN_ITEM_RESOURCE_TYPE, StringUtils.EMPTY);
        }
    }

    /**
     * Retrieves the resource type  that defines the view and behavior of EToolbox List this item belongs to
     * @return String value, nullable
     */
    public String getItemResType() {
        return itemResType;
    }

    /**
     * Retrieves the collection of properties of the current list item
     * @return {@code Map} object, non-null
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Retrieves the {@code Resource} object characterizing the {@code Page} this item belongs to
     * @return {@code Page} instance, nullable
     */
    private Resource getPageResource() {
        return Optional.of(currentResource)
            .map(Resource::getParent)
            .map(Resource::getParent)
            .orElse(null);
    }
}
