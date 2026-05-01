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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

import com.exadel.aem.toolkit.core.relay.models.RelayInfo;
import com.exadel.aem.toolkit.core.relay.models.RelayResource;
import com.exadel.aem.toolkit.core.relay.utils.RelayPathHelper;
import com.exadel.aem.toolkit.core.relay.utils.RelayResourceHelper;
import com.exadel.aem.toolkit.core.utils.ResolverUtil;

/**
 * A Sling {@link ResourceProvider} and {@link ResourceChangeListener} that transparently relays resource
 * resolution and change notifications from a source JCR path to a configurable target path. Optionally maps
 * the resource resolver user identity to a different user or service account
 */
class RelayProvider extends ResourceProvider<Void> implements ResourceChangeListener, ExternalResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(RelayProvider.class);

    private final ResourceResolverFactory resolverFactory;
    private volatile RelayInfo relay;
    private volatile PathSampler sampler;

    /**
     * Creates a new {@code RelayProvider} instance for the given relay model
     * @param resolverFactory The {@link ResourceResolverFactory} instance used to create mapped resource resolvers for
     *                        change sampling and user identity mapping
     * @param relay           The {@link RelayInfo} model containing the configuration for this provider
     */
    RelayProvider(ResourceResolverFactory resolverFactory, RelayInfo relay) {
        this.resolverFactory = resolverFactory;
        update(relay);
    }

    /**
     * Updates the configuration of this provider based on the given relay model. This method is called when the OSGi
     * component is activated or its configuration is updated to apply the new configuration to this provider instance
     * @param model The {@link RelayInfo} model containing the new configuration for this provider
     */
    void update(RelayInfo model) {
        this.relay = model;
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

        RelayInfo localRelay = relay;   // Use a local copy to avoid potential race conditions with the update() method
        if (!RelayPathHelper.isSubpath(path, localRelay.getSource())) {
            return null;
        }
        String targetPath = localRelay.getTarget() + StringUtils.substring(path, localRelay.getSource().length());
        Resource resolved = RelayResourceHelper.getResource(
            context.getResourceResolver(),
            this::getMappedResourceResolver,
            targetPath);
        if (resolved != null) {
            return new RelayResource(resolved, path);
        }
        return RelayResourceHelper.getResource(context, path, resourceContext, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Iterator<Resource> listChildren(@Nonnull ResolveContext<Void> context, @Nonnull Resource parent) {

        RelayInfo localRelay = relay;   // Use a local copy to avoid potential race conditions with the update() method
        String path = parent.getPath();
        String targetPath = RelayPathHelper.replace(path, localRelay.getSource(), localRelay.getTarget());
        Resource targetResource = RelayResourceHelper.getResource(
            context.getResourceResolver(),
            this::getMappedResourceResolver,
            targetPath);
        if (targetResource != null) {
            targetResource = new RelayResource(targetResource, path);
        } else {
            targetResource = RelayResourceHelper.getResource(context, path, null, parent);
        }
        if (targetResource == null) {
            return null;
        } else if (!(targetResource instanceof RelayResource)) {
            // We have fallen back to an "original" resource, so we should iterate through it without any mapping
            return RelayResourceHelper.listChildren(context, parent);
        }
        return RelayResourceHelper.listChildren(targetResource, parent.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(@Nonnull ProviderContext providerContext) {
        RelayInfo localRelay = relay;   // Use a local copy to avoid potential race conditions with the update() method
        LOG.info("Relay provider for {} -> {} is starting", localRelay.getSource(), localRelay.getTarget());
        super.start(providerContext);
        sampler = PathSampler
            .builder()
            .resolverFactory(resolverFactory)
            .source(localRelay.getSource())
            .target(localRelay.getTarget())
            .samples(localRelay.getChangeSamples())
            .build();

        Collection<ResourceChange> changes = sampler.createChanges();
        if (!changes.isEmpty()) {
            providerContext.getObservationReporter().reportChanges(changes, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        RelayInfo localRelay = relay;   // Use a local copy to avoid potential race conditions with the update() method
        LOG.info("Relay provider for {} -> {} is stopping", localRelay.getSource(), localRelay.getTarget());
        if (getProviderContext() != null && sampler != null) {
            Collection<ResourceChange> declaredChanges = sampler.createChanges();
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
        RelayInfo localRelay = relay;   // Use a local copy to avoid potential race conditions with the update() method
        List<ResourceChange> mappedChanges = changes.stream()
            .map(change -> new ResourceChange(
                change.getType(),
                RelayPathHelper.replace(change.getPath(), localRelay.getTarget(), localRelay.getSource()),
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
        String mappedId = relay.getUserMapping(resolver.getUserID());
        if (mappedId != null && !mappedId.equals(resolver.getUserID())) {
            try {
                return ResolverUtil.newResolver(resolverFactory, mappedId);
            } catch (LoginException e) {
                LOG.error("Failed to create a resource resolver for {}", mappedId, e);
            }
        }
        return resolver;
    }
}

