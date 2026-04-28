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
package com.exadel.aem.toolkit.core.relay.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ExternalResourceChangeListener;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.spi.resource.provider.ProviderContext;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.relay.models.RelayResource;
import com.exadel.aem.toolkit.core.relay.utils.PathHelper;
import com.exadel.aem.toolkit.core.relay.utils.ResourceHelper;
import com.exadel.aem.toolkit.core.utils.ResolverUtil;

/**
 * A Sling {@link ResourceProvider} and {@link ResourceChangeListener} that transparently relays resource
 * resolution and change notifications from a source JCR path to a configurable target path. Optionally maps
 * the resource resolver user identity to a different user or service account
 */
class RelayProvider extends ResourceProvider<Void> implements ResourceChangeListener, ExternalResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(RelayProvider.class);

    private ResourceResolverFactory resolverFactory;
    private PathSampler pathSampler;
    private String source;
    private String target;
    private Map<String, String> userMappings;

    /**
     * Default (instantiation-blocking) constructor
     */
    private RelayProvider() {
    }

    /* ------------------------
       ResourceProvider members
       ------------------------ */

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(
        @Nonnull ResolveContext<Void> context,
        @Nonnull String path,
        @Nonnull ResourceContext resourceContext,
        @Nullable Resource parent) {

        if (!StringUtils.equals(path, source) && !StringUtils.startsWith(path, source + CoreConstants.SEPARATOR_SLASH)) {
            return null;
        }
        String targetPath = target + StringUtils.substring(path, source.length());
        return ResourceHelper.getResource(
            context.getResourceResolver(),
            this::getMappedResourceResolver,
            targetPath,
            resource -> resource != null ? new RelayResource(resource, path) : null,
            () -> ResourceHelper.getResource(
                context.getParentResourceProvider(),
                context.getParentResolveContext(),
                path,
                resourceContext,
                parent));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Iterator<Resource> listChildren(@Nonnull ResolveContext<Void> context, @Nonnull Resource parent) {
        String path = parent.getPath();
        String targetPath = PathHelper.replace(path, source, target);
        Resource targetResource = ResourceHelper.getResource(
            context.getResourceResolver(),
            this::getMappedResourceResolver,
            targetPath,
            resource -> resource != null ? new RelayResource(resource, path) : null,
            () -> ResourceHelper.getResource(
                context.getParentResourceProvider(),
                context.getParentResolveContext(),
                path,
                null,
                parent));
        if (targetResource == null) {
            return null;
        } else if (targetResource.getPath().equals(parent.getPath())) {
            // We have fallen back to an "original" resource, so we should iterate through it without any mapping
            return ResourceHelper.listChildren(
                context.getParentResourceProvider(),
                context.getParentResolveContext(),
                parent);
        }
        return ResourceHelper.listChildren(targetResource, parent.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(@Nonnull ProviderContext providerContext) {
        LOG.info("Relay provider for {} -> {} is starting", source, target);
        super.start(providerContext);
        try (ResourceResolver resolver = ResolverUtil.newResolver(resolverFactory)) {
            Session session = resolver.adaptTo(Session.class);
            Collection<ResourceChange> changes = pathSampler.createChanges(session);
            if (!changes.isEmpty()) {
                providerContext.getObservationReporter().reportChanges(changes, false);
            }
        } catch (LoginException e) {
            LOG.error("Failed to create a JCR session for script sampling at {}", target, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        LOG.info("Relay provider for {} -> {} is stopping", source, target);
        if (getProviderContext() != null && !pathSampler.isEmpty()) {
            Collection<ResourceChange> declaredChanges = pathSampler.createChanges();
            if (!declaredChanges.isEmpty()) {
                getProviderContext().getObservationReporter().reportChanges(declaredChanges, false);
            }
        }
        super.stop();
    }

    /* ------------------------------
       ResourceChangeListener members
       ------------------------------ */

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChange(@Nonnull List<ResourceChange> changes) {
        if (getProviderContext() == null) {
            return;
        }
        List<ResourceChange> mappedChanges = changes.stream()
            .map(change -> new ResourceChange(
                change.getType(),
                PathHelper.replace(change.getPath(), target, source),
                change.isExternal()))
            .collect(Collectors.toList());
        getProviderContext().getObservationReporter().reportChanges(mappedChanges, false);
    }

    /* ---------------
       Service methods
       --------------- */

    /**
     * Returns a {@link ResourceResolver} for the user identity mapped to the provided resolver's user ID.
     * If no mapping is configured for the current user, returns the original resolver unchanged
     * @param resolver {@code ResourceResolver} whose user ID is used as the mapping key
     * @return A non-null {@code ResourceResolver} instance; may be the original if no mapping applies
     */
    private ResourceResolver getMappedResourceResolver(ResourceResolver resolver) {
        String mappedId = userMappings.get(resolver.getUserID());
        if (mappedId != null && !mappedId.equals(resolver.getUserID())) {
            Map<String, Object> authInfo = new HashMap<>();
            try {
                if (StringUtils.contains(mappedId, CoreConstants.SEPARATOR_COLON)) {
                    authInfo.put(ResourceResolverFactory.USER, StringUtils.substringBefore(mappedId, CoreConstants.SEPARATOR_COLON));
                    authInfo.put(ResourceResolverFactory.PASSWORD, StringUtils.substringAfter(mappedId, CoreConstants.SEPARATOR_COLON).toCharArray());
                    return resolverFactory.getResourceResolver(authInfo);
                }
                authInfo.put(ResourceResolverFactory.SUBSERVICE, mappedId);
                return resolverFactory.getServiceResourceResolver(authInfo);
            } catch (LoginException e) {
                LOG.error(
                    "Failed to create a resource resolver for user {}",
                    authInfo.containsKey(ResourceResolverFactory.USER)
                        ? authInfo.get(ResourceResolverFactory.USER)
                        : authInfo.get(ResourceResolverFactory.SUBSERVICE),
                    e);
            }
        }
        return resolver;
    }

    /* -------------
       Factory logic
       ------------- */

    /**
     * Creates a new {@link Builder} for configuring and instantiating a {@link RelayProvider}
     * @return A new {@code Builder} instance
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs {@link RelayProvider} instances with the required configuration
     */
    @SuppressWarnings({"UnusedReturnValue"})
    static class Builder {

        private List<String> reportedPaths;
        private ResourceResolverFactory resolverFactory;
        private String source;
        private String target;
        private Map<String, String> userMappings;

        /**
         * Default (instantiation-restricting) constructor
         */
        private Builder() {
        }

        /**
         * Adds a JCR path to report as changed when the provider starts or stops
         * @param value JCR path to include in change reports
         * @return This builder
         */
        Builder reportedPath(String value) {
            if (reportedPaths == null) {
                reportedPaths = new ArrayList<>();
            }
            reportedPaths.add(value);
            return this;
        }

        /**
         * Sets the {@link ResourceResolverFactory} used to create user-mapped resolvers
         * @param value {@link ResourceResolverFactory} instance
         * @return This builder
         */
        Builder resolverFactory(ResourceResolverFactory value) {
            resolverFactory = value;
            return this;
        }

        /**
         * Sets the JCR source path handled by this provider
         * @param value Source JCR path
         * @return This builder
         */
        Builder source(String value) {
            this.source = value;
            return this;
        }

        /**
         * Sets the JCR target path to which resource resolution is delegated
         * @param value Target JCR path
         * @return This builder
         */
        Builder target(String value) {
            this.target = value;
            return this;
        }

        /**
         * Adds a user identity mapping from a source user ID to a target user or subservice
         * @param source Source user ID
         * @param value  Target user ID, {@code "user:password"} credential string, or service subservice name
         * @return This builder
         */
        Builder userMapping(String source, String value) {
            if (userMappings == null) {
                userMappings = new HashMap<>();
            }
            userMappings.put(source, value);
            return this;
        }

        /**
         * Creates a configured {@link RelayProvider} from the current builder state
         * @return A new {@link RelayProvider} instance
         */
        RelayProvider build() {
            RelayProvider result = new RelayProvider();
            result.pathSampler = new PathSampler(reportedPaths);
            result.resolverFactory = resolverFactory;
            result.source = source;
            result.target = target;
            result.userMappings = userMappings != null ? userMappings : Collections.emptyMap();
            return result;
        }
    }
}

