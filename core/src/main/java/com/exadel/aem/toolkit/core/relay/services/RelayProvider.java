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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

class RelayProvider extends ResourceProvider<Void> implements ResourceChangeListener, ExternalResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(RelayProvider.class);

    private ResourceResolverFactory resolverFactory;
    private ScriptSampler scriptSampler;
    private String source;
    private String target;
    private Map<String, String> userMappings;

    private RelayProvider() {
    }

    /* ------------------------
       ResourceProvider members
       ------------------------ */

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

    @Override
    public void start(@Nonnull ProviderContext providerContext) {
        LOG.info("Relay provider for {} -> {} is starting", source, target);
        super.start(providerContext);
        if (!StringUtils.startsWithAny(source, "/apps", "/libs")
            || !StringUtils.startsWith(target, "/content")) {
            return;
        }
        try (ResourceResolver resolver = ResolverUtil.newResolver(resolverFactory)) {
            Session session = resolver.adaptTo(Session.class);
            scriptSampler = ScriptSampler.from(Objects.requireNonNull(session), source, target);
            Collection<ResourceChange> declaredChanges = scriptSampler.generateChanges();
            if (!declaredChanges.isEmpty()) {
                providerContext.getObservationReporter().reportChanges(declaredChanges, false);
            }
        } catch (LoginException e) {
            LOG.error("Failed to create a JCR session for script sampling at {}", target, e);
        }
    }

    @Override
    public void stop() {
        LOG.info("Relay provider for {} -> {} is stopping", source, target);
        if (getProviderContext() != null && scriptSampler != null) {
            Collection<ResourceChange> declaredChanges = scriptSampler.generateChanges();
            if (!declaredChanges.isEmpty()) {
                getProviderContext().getObservationReporter().reportChanges(declaredChanges, false);
            }
        }
        super.stop();
    }

    /* ------------------------------
       ResourceChangeListener members
       ------------------------------ */

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

    static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unused")
    static class Builder {

        private ResourceResolverFactory resolverFactory;
        private String source;
        private String target;
        private Map<String, String> userMappings;

        private Builder() {
        }

        Builder resolverFactory(ResourceResolverFactory value) {
            resolverFactory = value;
            return this;
        }

        Builder source(String value) {
            this.source = value;
            return this;
        }

        Builder target(String value) {
            this.target = value;
            return this;
        }

        Builder userMappings(Map<String, String> value) {
            this.userMappings = value;
            return this;
        }

        RelayProvider build() {
            RelayProvider result = new RelayProvider();
            result.resolverFactory = resolverFactory;
            result.source = source;
            result.target = target;
            result.userMappings = userMappings;
            return result;
        }
    }
}

