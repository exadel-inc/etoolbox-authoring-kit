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
package com.exadel.aem.toolkit.core.relay.utils;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.relay.models.RelayResource;

public class ResourceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHelper.class);

    private static final String KEY_SUBSIDIARY = "subsidiary";

    private ResourceHelper() {}

    public static Resource getResource(
        ResourceResolver basicResolver,
        UnaryOperator<ResourceResolver> resolverModifier,
        String path,
        Function<Resource, ? extends Resource> onSuccess,
        Supplier<Resource> onFailure) {

        ResourceResolver effectiveResolver = resolverModifier.apply(basicResolver);
        Resource result = effectiveResolver.getResource(path);
        if (result == null) {
            LOG.warn("Could not resolve {} with user {}", path, effectiveResolver.getUserID());
            return onFailure.get();
        }
        if (!effectiveResolver.equals(basicResolver)) {
            // We have created another {@link ResourceResolver} via the {@code resolverModifier}. We cannot close it
            // in place -- instead, we need it to live as long as the resource(-s) we have resolved with it live.
            // To achieve that, we put it into the property map of the {@code basicResolver} so that it will be
            // automatically closed when the {@code basicResolver} is closed by Sling
            // See {@link ResourceResolver#getPropertyMap()}.
            ResourceResolver existingSubsidiary = (ResourceResolver) basicResolver.getPropertyMap().get(KEY_SUBSIDIARY);
            if (existingSubsidiary != null) {
                LOG.warn("A subsidiary resolver for {} will close", existingSubsidiary.getUserID());
                existingSubsidiary.close();
            }
            basicResolver.getPropertyMap().put(KEY_SUBSIDIARY, effectiveResolver);
        }
        LOG.debug("Resolved {} to {} with user {}", path, result.getPath(), effectiveResolver.getUserID());
        return onSuccess.apply(result);
    }

    @SuppressWarnings("unchecked")
    public static Resource getResource(
        ResourceProvider<?> resourceProvider,
        ResolveContext<?> resolveContext,
        String path,
        ResourceContext resourceContext,
        Resource parent) {
        if (resourceProvider == null || resolveContext == null) {
            reportMissingContext(path);
            return null;
        }
        LOG.debug("Falling back to parent resource provider for {}", path);
        return ((ResourceProvider<Void>)resourceProvider).getResource((ResolveContext<Void>) resolveContext, path, resourceContext, parent);
    }

    @SuppressWarnings("unchecked")
    public static Iterator<Resource> listChildren(
        ResourceProvider<?> resourceProvider,
        ResolveContext<?> resolveContext,
        Resource parent) {
        if (resourceProvider == null || resolveContext == null) {
            reportMissingContext(parent.getPath());
            return null;
        }
        return ((ResourceProvider<Void>)resourceProvider).listChildren((ResolveContext<Void>) resolveContext, parent);
    }

    public static Iterator<Resource> listChildren(Resource target, String path) {
        if (target instanceof RelayResource) {
            return target.listChildren();
        }
        return StreamSupport.stream(IteratorUtils.asIterable(target.listChildren()).spliterator(), false)
            .map(child -> new RelayResource(child, path + CoreConstants.SEPARATOR_SLASH + child.getName()))
            .map(Resource.class::cast)
            .iterator();
    }

    private static void reportMissingContext(String path) {
        LOG.warn("Missing resolution context for {}", path);
    }
}
