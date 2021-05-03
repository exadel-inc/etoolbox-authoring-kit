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
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.PagingIterator;
import com.adobe.granite.ui.components.ds.AbstractDataSource;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Provides the collection of AEM resources that either represent EToolbox Lists or serve as folders for EToolbox Lists
 * to be displayed in the EToolbox Lists console
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=/apps/etoolbox-authoring-kit/datasources/lists",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    }
)
public class ListsServlet extends SlingSafeMethodsServlet {
    private static final String LIST_TEMPLATE_NAME = "/conf/etoolbox-authoring-kit/settings/wcm/templates/list";
    private static final String PATH_JCR_CONTENT = CoreConstants.SEPARATOR_SLASH + JcrConstants.JCR_CONTENT;
    private static final String PREFIX_REP = "rep:";

    @Reference
    private transient ExpressionResolver expressionResolver;

    /**
     * Processes {@code GET} requests to the current endpoint to add to the {@code SlingHttpServletRequest}
     * a {@code DataSource} object filled with all child pages under the current root path, which are either
     * lists themselves, or folders that may contain lists inside.
     * The result is then limited by {@code offset} and {@code limit} parameter values.
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

            String parentPath = expressionHelper.getString(dsCfg.get(CoreConstants.PN_PATH, String.class));
            int offset = expressionHelper.get(dsCfg.get(CoreConstants.PN_OFFSET, String.class), Integer.class);
            int limit = expressionHelper.get(dsCfg.get(CoreConstants.PN_LIMIT, String.class), Integer.class);
            String itemResourceType = dsCfg.get(CoreConstants.PN_ITEM_RESOURCE_TYPE, String.class);

            Resource parent = parentPath != null ? resolver.getResource(parentPath) : null;
            if (parent != null) {
                List<Resource> resources = getValidChildren(resolver, parent);
                dataSource = new PagingDataSource(resources, offset, limit, itemResourceType);
            }
        }
        request.setAttribute(DataSource.class.getName(), dataSource);
    }

    /**
     * Retrieves the collection of resources which are either EToolbox Lists or folders that can contain lists.
     * Service and content nodes are excluded from the output
     * @param resolver {@code ResourceResolver} object
     * @param parent   {@code Resource} instance used as the source of markup
     * @return List of {@code Resource} objects
     */
    private List<Resource> getValidChildren(ResourceResolver resolver, Resource parent) {
        return getChildrenStream(parent)
            .filter(resource -> !isServiceNode(resource) && (isFolder(resource) || containsResource(resource) || isList(resolver, resource)))
            .collect(Collectors.toList());
    }

    /**
     * Checks whether the resource is a EToolbox List page
     * @param resolver {@code ResourceResolver} object
     * @param resource {@code Resource} instance
     * @return True or false
     */
    private static boolean isList(ResourceResolver resolver, Resource resource) {
        Resource childParameters = resolver.getResource(resource.getPath() + PATH_JCR_CONTENT);
        if (childParameters != null) {
            String template = childParameters.getValueMap().get(NameConstants.NN_TEMPLATE, StringUtils.EMPTY);
            return template.equals(LIST_TEMPLATE_NAME);
        }
        return false;
    }

    /**
     * Checks whether the resource is a folder
     * @param resource {@code Resource} instance
     * @return True or false
     */
    private static boolean isFolder(Resource resource) {
        String primaryType = resource.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE, StringUtils.EMPTY);
        return primaryType.equals(JcrConstants.NT_FOLDER) || primaryType.equals(JcrResourceConstants.NT_SLING_FOLDER) ||
            primaryType.equals(JcrResourceConstants.NT_SLING_ORDERED_FOLDER);
    }

    /**
     * Checks whether the resource contains child resources which are either pages, or folders that can contain EToolbox
     * Lists
     * @param resource {@code Resource} instance
     * @return True or false
     */
    private static boolean containsResource(Resource resource) {
        return getChildrenStream(resource).anyMatch(item -> {
            String childPrimaryType = item.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE, StringUtils.EMPTY);
            return childPrimaryType.equals(NameConstants.NT_PAGE) || childPrimaryType.equals(JcrConstants.NT_FOLDER);
        });
    }

    /**
     * Checks whether the resource is a service/content node
     * @param resource {@code Resource} instance
     * @return True or false
     */
    private static boolean isServiceNode(Resource resource) {
        return resource.getName().startsWith(PREFIX_REP) || resource.getName().equals(JcrConstants.JCR_CONTENT);
    }

    private static Stream<Resource> getChildrenStream(Resource resource) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(resource.listChildren(), Spliterator.ORDERED), false);
    }

    /**
     * Gets {@link SlingScriptHelper} object associated with the current request
     * @param request {@code ServletRequest} instance
     * @return {@code SlingScriptHelper} object
     */
    private static SlingScriptHelper getScriptHelper(ServletRequest request) {
        SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        return bindings.getSling();
    }

    /**
     * Implements the {@code DataSource} pattern for displaying matched resources in pages
     */
    private static class PagingDataSource extends AbstractDataSource {
        private final List<Resource> resources;
        private final int offset;
        private final int limit;
        private final String itemResourceType;

        private PagingDataSource(List<Resource> resources, int offset, int limit, String itemResourceType) {
            this.resources = resources;
            this.offset = offset;
            this.limit = limit;
            this.itemResourceType = itemResourceType;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Iterator<Resource> iterator() {
            Iterator<Resource> it = new PagingIterator<>(resources.iterator(), offset, limit);

            return new TransformIterator(it, o -> new ResourceWrapper((Resource) o) {
                @Nonnull
                @Override
                public String getResourceType() {
                    return itemResourceType;
                }
            });
        }
    }
}
