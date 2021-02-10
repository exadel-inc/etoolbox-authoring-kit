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

package com.exadel.aem.toolkit.bundle.optionprovider.services.impl;

import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagConstants;

/**
 * This utility class encapsulates methods for resolving paths to datasources specified directly or via a reference
 * to path-containing node/attribute
 * @see com.exadel.aem.toolkit.bundle.optionprovider.services.OptionProvider
 */
class OptionSourceResolver {
    private static final String NODE_NAME_LIST = "jcr:content/list";

    /**
     * Default (private) constructor
     */
    private OptionSourceResolver() {}

    /**
     * Calls {@link OptionSourceResolver#resolvePath(SlingHttpServletRequest, String)} for the {@code path} argument,
     * and returns a non-null return value. But if null returned, calls the same method once again
     * for the {@code fallbackPath} argument
     * @param request Current {@code SlingHttpServletRequest}
     * @param path Path to {@code Resource} that either represents datasource or contains user-authored path to actual
     *             datasource
     * @param fallbackPath Path to {@code Resource} that either represents datasource or contains user-authored path
     *                     to actual datasource. Applied to if {@code path} resolves oto null
     * @return {@code Resource} instance, or null
     */
    static Resource resolve(SlingHttpServletRequest request, String path, String fallbackPath) {
        Resource result = resolvePath(request, path);
        if (result != null) {
            return result;
        }
        return resolvePath(request, fallbackPath);
    }

    /**
     * <p>Tries to retrieve {@code Resource} instance based on user-provided path.</p>
     * <p>Both absolute and relative paths supported.</p>
     * <p>Also, both direct and <i>referenced</i> paths supported. For instance, if a user-provided settings
     * contains {@code @} symbol, this is considered to be a <u>reference</u> to a <i>foreign</i> node and its attribute
     * in which actual path to datasource is authored (say, via a dialog path picker)
     * @param request Current {@code SlingHttpServletRequest}
     * @param path Path to {@code Resource} that either represents datasource or contains user-authored path to actual
     *             datasource
     * @return {@code Resource} instance, or null
     */
    private static Resource resolvePath(SlingHttpServletRequest request, String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        // path containing "@" is considered path-and-attribute and is further parsed at the second method's overload
        if (path.contains(OptionSourceParameters.SEPARATOR_AT)) {
            return resolvePath(request,
                    StringUtils.substringBefore(path, OptionSourceParameters.SEPARATOR_AT),
                    StringUtils.substringAfter(path, OptionSourceParameters.SEPARATOR_AT));
        }

        Resource result;
        if (path.startsWith(OptionSourceParameters.SEPARATOR_SLASH)) {
            // path starting with "/" is considered absolute so it is resolved directly  via ResourceResolver
            result = request.getResourceResolver().resolve(path);
        } else {
            // for a non-absolute path, we must resolve *target* content resource
            // (whilst the current resource is rather the Granite node of the component's structure under /apps)
            // the target resource path is passed via request suffix
            result = resolvePathViaRequestSuffix(request, path);
        }

        // early return in case result is not resolvable
        if (result == null || result instanceof NonExistingResource) {
            return null;
        }

        // return tag root
        if (isTagCollection(result)) {
            return result;
        }

        // if this is an ACS List -like structure, return the jcr:content/list subnode as datasource root
        Resource listingChild = result.getResourceResolver().getResource(result, NODE_NAME_LIST);
        if (listingChild != null) {
            return listingChild;
        }

        // otherwise, return the retrieved Resource as is
        return result;
    }

    /**
     * Tries to retrieve {@code Resource} instance representing datasource based path to node where datasource path is stored,
     * and the name of node's attribute. Both absolute and relative paths supported, and so are direct and referenced paths
     * @param request Current {@code SlingHttpServletRequest}
     * @param referencePath Path to {@code Resource} that contains user-authored path to actual datasource
     * @param referenceAttribute Name of the attribute storing user-authored path to actual datasource
     * @return {@code Resource} instance, or null
     */
    private static Resource resolvePath(SlingHttpServletRequest request, String referencePath, String referenceAttribute) {
        Resource contentResource = referencePath.startsWith(OptionSourceParameters.SEPARATOR_SLASH)
                ? request.getResourceResolver().resolve(referencePath)
                : resolvePathViaRequestSuffix(request, referencePath);
        if (contentResource == null || contentResource instanceof NonExistingResource) {
            return null;
        }
        String contentResourceAttributeValue = contentResource.getValueMap().get(referenceAttribute, String.class);
        if (StringUtils.isBlank(contentResourceAttributeValue)) {
            return null;
        }
        return resolvePath(request, contentResourceAttributeValue);
    }

    /**
     * Utility method called by {@link OptionSourceResolver#resolvePath(SlingHttpServletRequest, String)}
     * and {@link OptionSourceResolver#resolve(SlingHttpServletRequest, String, String)} to marshal
     * retrieving <i>content</i> resource via the current {@code SlingHttpServletRequest} path info suffix, rather
     * than the referenced Granite resource
     * @param request Current {@code SlingHttpServletRequest}
     * @param relativePath Path to another content resource
     * @return {@code Resource} instance, or null
     */
    private static Resource resolvePathViaRequestSuffix(SlingHttpServletRequest request, String relativePath) {
        String contentResourcePath = request.getRequestPathInfo().getSuffix();
        if (StringUtils.isEmpty(contentResourcePath)) {
            return null;
        }
        Resource contentResource =  request.getResourceResolver().resolve(contentResourcePath);
        if (contentResource instanceof NonExistingResource
                || StringUtils.isEmpty(relativePath)
                || relativePath.equals("./")) {
            return contentResource;
        }
        return request.getResourceResolver().getResource(contentResource, relativePath);
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
}
