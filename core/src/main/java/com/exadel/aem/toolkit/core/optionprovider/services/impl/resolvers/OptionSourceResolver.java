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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagConstants;

import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionSourceResolutionResult;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;

/**
 * Contains methods for resolving provided URIs (representing either JCR paths, HTTP endpoints or class name) to option data sources
 * @see OptionProviderService
 */
public interface OptionSourceResolver {
    OptionSourceResolver HTTP_RESOLVER = new HttpOptionSourceResolver();
    OptionSourceResolver JCR_RESOLVER = new JcrOptionSourceResolver();
    OptionSourceResolver CLASS_RESOLVER = new ClassOptionSourceResolver();
    Pattern CLASS_NAME = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*");

    /**
     * Called by {@link OptionSourceResolver#resolve(SlingHttpServletRequest, PathParameters)} to retrieve a {@link
     * Resource} object representing the option datasource
     * @param request Current {@code SlingHttpServletRequest}
     * @param uri     Either a JCR path to the {@code Resource} that represents a datasource or contains a link to the
     *                actual datasource, or else an HTTP endpoint containing the datasource data in JSON format
     * @return {@code Resource} instance, or null
     */
    OptionSourceResolutionResult resolve(SlingHttpServletRequest request, PathParameters pathParameters, String uri);

    /**
     * Static method compatible with business logic and testing logic. Depending on
     * @param pathParameters {@link PathParameters} it constructs the needed {@link OptionSourceResolver} realization
     *                       and uses
     * @param request        {@link SlingHttpServletRequest} to build and to
     * @return {@link Resource}
     */
    static OptionSourceResolutionResult resolve(SlingHttpServletRequest request, PathParameters pathParameters) {
        OptionSourceResolver predefinedResolver =
            (OptionSourceResolver) request.getAttribute(OptionSourceResolver.class.getName());
        OptionSourceResolver effectiveResolver = ObjectUtils.firstNonNull(
            predefinedResolver,
            getResolver(pathParameters.getPath()));

        OptionSourceResolutionResult result = effectiveResolver.resolve(
            request,
            pathParameters,
            pathParameters.getPath());

        if (result != null) {
            return result;
        }

        effectiveResolver = ObjectUtils.firstNonNull(
            predefinedResolver,
            getResolver(pathParameters.getFallbackPath()));

        return effectiveResolver.resolve(request, pathParameters, pathParameters.getFallbackPath());
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

    static OptionSourceResolver getResolver(String uri) {
        if (isClassName(uri)) {
            return CLASS_RESOLVER;
        }
        if (isUrl(uri)) {
            return HTTP_RESOLVER;
        }
        return JCR_RESOLVER;
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
     * @param value A class name string
     * @return True or false
     */
    static boolean isClassName(String value) {
        return StringUtils.isNotBlank(value) && CLASS_NAME.matcher(value).matches();
    }
}
