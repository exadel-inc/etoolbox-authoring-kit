/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.exadel.aem.toolkit.bundle.customlists.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.jcr.query.Query;
import javax.servlet.Servlet;

import com.day.cq.commons.jcr.JcrConstants;
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

import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that implements {@code datasource} pattern for populating a TouchUI {@code select} widget
 * with item components that have acl-item-component group in a Granite datasource
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=aem-custom-lists/datasources/list-items",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    }
)
public class ItemComponentDatasource extends SlingSafeMethodsServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ItemComponentDatasource.class);

    private static final String SELECT_STATEMENT = "SELECT * FROM [cq:Component] AS s WHERE ISDESCENDANTNODE(s,'/apps') AND [componentGroup] = 'acl-item-component'";

    private static final String VALUE = "value";
    private static final String TEXT = "text";

    /**
     * Processes {@code GET} requests to the current endpoint to add to the {@code SlingHttpServletRequest}
     * a {@code datasource} object filled with item components that have acl-item-component group in a Granite datasource
     *
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
                valueMap.put(VALUE, item.getPath());
                valueMap.put(TEXT, item.getValueMap().get(JcrConstants.JCR_TITLE, ""));
                actualList.add(new ValueMapResource(resolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
            }
            DataSource dataSource = new SimpleDataSource(actualList.iterator());
            request.setAttribute(DataSource.class.getName(), dataSource);
        }
        catch (SlingException | IllegalStateException ex){
            LOG.error(ex.getMessage());
        }
    }
}

