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
package com.exadel.aem.toolkit.core.lists.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;

/**
 * Delivers content of an Exadel Toolbox List as a JSON array
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=/apps/etoolbox-authoring-kit/lists/components/structure/page-list",
        "sling.servlet.extensions=json",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.selectors=list"
    }
)
public class ListsJsonServlet extends SlingSafeMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ListsJsonServlet.class);

    private static final Gson GSON = new Gson();

    /**
     * Processes a GET request to deliver the content of an Exadel Toolbox List as a JSON array
     * @param request  {@code SlingHttpServletRequest} instance
     * @param response {@code SlingHttpServletResponse} instance
     * @throws IOException If failed to output to the response stream
     */
    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws IOException {
        PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
        Page page = pageManager != null ? pageManager.getContainingPage(request.getResource()) : null;
        if (page == null) {
            LOG.warn("Resource at {} cannot be adapted to a page", request.getResource().getPath());
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return;
        }

        Resource listResource = page.getContentResource().getChild("list");
        if (listResource == null) {
            LOG.warn("Path {} does not belong to an EToolbox List", request.getResource().getPath());
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Resource item : listResource.getChildren()) {
            if (!ListConstants.LIST_ITEM_RESOURCE_TYPE.equals(item.getResourceType())) {
                continue;
            }
            ValueMap valueMap = item.getValueMap();
            Map<String, Object> userValues = valueMap
                .entrySet()
                .stream()
                .filter(entry -> JcrConstants.JCR_TITLE.equals(entry.getKey())
                    || !StringUtils.contains(entry.getKey(), CoreConstants.SEPARATOR_COLON))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (userValues.isEmpty()) {
                continue;
            }
            result.add(userValues);
        }

        response.setContentType(CoreConstants.CONTENT_TYPE_JSON);
        response.getWriter().write(GSON.toJson(result));
    }
}
