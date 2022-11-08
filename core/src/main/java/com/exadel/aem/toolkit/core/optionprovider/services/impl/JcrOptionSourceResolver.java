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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Implements {@link OptionSourceResolver} to provide resolving JCR paths to option data sources
 */
class JcrOptionSourceResolver implements OptionSourceResolver {

    private static final String PATH_JCR_CONTENT_LIST = "jcr:content/list";

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource pathResolve(SlingHttpServletRequest request, PathParameters pathParameters) {
        return resolve(request, pathParameters.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource fallbackResolve(SlingHttpServletRequest request, PathParameters pathParameters) {
        return resolve(request, pathParameters.getFallbackPath());
    }

    /**
     * <p>Both direct and <i>referenced</i> paths are supported. For instance, if a user-provided setting
     * contains the {@code @} symbol, this is considered to be a <u>reference</u> to a <i>foreign</i> node and its
     * attribute in which the actual path to datasource is authored (say, via a dialog path picker)
     * @param request represents {@link SlingHttpServletRequest}
     * @param uri is {@link String} jcr address
     * @return {@link Resource}
     */
    private Resource resolve(SlingHttpServletRequest request, String uri) {
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        // Path containing "@" is considered path-and-attribute and is further parsed at the second method's overload
        if (uri.contains(CoreConstants.SEPARATOR_AT)) {
            return resolvePath(request,
                StringUtils.substringBefore(uri, CoreConstants.SEPARATOR_AT),
                StringUtils.substringAfter(uri, CoreConstants.SEPARATOR_AT));
        }

        Resource result;
        if (uri.startsWith(CoreConstants.SEPARATOR_SLASH)) {
            // Path starting with "/" is considered absolute, so it is resolved directly  via ResourceResolver
            result = request.getResourceResolver().resolve(uri);
        } else {
            // For a non-absolute path, we must resolve *target* content resource
            // (whilst the current resource is rather the Granite node of the component's structure under /apps)
            // the target resource path is passed via request suffix
            result = resolvePathViaRequestSuffix(request, uri);
        }

        // Early return in case result is not resolvable
        if (result == null || result instanceof NonExistingResource) {
            return null;
        }

        // Return tag root
        if (OptionSourceResolver.isTagCollection(result)) {
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
     * @param referencePath      Path to the {@code Resource} that contains a user-authored path to the actual
     *                           datasource
     * @param referenceAttribute Name of the attribute that exposes a user-authored path to the actual datasource
     * @return {@code Resource} instance, or null
     */
    private Resource resolvePath(SlingHttpServletRequest request, String referencePath, String referenceAttribute) {
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
        return resolve(request, contentResourceAttributeValue);
    }

    /**
     * Provides retrieving a <i>content</i> resource via the current {@code SlingHttpServletRequest}'s path suffix,
     * rather than the referenced Granite resource
     * @param request      Current {@code SlingHttpServletRequest}
     * @param relativePath Path to another content resource
     * @return {@code Resource} instance, or null
     */
    private static Resource resolvePathViaRequestSuffix(SlingHttpServletRequest request, String relativePath) {
        String contentResourcePath = request.getRequestPathInfo().getSuffix();
        if (StringUtils.isEmpty(contentResourcePath)) {
            return null;
        }
        Resource contentResource = request.getResourceResolver().resolve(contentResourcePath);
        if (contentResource instanceof NonExistingResource
            || StringUtils.isEmpty(relativePath)
            || relativePath.equals(CoreConstants.RELATIVE_PATH_PREFIX)) {
            return contentResource;
        }
        return request.getResourceResolver().getResource(contentResource, relativePath);
    }
}
