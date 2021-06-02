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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

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
    private static final Pattern SYSTEM_PROPERTIES = Pattern.compile("^(sling:|cq:|jcr:(?!title)).*");
    private static final String PREVIEW_RENDERER_PATH = "itemPreview.html";
    private static final String INFO_RENDERER_PATH = "itemInfo.html";

    @Self
    private Resource currentResource;

    @SlingObject
    private ResourceResolver resourceResolver;

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
            .filter(entry -> !SYSTEM_PROPERTIES.matcher(entry.getKey()).matches())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Resource pageRes = getPageResource();
        if (pageRes != null) {
            itemResType = pageRes.getValueMap().get(CoreConstants.PN_ITEM_RESOURCE_TYPE, StringUtils.EMPTY);
        }
    }

    /**
     * Retrieves the resource type that defines the view and behavior of EToolbox List this item belongs to
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
     * Retrieves the path to a custom renderer for the 'preview' section of the List item.
     * @return String value, non-null
     */
    public String getPreviewRendererPath() {
        return getRendererPath(PREVIEW_RENDERER_PATH);
    }

    /**
     * Retrieves the path to a custom renderer for the 'info' section of the List item.
     * @return String value, non-null
     */
    public String getInfoRendererPath() {
        return getRendererPath(INFO_RENDERER_PATH);
    }

    /**
     * Retrieves the path to a custom renderer specified by {@code rendererPath} parameter
     * If the renderer does not exist, returns an empty string
     * @param rendererPath relative path to the renderer
     * @return String value, non-null
     */
    private String getRendererPath(String rendererPath) {
        String path = StringUtils.joinWith(CoreConstants.SEPARATOR_SLASH, itemResType, rendererPath);
        if (resourceResolver.getResource(path) != null) {
            return path;
        }
        return StringUtils.EMPTY;
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
