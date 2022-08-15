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
package com.exadel.aem.toolkit.core.injectors.utils;

import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

/**
 * Contains utility methods for adaptation and conversion of entities involved in Sing injectors processing.
 * <p><u>Note</u>: This class is not a part of the public API</p>
 */
public class AdaptationUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private AdaptationUtil() {
    }

    /**
     * Retrieves a {@link SlingHttpServletRequest} instance from the provided adaptable object
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code SlingHttpServletRequest} object if adaptable is of an appropriate type, or null
     */
    public static SlingHttpServletRequest getRequest(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return (SlingHttpServletRequest) adaptable;
        }
        return null;
    }

    /**
     * Retrieves a {@code SlingHttpServletRequest} instance from the provided initial request and the given Sling
     * resource
     * @param request  {@code SlingHttpServletRequest} instance used to construct the resulting request
     * @param resource Sling resource that the resulting request will report as its bundled resource
     * @return {@code SlingHttpServletRequest} instance
     */
    public static SlingHttpServletRequest getRequest(SlingHttpServletRequest request, Resource resource) {
        if (request.getResource().equals(resource)) {
            return request;
        }
        return new SlingHttpServletRequestFacade(request, resource);
    }

    /**
     * Retrieves a {@link Resource} instance from the provided adaptable object if it is assignable from {@code
     * SlingHttpServletRequest} or {@code Resource}
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code Resource} object if the {@code adaptable} is of an appropriate type, or null
     */
    public static Resource getResource(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        }
        return null;
    }

    /**
     * Retrieves a {@link ResourceResolver} instance from the provided adaptable object if it is assignable from {@code
     * Resource} or {@code SlingHttpServletRequest}
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code ResourceResolver} object if the {@code adaptable} is of an appropriate type, or null
     */
    public static ResourceResolver getResourceResolver(Object adaptable) {
        ResourceResolver resolver = null;
        if (adaptable instanceof Resource) {
            resolver = ((Resource) adaptable).getResourceResolver();
        } else if (adaptable instanceof SlingHttpServletRequest) {
            resolver = ((SlingHttpServletRequest) adaptable).getResourceResolver();
        }
        return resolver;
    }

    /**
     * Retrieves the {@code ValueMap} instance from the provided adaptable if it is of type {@code
     * SlingHttpServletRequest}, or {@code Resource}, or else {@code ValueMap}
     * @param adaptable The object which Sling tries to adapt from
     * @return Data-filled {@code ValueMap} if adaptation was successful. Otherwise, an empty {@code ValueMap} is
     * returned
     */
    public static ValueMap getValueMap(Object adaptable) {
        ValueMap result = null;
        if (adaptable instanceof SlingHttpServletRequest) {
            result = ((SlingHttpServletRequest) adaptable).getResource().getValueMap();
        } else if (adaptable instanceof Resource) {
            result = ((Resource) adaptable).getValueMap();
        } else if (adaptable instanceof ValueMap) {
            result = (ValueMap) adaptable;
        }
        if (result != null) {
            return result;
        }
        return ValueMap.EMPTY;
    }

    /* ---------------
       Utility classes
       --------------- */

    /**
     * Extends {@link SlingHttpServletRequestWrapper} to provide a {@code SlingHttpServletRequest} instance that reports
     * the pre-defined {@code Resource} as its bundled resource
     */
    private static class SlingHttpServletRequestFacade extends SlingHttpServletRequestWrapper {
        private final Resource resource;

        /**
         * Creates a new instance that is based on the given {@code SlingHttpServletRequest} while reporting the given
         * {@code Resource} as the bundled resource
         * @param request  {@code SlingHttpServletRequest} instance used for the decoration
         * @param resource Sling resource that the resulting request will report as its bundled resource
         */
        SlingHttpServletRequestFacade(SlingHttpServletRequest request, Resource resource) {
            super(request);
            this.resource = resource;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        public Resource getResource() {
            return resource != null ? resource : super.getResource();
        }
    }
}
