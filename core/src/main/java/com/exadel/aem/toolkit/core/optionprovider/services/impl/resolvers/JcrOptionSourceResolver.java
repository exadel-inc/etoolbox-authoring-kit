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
package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;
import com.exadel.aem.toolkit.core.optionprovider.utils.ResourceTypeUtil;

/**
 * Implements {@link OptionSourceResolver} to provide resolution of JCR paths to option data sources
 */
class JcrOptionSourceResolver implements OptionSourceResolver {

    private static final String PATH_JCR_CONTENT_LIST = "jcr:content/list";

    /**
     * {@inheritDoc}
     * <p>Both direct and <i>referenced</i> paths are supported. For instance, if a user-provided setting
     * contains the {@code @} symbol, this is considered to be a <u>reference</u> to a <i>foreign</i> node and its
     * attribute in which the actual path to datasource is authored (say, via a dialog path picker)
     */
    @Override
    public Resource resolve(SlingHttpServletRequest request, PathParameters params) {
        if (StringUtils.isBlank(params.getPath())) {
            return null;
        }
        return resolve(request, params.getPath());
    }

    /**
     * Retrieves or produces a {@link Resource} object representing the option datasource
     * @param request Current {@link SlingHttpServletRequest}
     * @param path    A JCR path
     * @return {@code Resource} instance, or else {@code null}
     */
    private Resource resolve(SlingHttpServletRequest request, String path) {
        // Path containing "@" is considered path-and-attribute and is further parsed at the second method's overload
        if (path.contains(CoreConstants.SEPARATOR_AT)) {
            return resolvePath(
                request,
                StringUtils.substringBefore(path, CoreConstants.SEPARATOR_AT),
                StringUtils.substringAfter(path, CoreConstants.SEPARATOR_AT));
        }

        Resource dataSource;
        if (path.startsWith(CoreConstants.SEPARATOR_SLASH)) {
            // A path starting with "/" is considered absolute, so it is resolved directly via ResourceResolver
            dataSource = request.getResourceResolver().resolve(path);
        } else {
            // For a non-absolute path, we must resolve the target content resource
            // (while the current resource is the Granite node of the component's structure under /apps).
            // The target resource path is passed via the request suffix
            dataSource = resolvePathViaRequestSuffix(request, path);
        }

        // Early return in case dataSource is not resolvable
        if (dataSource == null || dataSource instanceof NonExistingResource) {
            return null;
        }

        // Return tag root
        if (ResourceTypeUtil.isTagCollection(dataSource)) {
            return dataSource;
        }

        // If this is an ACS List -like structure, return the jcr:content/list subnode as datasource root
        Resource listingChild = dataSource.getResourceResolver().getResource(dataSource, PATH_JCR_CONTENT_LIST);
        if (listingChild != null) {
            return listingChild;
        }

        // Otherwise, return the retrieved Resource as is
        return dataSource;
    }

    /**
     * Tries to retrieve a {@code Resource} instance representing the path to a node where the actual datasource path is
     * stored and the name of the node's attribute. Absolute and relative paths are supported, and so are direct and
     * referenced paths
     * @param request            Current {@code SlingHttpServletRequest}
     * @param referencePath      Path to the {@code Resource} that contains a user-authored address of the actual
     *                           datasource
     * @param referenceAttribute Name of the attribute that exposes a user-authored path to the actual datasource
     * @return {@code Resource} instance, or null
     */
    private Resource resolvePath(
        SlingHttpServletRequest request,
        String referencePath,
        String referenceAttribute) {
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
