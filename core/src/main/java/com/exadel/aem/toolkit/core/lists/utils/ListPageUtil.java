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
package com.exadel.aem.toolkit.core.lists.utils;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;

/**
 * Contains methods for manipulating EToolbox List Pages
 * <p><u>Note</u>: This class is not a part of the public API</p>
 */
class ListPageUtil {

    private static final Map<String, Object> LIST_PROPERTIES = Collections.singletonMap(
        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
        "wcm/foundation/components/responsivegrid");


    /**
     * Creates a page representing an EToolbox List under the given JCR path
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param path             JCR path of the items list page
     * @return {@link Page} containing the list of entries
     * @throws WCMException If the page cannot be created
     */
    public static Page createPage(ResourceResolver resourceResolver, String path) throws WCMException, PersistenceException {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            throw new WCMException("Could not retrieve a PageManager instance");
        }

        String parentPath = StringUtils.substringBeforeLast(path, CoreConstants.SEPARATOR_SLASH);
        String pageName = StringUtils.substringAfterLast(path, CoreConstants.SEPARATOR_SLASH);
        Page listPage = pageManager.create(parentPath, JcrUtil.createValidName(pageName), ListConstants.LIST_TEMPLATE_NAME, pageName);

        ModifiableValueMap pageProperties = listPage.getContentResource().adaptTo(ModifiableValueMap.class);
        if (pageProperties != null) {
            pageProperties.put(CoreConstants.PN_ITEM_RESOURCE_TYPE, ListConstants.SIMPLE_LIST_ITEM_RESOURCE_TYPE);
        }

        resourceResolver.create(listPage.getContentResource(), CoreConstants.NN_LIST, LIST_PROPERTIES);
        return listPage;
    }

    /**
     * Default (instantiation-restricting) constructor
     */
    private ListPageUtil() {
    }
}
