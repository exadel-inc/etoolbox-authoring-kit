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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.relay.models.ChangeSample;
import com.exadel.aem.toolkit.core.relay.models.RelayMapping;
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
    private PathSampler sampler;
    private String source;
    private String target;
    private Map<String, String> userMappings;

    /**
     * Default (instantiation-restricting) constructor
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
        Resource resolved = ResourceHelper.getResource(
            context.getResourceResolver(),
            this::getMappedResourceResolver,
            targetPath);
        if (resolved != null) {
            return new RelayResource(resolved, path);
        }
        return ResourceHelper.getResource(context, path, resourceContext, parent);
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
            targetPath);
        if (targetResource != null) {
            targetResource = new RelayResource(targetResource, path);
        } else {
            targetResource = ResourceHelper.getResource(context, path, null, parent);
        }
        if (targetResource == null) {
            return null;
        } else if (targetResource.getPath().equals(parent.getPath())) {
            // We have fallen back to an "original" resource, so we should iterate through it without any mapping
            return ResourceHelper.listChildren(context, parent);
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
        LOG.info("Relay provider for {} -> {} is stopping", source, target);
        if (getProviderContext() != null && !sampler.isEmpty()) {
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
            try {
                return ResolverUtil.newResolver(resolverFactory, mappedId);
            } catch (LoginException e) {
                LOG.error(
                    "Failed to create a resource resolver for user {}",
                    StringUtils.contains(mappedId, CoreConstants.SEPARATOR_COLON)
                        ? StringUtils.substringBefore(mappedId, CoreConstants.SEPARATOR_COLON)
                        : mappedId,
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

        private Collection<ChangeSample> announcements;
        private ResourceResolverFactory resolverFactory;
        private String source;
        private String target;
        private Collection<RelayMapping> userMappings;

        /**
         * Default (instantiation-restricting) constructor
         */
        private Builder() {
        }

        /**
         * Sets the collection of {@link ChangeSample} instances defining the paths to report as changed when the
         * provider is enabled or disabled
         * @param value Collection of {@code  ChangeAnnouncement} instances the provider is enabled or disabled
         * @return This builder
         */
        Builder samples(Collection<ChangeSample> value) {
            announcements = value;
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
         * Sets the collection of {@link RelayMapping} instances defining the user identity mappings to apply when
         * creating mapped resource resolvers
         * @param value Collection of {@code RelayMapping} instances defining user identity mappings
         * @return This builder
         */
        Builder userMappings(Collection<RelayMapping> value) {
            userMappings = value;
            return this;
        }

        /**
         * Creates a configured {@link RelayProvider} from the current builder state
         * @return A new {@link RelayProvider} instance
         */
        RelayProvider build() {
            RelayProvider result = new RelayProvider();
            result.sampler = PathSampler
                .builder()
                .source(source)
                .target(target)
                .resolverFactory(resolverFactory)
                .samples(announcements)
                .build();
            result.resolverFactory = resolverFactory;
            result.source = source;
            result.target = target;
            result.userMappings = userMappings == null
                ? Collections.emptyMap()
                : userMappings.stream().filter(RelayMapping::isValid).collect(Collectors.toMap(RelayMapping::getFrom, RelayMapping::getTo));
            return result;
        }
    }
}

