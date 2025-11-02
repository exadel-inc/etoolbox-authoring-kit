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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.jcr.Session;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.engine.EngineConstants;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationContentFilterFactory;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.ReplicationPathTransformer;
import com.day.cq.replication.Replicator;

import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;
import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigAccess;
import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener;

/**
 * Implements a servlet that handles publishing and unpublishing of configurations managed by the
 * {@code EToolbox Configurator} interface
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@Component(
    service = Servlet.class,
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    property = {
        "sling.servlet.resourceTypes=" + ConfiguratorConstants.RESOURCE_TYPE_CONFIG,
        "sling.servlet.selectors=" + ReplicationServlet.SELECTOR_PUBLISH,
        "sling.servlet.selectors=" + ReplicationServlet.SELECTOR_UNPUBLISH,
        "sling.servlet.methods=" + HttpConstants.METHOD_POST,
    }
)
public class ReplicationServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicationServlet.class);

    static final String SELECTOR_PUBLISH = "publish";
    static final String SELECTOR_UNPUBLISH = "unpublish";

//    private static final int PROPERTY_CACHE_TTL = 10_000;

    private static final String EXCEPTION_COULD_NOT_PUBLISH = "Could not publish configuration {}";

    @Reference
    private transient ConfigChangeListener configChangeListener;

    @Reference
    private transient Replicator replicator;

//    private ExpiringCache<ReplicationAction, List<String>> propertyCache;

//    private ServiceRegistration<Preprocessor> preprocessorRegistration;
//    Preprocessor preprocessor;

    private ServiceRegistration<Filter> replicationContextRegistration;
    ReplicationContext replicationContext;

    private ServiceRegistration<ReplicationPathTransformer> pathTransformerRegistration;
    PropertyAwarePathTransformer pathTransformer;

    private ServiceRegistration<ReplicationContentFilterFactory> contentFilterFactoryRegistration;
    PropertyAwareFilterFactory contentFilterFactory;

    /**
     * Activates the servlet component and registers subsidiary services
     * @param context The OSGi {@link BundleContext} instance
     */
    @Activate
    private void activate(BundleContext context) {
        doDeactivate();
        LOG.info("Configuration replication is {}", configChangeListener.isEnabled() ? "enabled" : "disabled");

        if (!configChangeListener.isEnabled()) {
            return;
        }

        replicationContext = new ReplicationContext();
        Dictionary<String, Object> replicationContextProps = new Hashtable<>();
        replicationContextProps.put(EngineConstants.SLING_FILTER_SCOPE, EngineConstants.FILTER_SCOPE_REQUEST);
        replicationContextProps.put("sling.filter.resourceTypes", ConfiguratorConstants.RESOURCE_TYPE_CONFIG);
        replicationContextProps.put("sling.filter.selectors", "publish");
        replicationContextRegistration = context.registerService(Filter.class,
            replicationContext,
            replicationContextProps);

        pathTransformer = new PropertyAwarePathTransformer();
        pathTransformerRegistration = context.registerService(ReplicationPathTransformer.class,
            pathTransformer,
            null);

        contentFilterFactory = new PropertyAwareFilterFactory();
        contentFilterFactoryRegistration = context.registerService(
            ReplicationContentFilterFactory.class,
            contentFilterFactory,
            null);
    }

    /**
     * Deactivates the servlet component and unregisters subsidiary services
     */
    @Deactivate
    private void deactivate() {
        LOG.info("Configuration replication is shutting down");
        doDeactivate();
    }

    /**
     * Unregisters subsidiary services
     */
    private void doDeactivate() {
//        if (preprocessorRegistration != null) {
//            preprocessorRegistration.unregister();
//            preprocessorRegistration = null;
//        }
//        if (propertyCache != null) {
//            propertyCache.close();
//            propertyCache = null;
//        }
        if (contentFilterFactoryRegistration != null) {
            contentFilterFactoryRegistration.unregister();
            contentFilterFactoryRegistration = null;
        }
        if (pathTransformerRegistration != null) {
            pathTransformerRegistration.unregister();
            pathTransformerRegistration = null;
        }
        if (replicationContextRegistration != null) {
            replicationContextRegistration.unregister();
            replicationContextRegistration = null;
        }
    }

    /**
     * Handles HTTP POST requests. Depending on the selector, it either publishes or unpublishes the configuration
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws IOException If an error occurs during publishing or unpublishing
     */
    @Override
    protected void doPost(
        @NotNull SlingHttpServletRequest request,
        @NotNull SlingHttpServletResponse response) throws IOException {

        if (!configChangeListener.isEnabled()) {
            sendError(response, SlingHttpServletResponse.SC_SERVICE_UNAVAILABLE, ConfigAccess.DISABLED.getError());
            return;
        }

        String selector = request.getRequestPathInfo().getSelectorString();
        if (SELECTOR_PUBLISH.equals(selector)) {
            doPublish(request, response);
        } else if (SELECTOR_UNPUBLISH.equals(selector)) {
            doUnpublish(request, response);
        } else {
            sendError(response, SlingHttpServletResponse.SC_BAD_REQUEST, "Invalid selector");
        }
    }

    /**
     * Publishes the configuration represented by the target resource
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws IOException If an error occurs during publishing
     */
    private void doPublish(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String properties = request.getParameter("properties");
        if (StringUtils.isNotBlank(properties) && !"all".equals(properties)) {
            List<String> propertyList = Stream.of(properties.split("[,;]"))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
            ReplicationContext.setProperties(propertyList);
        }
        try {
            replicator.replicate(
                Objects.requireNonNull(request.getResourceResolver().adaptTo(Session.class)),
                ReplicationActionType.ACTIVATE,
                request.getResource().getPath());
        } catch (ReplicationException e) {
            LOG.error(EXCEPTION_COULD_NOT_PUBLISH, request.getResource().getName(), e);
            sendError(response, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        /*
        PropertyAwareReplicationOptions options = new PropertyAwareReplicationOptions(optionList);
        try {
            replicator.replicate(
                Objects.requireNonNull(request.getResourceResolver().adaptTo(Session.class)),
                ReplicationActionType.ACTIVATE,
                request.getResource().getPath());
        } catch (ReplicationException e) {
            LOG.error(EXCEPTION_COULD_NOT_PUBLISH, request.getResource().getName(), e);
            sendError(response, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            propertyCache.remove(options.getAction());
        }*/

    }

    /**
     * Unpublishes the configuration represented by the target resource
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws IOException If an error occurs during unpublishing
     */
    private void doUnpublish(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try {
            replicator.replicate(
                Objects.requireNonNull(request.getResourceResolver().adaptTo(Session.class)),
                ReplicationActionType.DEACTIVATE,
                request.getResource().getPath(),
                new ReplicationOptions());
        } catch (ReplicationException | NullPointerException e) {
            LOG.error("Could not unpublish configuration {}", request.getResource().getName(), e);
            sendError(response, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }
        cleanUpEmptyUnpublishedNode(request.getResourceResolver(), request.getResource());
    }

    /**
     * Removes the target resource if it has no children after unpublishing
     * @param resolver The {@link ResourceResolver} instance
     * @param resource The resource to remove if empty
     */
    private static void cleanUpEmptyUnpublishedNode(ResourceResolver resolver, Resource resource) {
        if (resource.hasChildren()) {
            return;
        }
        try {
            resolver.delete(resource);
            resolver.commit();
        } catch (Exception e) {
            LOG.error("Could not clean up configuration node {}", resource.getPath(), e);
        }
    }

    /**
     * Sends an error response with a specific status and message
     * @param response The HTTP response
     * @param status   The HTTP status code
     * @param message  The message to include in the response body
     * @throws IOException If an I/O error occurs
     */
    private static void sendError(SlingHttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().println(message);
        response.getWriter().flush();
    }
}
