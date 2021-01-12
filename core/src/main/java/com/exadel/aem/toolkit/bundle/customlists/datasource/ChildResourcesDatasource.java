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

import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.PagingIterator;
import com.adobe.granite.ui.components.ds.AbstractDataSource;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.jackrabbit.commons.iterator.FilterIterator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_FOLDER;
import static com.day.cq.wcm.api.NameConstants.NN_TEMPLATE;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;

/**
 * Retrieves all child pages under the current root path, which are either custom lists themselves,
 * or folders that may contain lists inside. The result is then limited by 'offset' and 'limit' parameter values.
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=aem-custom-lists/datasources/list",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    }
)
public class ChildResourcesDatasource extends SlingSafeMethodsServlet {
    private static final String TEMPLATE = "/conf/authoring-toolkit/settings/wcm/templates/custom-list";
    private static final String PATH = "path";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    private static final String REP_PREFIX = "rep:";
    private static final String ITEM_RESOURCE_TYPE = "itemResourceType";
    private static final String PATH_TO_JCR_CONTENT = String.format("/%s", JCR_CONTENT);

    @Reference
    private transient ExpressionResolver expressionResolver;

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) {
        ResourceResolver resolver = request.getResourceResolver();

        SlingScriptHelper sling = getScriptHelper(request);
        DataSource ds = EmptyDataSource.instance();

        if (sling != null) {
            ExpressionHelper ex = new ExpressionHelper(expressionResolver, request);
            Config dsCfg = new Config(request.getResource().getChild(Config.DATASOURCE));

            String parentPath = ex.getString(dsCfg.get(PATH, String.class));
            Integer offset = ex.get(dsCfg.get(OFFSET, String.class), Integer.class);
            Integer limit = ex.get(dsCfg.get(LIMIT, String.class), Integer.class);

            Resource parent = parentPath != null ? resolver.getResource(parentPath) : null;

            if (parent != null) {
                String itemRT = dsCfg.get(ITEM_RESOURCE_TYPE, String.class);

                Iterator<Resource> children = parent.listChildren();
                Iterator<Resource> resources = getValidChildren(children, resolver).iterator();

                ds = createDataSource(resources, offset, limit, itemRT);
            }
        }
        request.setAttribute(DataSource.class.getName(), ds);
    }

    private static SlingScriptHelper getScriptHelper(ServletRequest request) {
        SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        return bindings.getSling();
    }

    private DataSource createDataSource(Iterator<Resource> resources, Integer offset, Integer limit, String itemRT) {
        return new AbstractDataSource() {
            @SuppressWarnings("unchecked")
            @Override
            public Iterator<Resource> iterator() {
                Iterator<Resource> it = new PagingIterator<>(new FilterIterator<>(resources, o -> {
                    String name = ((Resource) o).getName();
                    return !name.startsWith(REP_PREFIX) && !name.equals(JCR_CONTENT);
                }), offset, limit);

                return new TransformIterator(it, o -> new ResourceWrapper((Resource) o) {
                    @Nonnull
                    @Override
                    public String getResourceType() {
                        return itemRT;
                    }
                });
            }
        };
    }

    private List<Resource> getValidChildren(Iterator<Resource> parent, ResourceResolver resolver) {
        List<Resource> resources = new ArrayList<>();
        while (parent.hasNext()) {
            Resource child = parent.next();
            Resource childParameters = resolver.getResource(child.getPath() + PATH_TO_JCR_CONTENT);
            if (childParameters != null) {
                String template = childParameters.getValueMap().get(NN_TEMPLATE, String.class);
                if (template != null && template.equals(TEMPLATE)) {
                    resources.add(child);
                }
            }
            Iterator<Resource> children = child.listChildren();
            while (children.hasNext()) {
                String primaryType = children.next().getValueMap().get(JCR_PRIMARYTYPE, String.class);
                if (primaryType != null && (primaryType.equals(NT_PAGE) || primaryType.equals(NT_FOLDER))) {
                    resources.add(child);
                    break;
                }
            }
        }
        return resources;
    }
}
