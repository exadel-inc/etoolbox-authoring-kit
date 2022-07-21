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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagConstants;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.servlets.OptionProviderServlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encapsulates methods for resolving paths to datasources specified directly or via a reference to a path-containing
 * node (attribute)
 * @see OptionProviderService
 */
class OptionSourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(OptionSourceResolver.class);
    private static final String PATH_JCR_CONTENT_LIST = "jcr:content/list";

    private static final Pattern INTERNAL_PATH_PATTERN = Pattern.compile(".+\\.\\w+/(.+)$");

    private static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";


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
        Resource result = isUrl(path) ? resolveUrl(request, path) : resolvePath(request, path);
        if (result != null) {
            return result;
        }
        return isUrl(fallbackPath) ? resolveUrl(request, fallbackPath) : resolvePath(request, fallbackPath);
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
                || relativePath.equals(CoreConstants.RELATIVE_PATH_PREFIX)) {
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

    /**
     * Checks if the String is URL
     * @param urlString the URL string
     * @return the result of the checking if String URL or not
     */
    static boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
        }
        return false;
    }

    /**
     * Called from {@link OptionProviderServiceImpl#getOptions(SlingHttpServletRequest)} to get response as JsonNode
     * @param pathParameter URL String for getting request
     * @return {@code JsonNode} instance, or null
     */
    static Resource resolveUrl(SlingHttpServletRequest request, String pathParameter) {
        String internalPath = getInternalPath(pathParameter);
        String url = StringUtils.removeEnd(pathParameter, internalPath);
        String json = getJson(url);
        JsonNode jsonWithValues = parseJson(json, internalPath);
        return createResource(request, jsonWithValues);
    }

    /**
     * Extracts the path to the target node within the JSON structure from the URL.
     * This path can be specified if we need not from the root
     * @param url The URL of the JSON
     * @return String value, can be empty if need to read data from the root
     */
    static String getInternalPath(String url) {
        Matcher matcher = INTERNAL_PATH_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Makes GET request to the URL and returns a JSON response
     * @param url the URL String
     * @return the JSON response as a String or empty String
     */
    static String getJson(String url) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.USER_AGENT, USER_AGENT_VALUE);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, OptionProviderServlet.CONTENT_TYPE_JSON);
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            LOG.error("Can't get a response from {}", url, e);
        } finally {
            if (httpResponse != null) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            httpGet.releaseConnection();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Extract jsonNode from the JSON string by the JSON field name
     * @param json the JSON string
     * @param suffix the suffix name
     * @return {@code JsonNode} object or null
     */
    static JsonNode parseJson(String json, String suffix) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            if (StringUtils.isBlank(suffix)) {
                return jsonNode;
            }
            String[] fields = suffix.split(CoreConstants.SEPARATOR_SLASH);
            for (String field : fields) {
                jsonNode = jsonNode.get(field);
            }
            return jsonNode;
        } catch (IOException e) {
            LOG.error("Can't read JSON tree", e);
        }
        return null;
    }

    static Resource createResource(SlingHttpServletRequest request, JsonNode jsonNode) {
        List<Resource> children;
        if (jsonNode.isArray()) {
            children = StreamSupport.stream(Spliterators.spliteratorUnknownSize(jsonNode.elements(), Spliterator.ORDERED), false)
                .map(element -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(element.fields(), Spliterator.ORDERED), false)
                    .collect(Collectors.toMap(Map.Entry::getKey, field -> (Object) field.getValue().textValue())))
                .map(ValueMapDecorator::new)
                .map(valueMap -> new ValueMapResource(request.getResourceResolver(), StringUtils.EMPTY, StringUtils.EMPTY, valueMap))
                .collect(Collectors.toList());
        } else {
            Map<String, Object> sourceMap = StreamSupport.stream(Spliterators.spliteratorUnknownSize(jsonNode.fields(), Spliterator.ORDERED), false)
                .collect(Collectors.toMap(Map.Entry::getKey, field -> field.getValue().textValue()));
            ValueMapDecorator valueMapDecorator = new ValueMapDecorator(sourceMap);
            ValueMapResource valueMapResource = new ValueMapResource(request.getResourceResolver(), StringUtils.EMPTY, StringUtils.EMPTY, valueMapDecorator);
            children = Collections.singletonList(valueMapResource);
        }
        return new ValueMapResource(request.getResourceResolver(), StringUtils.EMPTY, StringUtils.EMPTY, new ValueMapDecorator(Collections.emptyMap()), children);
    }
}
