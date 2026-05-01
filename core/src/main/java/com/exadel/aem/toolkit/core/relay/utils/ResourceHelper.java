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

import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterators;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

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
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class ResourceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHelper.class);

    static final String KEY_SUBSIDIARY = "subsidiary";

    /**
     * Default (instantiation-blocking) constructor
     */
    private ResourceHelper() {}

    /**
     * Resolves a resource at the provided path using a potentially modified {@link ResourceResolver}. Returns
     * the resolved resource or {@code null} when the path cannot be resolved. Manages the lifecycle of any
     * subsidiary resolver created by the {@code resolverModifier}
     * @param basicResolver    The base {@link ResourceResolver} instance used for resolution
     * @param resolverModifier A {@code UnaryOperator} that optionally produces an alternative {@code ResourceResolver}
     *                         from the provided one
     * @param path             JCR path of the resource to resolve
     * @return A nullable {@link Resource} instance
     */
    public static Resource getResource(
        ResourceResolver basicResolver,
        UnaryOperator<ResourceResolver> resolverModifier,
        String path) {

        ResourceResolver effectiveResolver = resolverModifier.apply(basicResolver);
        Resource result = effectiveResolver.getResource(path);
        if (result == null) {
            LOG.debug("Could not resolve {} with user {}", path, effectiveResolver.getUserID());
            if (!effectiveResolver.equals(basicResolver)) {
                effectiveResolver.close();
            }
            return null;
        }
        if (!effectiveResolver.equals(basicResolver)) {
            // We have created another {@link ResourceResolver} via the {@code resolverModifier}. We cannot close it
            // in place - instead, we need it to live as long as the resource(-s) we have resolved with it live.
            // To achieve that, we put it into the property map of the {@code basicResolver} so that it will be
            // automatically closed when the {@code basicResolver} is closed by Sling.
            // A {@link SubsidiaryHolder} wrapper is used so that the swap-and-close of replaced resolvers is atomic.
            // See https://sling.apache.org/apidocs/sling12/org/apache/sling/api/resource/ResourceResolver.html#getPropertyMap
            Map<String, Object> propertyMap = basicResolver.getPropertyMap();
            propertyMap.compute(KEY_SUBSIDIARY, (key, existing) -> {
                if (existing instanceof ResolverHolder) {
                    ((ResolverHolder) existing).swap(effectiveResolver);
                    return existing;
                }
                return new ResolverHolder(effectiveResolver);
            });
        }
        LOG.debug("Resolved {} to {} with user {}", path, result.getPath(), effectiveResolver.getUserID());
        return result;
    }

    /**
     * Delegates resource resolution for the provided path to a parent {@link ResourceProvider} obtained from the given
     * {@link ResolveContext}. This is generally used as a fallback method for
     * {@link #getResource(ResourceResolver, UnaryOperator, String)}
     * @param resolveContext  {@link ResolveContext} from which the parent provider and parent context are extracted
     * @param path            JCR path of the resource to resolve
     * @param resourceContext {@link ResourceContext} for the resolution request
     * @param parent          Nullable parent {@link Resource}
     * @return A nullable {@link Resource} resolved by the parent provider, or {@code null} if the context or parent
     * provider is missing
     */
    @SuppressWarnings("unchecked")
    public static Resource getResource(
        ResolveContext<?> resolveContext,
        String path,
        ResourceContext resourceContext,
        Resource parent) {
        reportFallingBack(path);
        if (resolveContext == null) {
            reportMissingContext(path);
            return null;
        }
        ResourceProvider<?> parentResourceProvider = resolveContext.getParentResourceProvider();
        ResolveContext<?> parentContext = resolveContext.getParentResolveContext();
        if (parentResourceProvider == null || parentContext == null) {
            reportMissingContext(path);
            return null;
        }
        return ((ResourceProvider<Void>) parentResourceProvider).getResource((ResolveContext<Void>) parentContext, path, resourceContext, parent);
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
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(target.listChildren(), 0), false)
            .map(child -> new RelayResource(child, path + CoreConstants.SEPARATOR_SLASH + child.getName()))
            .map(Resource.class::cast)
            .iterator();
    }

    /**
     * Delegates child listing for the provided parent resource to a parent {@link ResourceProvider} obtained from the
     * given {@link ResolveContext}. This is generally used as a fallback method for listing children of a resource that
     * has not been relayed (is "original")
     * @param resolveContext {@link ResolveContext} from which the parent provider and parent context are extracted
     * @param parent         Parent {@link Resource} whose children to list
     * @return A nullable {@code Iterator} of child {@link Resource} instances, or {@code null} if the context or parent
     * provider is missing
     */
    @SuppressWarnings("unchecked")
    public static Iterator<Resource> listChildren(
        ResolveContext<?> resolveContext,
        Resource parent) {
        reportFallingBack(parent.getPath());
        if (resolveContext == null) {
            reportMissingContext(parent.getPath());
            return null;
        }
        ResourceProvider<?> parentResourceProvider = resolveContext.getParentResourceProvider();
        ResolveContext<?> parentContext = resolveContext.getParentResolveContext();
        if (parentResourceProvider == null || parentContext == null) {
            reportMissingContext(parent.getPath());
            return null;
        }
        return ((ResourceProvider<Void>) parentResourceProvider).listChildren((ResolveContext<Void>) parentContext, parent);
    }

    /**
     * Logs a warning when the resource provider or resolve context is missing for the given path
     * @param path JCR path that could not be resolved
     */
    private static void reportMissingContext(String path) {
        LOG.warn("Missing resolution context for {}", path);
    }

    /**
     * Logs a debug message when falling back to the parent resource provider for the given path
     * @param path JCR path that is being resolved
     */
    private static void reportFallingBack(String path) {
        LOG.debug("Falling back to parent resource provider for {}", path);
    }

    /* ------------------
       Subsidiary classes
       ------------------ */

    /**
     * A thread-safe {@link Closeable} wrapper around a subsidiary {@link ResourceResolver}. Stored in the property
     * map of a base resolver so that Sling automatically closes the held resolver when the base resolver is closed.
     * The {@link #swap(ResourceResolver)} method atomically replaces the held resolver, closing the previous one
     */
    static class ResolverHolder implements Closeable {

        private volatile ResourceResolver resolver;

        /**
         * Creates a new holder with the provided resolver
         * @param resolver Initial subsidiary {@link ResourceResolver}
         */
        ResolverHolder(ResourceResolver resolver) {
            this.resolver = resolver;
        }

        /**
         * Atomically replaces the held resolver with a new one, closing the previous resolver if present
         * @param newResolver The replacement {@link ResourceResolver}
         */
        synchronized void swap(ResourceResolver newResolver) {
            if (resolver != null) {
                LOG.warn("A subsidiary resolver for {} will close", resolver.getUserID());
                resolver.close();
            }
            resolver = newResolver;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void close() {
            if (resolver != null) {
                resolver.close();
                resolver = null;
            }
        }
    }
}
