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

package com.exadel.aem.toolkit.bundle.lists.datasource;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;

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

import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.PagingIterator;
import com.adobe.granite.ui.components.ds.AbstractDataSource;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_FOLDER;
import static com.day.cq.wcm.api.NameConstants.NN_TEMPLATE;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;

/**
 * Servlet that implements {@code datasource} pattern for populating a Lists Console
 * with all child pages under the current root path, which are either lists themselves,
 * or folders that may contain lists inside
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=aat-lists/datasources/list",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    }
)
public class ChildResourcesDatasource extends SlingSafeMethodsServlet {
    private static final String TEMPLATE_LIST = "/conf/authoring-toolkit/settings/wcm/templates/list";
    private static final String PATH = "path";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    private static final String REP_PREFIX = "rep:";
    private static final String PN_RESOURCE_TYPE = "itemResourceType";
    private static final String PATH_TO_JCR_CONTENT = String.format("/%s", JCR_CONTENT);

    @Reference
    private transient ExpressionResolver expressionResolver;

    /**
     * Processes {@code GET} requests to the current endpoint to add to the {@code SlingHttpServletRequest}
     * a {@code datasource} object filled with all child pages under the current root path, which are either
     * lists themselves, or folders that may contain lists inside.
     * The result is then limited by 'offset' and 'limit' parameter values.
     *
     * @param request  {@code SlingHttpServletRequest} instance
     * @param response {@code SlingHttpServletResponse} instance
     */
    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) {
        ResourceResolver resolver = request.getResourceResolver();

        DataSource dataSource = EmptyDataSource.instance();

        if (getScriptHelper(request) != null) {
            ExpressionHelper expressionHelper = new ExpressionHelper(expressionResolver, request);
            Config dsCfg = new Config(request.getResource().getChild(Config.DATASOURCE));

            String parentPath = expressionHelper.getString(dsCfg.get(PATH, String.class));
            Integer offset = expressionHelper.get(dsCfg.get(OFFSET, String.class), Integer.class);
            Integer limit = expressionHelper.get(dsCfg.get(LIMIT, String.class), Integer.class);
            String itemResourceType = dsCfg.get(PN_RESOURCE_TYPE, String.class);

            Resource parent = parentPath != null ? resolver.getResource(parentPath) : null;
            if (parent != null) {
                Iterator<Resource> resources = getValidChildren(resolver, parent).iterator();
                dataSource = createDataSource(resources, offset, limit, itemResourceType);
            }
        }
        request.setAttribute(DataSource.class.getName(), dataSource);
    }

    /**
     * Creates a {@code datasource} limited by 'offset' and 'limit' parameter values
     * from {@code Iterator<Resource>} instance
     *
     * @param resources {@code Iterator<Resource>} instance of valid child pages
     * @param offset    The integer number of items that should be skipped
     * @param limit     The integer number of items that should be included
     * @param itemRT    Resource type of items
     * @return {@code DataSource} object
     */
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

    /**
     * Retrieves the list of item resources, which are either lists themselves, or folders
     * that may contain lists inside
     *
     * @param resolver An instance of ResourceResolver
     * @param parent {@code Resource} instance used as the source of markup
     * @return a list of {@link Resource}s
     */
    private List<Resource> getValidChildren(ResourceResolver resolver, Resource parent) {
        return getChildrenStream(parent)
            .filter(resource -> checkResourceAsTemplate(resolver, resource) || checkResourceAsFolder(resource))
            .collect(Collectors.toList());
    }

    /**
     * Checks whether the resource is a list page
     *
     * @param resolver An instance of ResourceResolver
     * @param resource {@code Resource} instance used as the source of markup
     * @return True or false
     */
    private static boolean checkResourceAsTemplate(ResourceResolver resolver, Resource resource) {
        Resource childParameters = resolver.getResource(resource.getPath() + PATH_TO_JCR_CONTENT);
        if (childParameters != null) {
            String template = childParameters.getValueMap().get(NN_TEMPLATE, String.class);
            return template != null && template.equals(TEMPLATE_LIST);
        }
        return false;
    }

    /**
     * Checks whether the resource contains resources, which are either pages, or folders that may contain lists inside
     *
     * @param resource {@code Resource} instance used as the source of markup
     * @return True or false
     */
    private static boolean checkResourceAsFolder(Resource resource) {
        return getChildrenStream(resource).anyMatch(item -> {
            String primaryType = item.getValueMap().get(JCR_PRIMARYTYPE, String.class);
            return primaryType != null && (primaryType.equals(NT_PAGE) || primaryType.equals(NT_FOLDER));
        });
    }

    private static Stream<Resource> getChildrenStream(Resource resource) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(resource.listChildren(), Spliterator.ORDERED), false);
    }

    /**
     * Gets {@code request} sling script helper
     *
     * @param request {@code ServletRequest} instance
     * @return {@code SlingScriptHelper} instance
     */
    private static SlingScriptHelper getScriptHelper(ServletRequest request) {
        SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        return bindings.getSling();
    }
}
