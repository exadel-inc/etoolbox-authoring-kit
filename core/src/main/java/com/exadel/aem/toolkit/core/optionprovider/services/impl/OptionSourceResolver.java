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

import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagConstants;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Encapsulates methods for resolving paths to datasources specified directly or via a reference to a path-containing
 * node (attribute)
 * @see OptionProviderService
 */
class OptionSourceResolver {
    private static final String PATH_JCR_CONTENT_LIST = "jcr:content/list";

    /**
     * Default (instantiation-restricting) constructor
     */
    private OptionSourceResolver() {}

    /**
     * Calls {@link OptionSourceResolver#resolvePath(SlingHttpServletRequest, String)} with the {@code path} argument,
     * and returns a non-null resulting value. If null is returned, calls the same method once again with the
     * {@code fallbackPath} argument
     * @param request      Current {@code SlingHttpServletRequest}
     * @param path         Path to {@code Resource} that either represents datasource or contains user-authored path to
     *                     an actual datasource
     * @param fallbackPath Path to a {@code Resource} that either represents datasource or contains the user-authored path
     *                     to an actual datasource. Used if the {@code path} resolves to null
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
     * <p>Tries to retrieve a {@code Resource} instance based on user-provided path.</p>
     * <p>Both absolute and relative paths are supported.</p>
     * <p>Also, both direct and <i>referenced</i> paths are supported. For instance, if a user-provided setting
     * contains the {@code @} symbol, this is considered to be a <u>reference</u> to a <i>foreign</i> node and its
     * attribute in which the actual path to datasource is authored (say, via a dialog path picker)
     * @param request Current {@code SlingHttpServletRequest}
     * @param path    Path to a {@code Resource} that either represents a datasource or contains user-authored path
     *                to an actual datasource
     * @return {@code Resource} instance, or null
     */
    private static Resource resolvePath(SlingHttpServletRequest request, String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        // Path containing "@" is considered path-and-attribute and is further parsed at the second method's overload
        if (path.contains(CoreConstants.SEPARATOR_AT)) {
            return resolvePath(request,
                    StringUtils.substringBefore(path, CoreConstants.SEPARATOR_AT),
                    StringUtils.substringAfter(path, CoreConstants.SEPARATOR_AT));
        }

        Resource result;
        if (path.startsWith(CoreConstants.SEPARATOR_SLASH)) {
            // Path starting with "/" is considered absolute, so it is resolved directly  via ResourceResolver
            result = request.getResourceResolver().resolve(path);
        } else {
            // For a non-absolute path, we must resolve *target* content resource
            // (whilst the current resource is rather the Granite node of the component's structure under /apps)
            // the target resource path is passed via request suffix
            result = resolvePathViaRequestSuffix(request, path);
        }

        // Early return in case result is not resolvable
        if (result == null || result instanceof NonExistingResource) {
            return null;
        }

        // Return tag root
        if (isTagCollection(result)) {
            return result;
        }

        // If this is an ACS List -like structure, return the jcr:content/list subnode as datasource root
        Resource listingChild = result.getResourceResolver().getResource(result, PATH_JCR_CONTENT_LIST);
        if (listingChild != null) {
            return listingChild;
        }

        // Otherwise, return the retrieved Resource as is
        return result;
    }

    /**
     * Tries to retrieve a {@code Resource} instance representing the path to a node where the actual datasource path is
     * stored, and the name of node's attribute. Absolute and relative paths are supported, and so are direct and
     * referenced paths
     * @param request            Current {@code SlingHttpServletRequest}
     * @param referencePath      Path to the {@code Resource} that contains a user-authored path to the actual datasource
     * @param referenceAttribute Name of the attribute that exposes a user-authored path to the actual datasource
     * @return {@code Resource} instance, or null
     */
    private static Resource resolvePath(SlingHttpServletRequest request, String referencePath, String referenceAttribute) {
        Resource contentResource = referencePath.startsWith(CoreConstants.SEPARATOR_SLASH)
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
     * retrieving a <i>content</i> resource via the current {@code SlingHttpServletRequest}'s path suffix, rather
     * than the referenced Granite resource
     * @param request      Current {@code SlingHttpServletRequest}
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
