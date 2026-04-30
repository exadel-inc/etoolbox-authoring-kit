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
package com.exadel.aem.toolkit.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Provides utility methods for creating {@link ResourceResolver} instances
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own code</p>
 */
public class ResolverUtil {

    private static final String SERVICE_USER_ID = "eak-service";

    /**
     * Default (instantiation-restricting) constructor
     */
    private ResolverUtil() {
    }

    /**
     * Creates a new {@link ResourceResolver} instance using the provided resource resolver factory
     * @param factory  The {@code ResourceResolverFactory} instance
     * @return New instance of {@code ResourceResolver}
     * @throws LoginException If the resolver cannot be created
     */
    @Nonnull
    public static ResourceResolver newResolver(@Nonnull ResourceResolverFactory factory) throws LoginException {
        return factory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER_ID)
        );
    }

    /**
     * Creates a new {@link ResourceResolver} instance for the given user identifier using the provided resource
     * resolver factory. The user identifier may be in the format of {@code username@bundleId} or
     * {@code login:password}
     * @param factory The {@code ResourceResolverFactory} instance
     * @param user    The user identifier string
     * @return New instance of {@code ResourceResolver}
     * @throws LoginException If the resolver cannot be created
     */
    @Nonnull
    public static ResourceResolver newResolver(
        @Nonnull ResourceResolverFactory factory,
        String user) throws LoginException {

        if (StringUtils.isBlank(user)) {
            return newResolver(factory);   // Use the default eak-service resolver
        }

        if (!StringUtils.contains(user, CoreConstants.SEPARATOR_AT)) {
            Map<String, Object> authInfo = new HashMap<>();
            if (StringUtils.contains(user, CoreConstants.SEPARATOR_COLON)) {
                authInfo.put(
                    ResourceResolverFactory.USER,
                    StringUtils.substringBefore(user, CoreConstants.SEPARATOR_COLON));
                authInfo.put(
                    ResourceResolverFactory.PASSWORD,
                    StringUtils.substringAfter(user, CoreConstants.SEPARATOR_COLON).toCharArray());
                return factory.getResourceResolver(authInfo);
            }
            authInfo.put(ResourceResolverFactory.SUBSERVICE, user);
            return factory.getServiceResourceResolver(authInfo);
        }

        String localizedUserId = StringUtils.substringBefore(user, CoreConstants.SEPARATOR_AT);
        String bundleId = StringUtils.substringAfter(user, CoreConstants.SEPARATOR_AT);
        Bundle bundle = FrameworkUtil.getBundle(ResolverUtil.class);
        BundleContext bundleContext = bundle != null ? bundle.getBundleContext() : null;
        if (bundleContext == null) {
            throw new LoginException("Not running in an OSGi container");
        }
        Bundle targetBundle = Arrays
            .stream(bundleContext.getBundles())
            .filter(b -> b.getSymbolicName().equals(bundleId))
            .findFirst()
            .orElse(null);
        if (targetBundle == null) {
            throw new LoginException("Could not locate required bundle: " + bundleId);
        }

        AtomicReference<LoginException> nestedException = new AtomicReference<>();
        ResourceResolver resolverByTargetBundle = ServiceUtil.withService(
            ResourceResolverFactory.class,
            targetBundle.getBundleContext(),
            f -> {
                try {
                    return f.getServiceResourceResolver(
                        Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, localizedUserId)
                    );
                } catch (LoginException e) {
                    nestedException.set(e);
                    return null;
                }
            },
            null);
        if (nestedException.get() != null) {
            throw nestedException.get();
        } else if (resolverByTargetBundle == null) {
            throw new LoginException("No resource resolver created. See above log for details");
        }
        return resolverByTargetBundle;
    }

    /**
     * Creates a new {@link ResourceResolver} instance for the given username using the resource resolver factory
     * obtained from the provided request's Sling bindings
     * @param request A {@link SlingHttpServletRequest} instance
     * @return New instance of {@code ResourceResolver}
     * @throws LoginException If the resolver cannot be created
     */
    @Nonnull
    public static ResourceResolver newResolver(@Nonnull SlingHttpServletRequest request) throws LoginException {

        SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        SlingScriptHelper scriptHelper = bindings != null ? (SlingScriptHelper) bindings.get(SlingBindings.SLING) : null;
        ResourceResolverFactory factory = scriptHelper != null
            ? scriptHelper.getService(ResourceResolverFactory.class)
            : null;
        if (factory == null) {
            throw new LoginException("Could not obtain ResourceResolverFactory");
        }
        return factory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER_ID)
        );
    }
}
