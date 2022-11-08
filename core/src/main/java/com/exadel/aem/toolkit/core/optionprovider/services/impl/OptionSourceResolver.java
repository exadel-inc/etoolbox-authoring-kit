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
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagConstants;

import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Contains methods for resolving provided URIs (representing either JCR paths, HTTP endpoints or class name) to option data sources
 * @see OptionProviderService
 */
interface OptionSourceResolver {
    OptionSourceResolver HTTP_RESOLVER = new HttpOptionSourceResolver();
    OptionSourceResolver JCR_RESOLVER = new JcrOptionSourceResolver();
    OptionSourceResolver ENUM_RESOLVER = new EnumOptionSourceResolver();
    String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    /**
     * Method
     * @param request        and
     * @param pathParameters attributes
     *                       by retrieved by basic path.
     * @return {@link Resource} populated from
     */
    Resource pathResolve(SlingHttpServletRequest request, PathParameters pathParameters);

    /**
     * Method
     * @param request        and
     * @param pathParameters attributes
     *                       by retrieved by fallback path.
     * @return {@link Resource} populated from
     */
    Resource fallbackResolve(SlingHttpServletRequest request, PathParameters pathParameters);

    /**
     * Static method compatible either with business logic or with testing logic. Resolves
     * @param request {@link SlingHttpServletRequest} to get a {@link Resource} by particular
     * @param pathParameters {@link PathParameters}. Depending on path from {@link PathParameters}
     * uses either {@link HttpOptionSourceResolver}, {@link  JcrOptionSourceResolver} or {@link EnumOptionSourceResolver}
     */
    static Resource resolve(SlingHttpServletRequest request, PathParameters pathParameters) {
        OptionSourceResolver predefinedResolver =
            (OptionSourceResolver) request.getAttribute(OptionSourceResolver.class.getName());
        OptionSourceResolver effectiveResolver = ObjectUtils.firstNonNull(
            predefinedResolver,
            isUrl(pathParameters.getPath()) ? HTTP_RESOLVER : isClassName(pathParameters.getPath())
                ? ENUM_RESOLVER : JCR_RESOLVER);

        Resource result = effectiveResolver.pathResolve(request, pathParameters);

        if (result != null) {
            return result;
        }

        effectiveResolver = ObjectUtils.firstNonNull(
            predefinedResolver,
            isUrl(pathParameters.getFallbackPath()) ? HTTP_RESOLVER : isClassName(pathParameters.getFallbackPath())
                ? ENUM_RESOLVER : JCR_RESOLVER);

        return effectiveResolver.fallbackResolve(request, pathParameters);
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

    /**
     * Checks if the provided string is a class name. Supports nested classes as well.
     * @param value a class name string
     * @return True or false
     */
    static boolean isClassName(String value) {
        return Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*").matcher(value).matches();
    }
}
