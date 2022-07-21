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
package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagConstants;

import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Contains methods for resolving provided URIs (representing either JCR paths or HTTP endpoints) to option data sources
 * @see OptionProviderService
 */
interface OptionSourceResolver {
    OptionSourceResolver HTTP_RESOLVER = new HttpOptionSourceResolver();
    OptionSourceResolver JCR_RESOLVER = new JcrOptionSourceResolver();

    /**
     * Called by {@link OptionSourceResolver#resolve(SlingHttpServletRequest, String, String)} to retrieve a {@link
     * Resource} object representing the option datasource. Depending on the implementation, this method either
     * retrieves a JCR entry or gets an HTTP response and converts it into a virtual resource
     * @param request Current {@code SlingHttpServletRequest}
     * @param uri     Either a JCR path to the {@code Resource} that represents a datasource or contains a link to the
     *                actual datasource, or else an HTTP endpoint containing the datasource data in JSON format
     * @return {@code Resource} instance, or null
     */
    Resource resolve(SlingHttpServletRequest request, String uri);

    /**
     * Tries to retrieve a {@code Resource} instance based on user-provided URI: either a JCR path or an HTTP endpoint.
     * First, the {@code path} value is considered. If there's not a valid option source, the value of {@code
     * fallbackPath} is considered in turn
     * @param request     Current {@code SlingHttpServletRequest}
     * @param uri         Either a JCR path to the {@code Resource} that represents a datasource or contains a link to
     *                    the actual datasource, or else an HTTP endpoint containing the datasource data in JSON format
     * @param fallbackUri Either a JCR path to the {@code Resource} that represents a datasource or contains a link to
     *                    the actual datasource, or else an HTTP endpoint containing the datasource data in JSON format
     * @return {@code Resource} instance, or null
     */
    static Resource resolve(SlingHttpServletRequest request, String uri, String fallbackUri) {
        Resource result = isUrl(uri)
            ? HTTP_RESOLVER.resolve(request, uri)
            : JCR_RESOLVER.resolve(request, uri);
        if (result != null) {
            return result;
        }
        return isUrl(fallbackUri)
            ? HTTP_RESOLVER.resolve(request, fallbackUri)
            : JCR_RESOLVER.resolve(request, fallbackUri);
    }

    /**
     * Gets whether the current datasource path is a path to a tags folder
     * @param resource {@code Resource} instance representing the (expected) datasource
     * @return True if this datasource contains tags; otherwise, false
     */
    static boolean isTagCollection(Resource resource) {
        try {
            Node resourceNode = Objects.requireNonNull(resource.adaptTo(Node.class));
            if (!resourceNode.isNodeType(JcrConstants.NT_FOLDER) && !resourceNode.isNodeType(TagConstants.NT_TAG)) {
                return false;
            }
            return resourceNode.hasNodes()
                && resourceNode.getNodes().nextNode().isNodeType(TagConstants.NT_TAG);
        } catch (RepositoryException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Checks if the provided string can be considered a URL by trying to parse it into a {@code URL} object
     * @param value the URL string
     * @return True or false
     */
    static boolean isUrl(String value) {
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
