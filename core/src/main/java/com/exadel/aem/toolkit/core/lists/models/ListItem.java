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

package com.exadel.aem.toolkit.core.lists.models;

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
    componentGroup = "AEMBox Lists"
)
@EditConfig(
    actions = {ActionConstants.INSERT, ActionConstants.COPYMOVE, ActionConstants.DELETE}
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ListItem {

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

    public String getItemResType() {
        return itemResType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    private Resource getPageResource() {
        return Optional.of(currentResource)
            .map(Resource::getParent)
            .map(Resource::getParent)
            .orElse(null);
    }
}
