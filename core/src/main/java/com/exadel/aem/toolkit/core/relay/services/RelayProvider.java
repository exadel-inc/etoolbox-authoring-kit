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

import org.apache.sling.api.resource.Resource;
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

    /* -----------------
       Fields assignment
       ----------------- */

    /**
     * Updates the configuration of this provider based on the given relay model. This method is called when the OSGi
     * component is activated or its configuration is updated to apply the new configuration to this provider instance
     * @param model The {@link RelayInfo} model containing the new configuration for this provider
     */
    synchronized void update(RelayInfo model) {
        relay = model;
        sampler = PathSampler
            .builder()
            .resolverFactory(resolverFactory)
            .source(model.getSource())
            .target(model.getTarget())
            .samples(model.getChangeSamples())
            .build();
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
        if (!RelayPathHelper.isSamePathOrSubpath(path, localRelay.getSource())) {
            return null;
        }
        String targetPath = RelayPathHelper.replace(path, localRelay.getSource(), localRelay.getTarget());
        // Break the re-entry cycle: if targetPath is still under source, resolve via parent provider
        if (RelayPathHelper.isSamePathOrSubpath(targetPath, localRelay.getSource())) {
            Resource resolved = RelayResourceHelper.getResource(context, targetPath, resourceContext, null);
            return resolved != null ? new RelayResource(resolved, path) : null;
        }
        Resource resolved = RelayResourceHelper.getResource(
            context.getResourceResolver(),
            resolverFactory,
            localRelay.getUserMapping(context.getResourceResolver().getUserID()),
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
        // Re-entry guard: same as getResource() — if targetPath is still under source,
        // resolve via the parent provider to break the cycle
        if (RelayPathHelper.isSamePathOrSubpath(targetPath, localRelay.getSource())) {
            Resource resolved = RelayResourceHelper.getResource(context, targetPath, ResourceContext.EMPTY_CONTEXT, parent);
            if (resolved == null) {
                return null;
            }
            return RelayResourceHelper.listChildren(resolved, path);
        }

        Resource resolved = RelayResourceHelper.getResource(
            context.getResourceResolver(),
            resolverFactory,
            localRelay.getUserMapping(context.getResourceResolver().getUserID()),
            targetPath);
        if (resolved != null) {
            resolved = new RelayResource(resolved, path);
        } else {
            resolved = RelayResourceHelper.getResource(context, path, ResourceContext.EMPTY_CONTEXT, parent);
        }
        if (resolved == null) {
            return null;
        } else if (!(resolved instanceof RelayResource)) {
            // We have fallen back to an "original" resource, so we should iterate through it without any mapping
            return RelayResourceHelper.listChildren(context, resolved);
        }
        // This method call exerts the path mapping logic for the children of the target resource
        return RelayResourceHelper.listChildren(resolved, parent.getPath());
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
        announce(sampler, providerContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        RelayInfo localRelay = relay;   // Use a local copy to avoid potential race conditions with the update() method
        LOG.info("Relay provider for {} -> {} is stopping", localRelay.getSource(), localRelay.getTarget());
        if (sampler != null && getProviderContext() != null) {
            announce(sampler, getProviderContext());
        }
        sampler = null;
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

    /* -------------
       Announcements
       ------------- */

    /**
     * Announces the configured change samples as resource changes, generally to indicate that the relay is changing
     * state and trigger any necessary updates in the system
     */
    void announce() {
        PathSampler localSampler = sampler;
        if (localSampler != null && getProviderContext() != null) {
            announce(localSampler, getProviderContext());
        }
    }

    /**
     * Generates resource changes based on the configured change samples and reports them through the observation
     * reporter
     * @param sampler         The {@link PathSampler} instance used to generate the resource changes to report
     * @param providerContext The {@link ProviderContext} instance used to access the observation reporter for reporting
     *                        the generated changes
     */
    private static void announce(PathSampler sampler, ProviderContext providerContext) {
        Collection<ResourceChange> changes = sampler.createChanges();
        if (!changes.isEmpty()) {
            providerContext.getObservationReporter().reportChanges(changes, false);
        }
    }
}

