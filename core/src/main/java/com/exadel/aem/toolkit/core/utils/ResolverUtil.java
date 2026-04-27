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

import java.util.Collections;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Provides utility methods for creating {@link ResourceResolver} instances in the context of
 * {@code EToolbox Configurator}
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own code
 */
public class ResolverUtil {

    private static final String SERVICE_USER_ID = "eak-service";

    /**
     * Default (instantiation-restricting) constructor
     */
    private ResolverUtil() {
    }

    /**
     * Creates a new {@link ResourceResolver} instance for the given username using the provided resource resolver
     * factory
     * @param factory  The {@code ResourceResolverFactory} instance
     * @return New instance of {@code ResourceResolver}
     * @throws LoginException If the resolver cannot be created
     */
    @NotNull
    public static ResourceResolver newResolver(@NotNull ResourceResolverFactory factory) throws LoginException {

        return factory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER_ID)
        );
    }

    /**
     * Creates a new {@link ResourceResolver} instance for the given username using the resource resolver factory
     * obtained from the provided request's Sling bindings
     * @param request A {@link SlingHttpServletRequest} instance
     * @return New instance of {@code ResourceResolver}
     * @throws LoginException If the resolver cannot be created
     */
    @NotNull
    public static ResourceResolver newResolver(@NotNull SlingHttpServletRequest request) throws LoginException {

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
