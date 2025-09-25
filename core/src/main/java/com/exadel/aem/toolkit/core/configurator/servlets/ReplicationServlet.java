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
import java.util.Objects;
import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;

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
        "sling.servlet.resourceTypes=" + "/bin/etoolbox/authoring-kit/config",
        "sling.servlet.selectors=" + "publish",
        "sling.servlet.selectors=" + "unpublish",
        "sling.servlet.methods=" + HttpConstants.METHOD_POST,
    }
)
public class ReplicationServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicationServlet.class);

    @Reference
    private transient Replicator replicator;

    /**
     * Handles HTTP POST requests. Depending on the selector, it either publishes or unpublishes the configuration
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws IOException If an error occurs during publishing or unpublishing
     */
    @Override
    protected void doPost(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws IOException {
        if ("publish".equals(request.getRequestPathInfo().getSelectorString())) {
            try {
                replicator.replicate(
                    Objects.requireNonNull(request.getResourceResolver().adaptTo(Session.class)),
                    ReplicationActionType.ACTIVATE,
                    request.getResource().getPath());
            } catch (ReplicationException e) {
                LOG.error("Could not publish configuration {}", request.getResource().getName(), e);
                response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else if ("unpublish".equals(request.getRequestPathInfo().getSelectorString())) {
            try {
                replicator.replicate(
                    Objects.requireNonNull(request.getResourceResolver().adaptTo(Session.class)),
                    ReplicationActionType.DEACTIVATE,
                    request.getResource().getPath());
            } catch (ReplicationException e) {
                LOG.error("Could not unpublish configuration {}", request.getResource().getName(), e);
                response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            cleanUpEmptyUnpublishedNode(request.getResourceResolver(), request.getResource());
        } else {
            response.sendError(SlingHttpServletResponse.SC_BAD_REQUEST, "Invalid selector");
        }
    }

    /**
     * Removes the target resource if it has no children after unpublishing
     * @param resolver The {@link ResourceResolver} instance
     * @param resource The resource to remove if empty
     */
    private void cleanUpEmptyUnpublishedNode(ResourceResolver resolver, Resource resource) {
        if (resource.hasChildren()) {
            return;
        }
        try {
            resolver.delete(resource);
            resolver.commit();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }
}
