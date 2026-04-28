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

/**
 * Provides utility methods for resolving and listing Sling resources within the relay infrastructure
 */
public class ResourceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHelper.class);

    private static final String KEY_SUBSIDIARY = "subsidiary";

    /** Default (instantiation-blocking) constructor */
    private ResourceHelper() {}

    /**
     * Resolves a resource at the provided path using a potentially modified {@link ResourceResolver}. Falls back to
     * the {@code onFailure} supplier when the path cannot be resolved
     * @param basicResolver    The base {@link ResourceResolver} instance used for resolution
     * @param resolverModifier A {@code UnaryOperator} that optionally produces an alternative {@code ResourceResolver}
     *                         from the provided one
     * @param path             JCR path of the resource to resolve
     * @param onSuccess        A {@code Function} applied to the resolved resource to produce the final result
     * @param onFailure        A {@code Supplier} invoked when the resource cannot be resolved
     * @return A nullable {@link Resource} instance
     */
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

    /**
     * Delegates resource resolution for the provided path to a parent {@link ResourceProvider}
     * @param resourceProvider Parent {@code ResourceProvider} instance
     * @param resolveContext   {@link ResolveContext} associated with the current resolution
     * @param path             JCR path of the resource to resolve
     * @param resourceContext  {@link ResourceContext} for the resolution request
     * @param parent           Nullable parent {@link Resource}
     * @return A nullable {@link Resource} resolved by the parent provider, or {@code null} if the provider or
     * context is missing
     */
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

    /**
     * Delegates child listing for the provided parent resource to a parent {@link ResourceProvider}
     * @param resourceProvider Parent {@code ResourceProvider} instance
     * @param resolveContext   {@link ResolveContext} associated with the current child listing
     * @param parent           Parent {@link Resource} whose children to list
     * @return A nullable {@code Iterator} of child {@link Resource} instances, or {@code null} if the provider or
     * context is missing
     */
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

    /**
     * Lists children of the provided target resource, wrapping each in a {@link RelayResource} with a path
     * relative to the given path prefix
     * @param target {@link Resource} whose children to list
     * @param path   JCR path under which the children should be exposed
     * @return A non-null {@code Iterator} of {@link Resource} instances
     */
    public static Iterator<Resource> listChildren(Resource target, String path) {
        if (target instanceof RelayResource) {
            return target.listChildren();
        }
        return StreamSupport.stream(IteratorUtils.asIterable(target.listChildren()).spliterator(), false)
            .map(child -> new RelayResource(child, path + CoreConstants.SEPARATOR_SLASH + child.getName()))
            .map(Resource.class::cast)
            .iterator();
    }

    /**
     * Logs a warning when the resource provider or resolve context is missing for the given path
     * @param path JCR path that could not be resolved
     */
    private static void reportMissingContext(String path) {
        LOG.warn("Missing resolution context for {}", path);
    }
}
