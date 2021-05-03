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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.jcr.query.Query;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Provides the collection of AEM resources that will represent EToolbox Lists items. This collection will be displayed
 * in a Granite UI {@code Select} widget
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=/apps/etoolbox-authoring-kit/datasources/list-items",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    }
)
public class ItemComponentsServlet extends SlingSafeMethodsServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ItemComponentsServlet.class);

    private static final String SELECT_STATEMENT = String.format(
        "SELECT * FROM [cq:Component] AS s WHERE ISDESCENDANTNODE(s,'/apps') AND [%s] = 'true'",
        CoreConstants.PN_LIST_ITEM);

    /**
     * Processes {@code GET} requests to the current endpoint to add to the {@code SlingHttpServletRequest}
     * a {@code DataSource} object filled with item components that are list items
     * @param request  {@code SlingHttpServletRequest} instance
     * @param response {@code SlingHttpServletResponse} instance
     */
    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) {
        try {
            ResourceResolver resolver = request.getResourceResolver();
            Iterator<Resource> resources = resolver.findResources(SELECT_STATEMENT, Query.JCR_SQL2);
            List<Resource> actualList = new ArrayList<>();
            while (resources.hasNext()) {
                Resource item = resources.next();
                ValueMap valueMap = new ValueMapDecorator(new HashMap<>());
                valueMap.put(CoreConstants.PN_VALUE, item.getPath());
                valueMap.put(CoreConstants.PN_TEXT, item.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
                actualList.add(new ValueMapResource(resolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
            }
            DataSource dataSource = new SimpleDataSource(actualList.iterator());
            request.setAttribute(DataSource.class.getName(), dataSource);
        } catch (SlingException | IllegalStateException ex) {
            LOG.error(ex.getMessage());
        }
    }
}

