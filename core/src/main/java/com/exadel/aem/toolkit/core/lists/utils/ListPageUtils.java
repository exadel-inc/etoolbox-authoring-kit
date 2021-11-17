package com.exadel.aem.toolkit.core.lists.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.ResourceResolver;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;

/**
 * Contains methods for manipulation with List Page
 */
class ListPageUtils {

    /**
     * Creates a list page under given path
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param path             JCR path of the items list page.
     * @return Created page containing list of entries or ${@code null} if {@link PageManager} cannot be instantiated
     * or {@code path} is blank
     * @throws WCMException if a page cannot be created
     */
    static Page createListPage(ResourceResolver resourceResolver, String path) throws WCMException {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        if (pageManager == null || StringUtils.isBlank(path)) {
            return null;
        }

        String parentPath = StringUtils.substringBeforeLast(path, CoreConstants.SEPARATOR_SLASH);
        String pageName = StringUtils.substringAfterLast(path, CoreConstants.SEPARATOR_SLASH);
        Page listPage = pageManager.create(parentPath, JcrUtil.createValidName(pageName), ListConstants.LIST_TEMPLATE_NAME, pageName);

        ModifiableValueMap pageProperties = listPage.getContentResource().adaptTo(ModifiableValueMap.class);
        if (pageProperties != null) {
            pageProperties.put(CoreConstants.PN_ITEM_RESOURCE_TYPE, ListConstants.SIMPLE_LIST_ITEM_RESOURCE_TYPE);
        }

        return listPage;
    }

    /**
     * Default (instantiation-restricting) constructor
     */
    private ListPageUtils() {
    }
}
