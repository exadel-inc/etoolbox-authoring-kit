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

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.fasterxml.jackson.databind.JsonNode;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

/**
 * Implements {@link OptionSourceResolver} to facilitate extracting option data sources from HTTP endpoints
 */
class HttpOptionSourceResolver implements OptionSourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(HttpOptionSourceResolver.class);

    private static final Pattern INTERNAL_PATH_PATTERN = Pattern.compile(".+\\.json/(.+)$", Pattern.CASE_INSENSITIVE);

    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";
    private static final int HTTP_TIMEOUT = 10_000;

    private static final String EXCEPTION_COULD_NOT_PARSE = "Could not parse URI {}";
    private static final String EXCEPTION_NO_RESPONSE = "Could not get a response from {}";
    private static final String EXCEPTION_JSON = "Could not read or navigate the JSON tree";

    private HttpClientWrapper httpClient;

    /**
     * Default constructor
     */
    HttpOptionSourceResolver() {
    }

    /**
     * Creates a new class instance with the pre-defined {@link HttpClient} (useful for testing)
     * @param httpClient {@code HttpClient} instance
     */
    HttpOptionSourceResolver(HttpClient httpClient) {
        this.httpClient = new HttpClientWrapper(httpClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resolve(SlingHttpServletRequest request, PathParameters params) {
        String path = params.getPath();
        String internalPath = getInternalPath(path);
        path = StringUtils.removeEnd(path, CoreConstants.SEPARATOR_SLASH + internalPath);
        URI uri;
        try {
            uri = new URI(path);
        } catch (URISyntaxException e) {
            LOG.error(EXCEPTION_COULD_NOT_PARSE, path, e);
            return null;
        }
        String content = getResponseContent(uri);
        JsonNode jsonNode = parseJson(content, internalPath);
        return jsonNode != null ? createResource(request, jsonNode) : null;
    }

    /* ------------------------
       HTTP response processing
       ------------------------ */

    /**
     * Attempts an HTTP request to the given endpoint and retrieves the payload of the response
     * @param url Address of the endpoint
     * @return String value; can be an empty string
     */
    @SuppressWarnings("java:S2647") // Basic authentication is allowed on purpose
    private String getResponseContent(URI uri) {
        RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectTimeout(HTTP_TIMEOUT)
            .setConnectionRequestTimeout(HTTP_TIMEOUT)
            .setSocketTimeout(HTTP_TIMEOUT)
            .build();

        HttpGet httpGet = new HttpGet(uri.toString());
        httpGet.setHeader(HttpHeaders.USER_AGENT, HTTP_USER_AGENT);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        if (StringUtils.isNotEmpty(uri.getUserInfo())) {
            httpGet.setHeader(
                HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString((uri.getUserInfo()).getBytes(StandardCharsets.ISO_8859_1)));
        }

        HttpResponse httpResponse = null;

        try (HttpClientWrapper http = getCloseableHttpClient(requestConfig)) {
            httpResponse = http.getClient().execute(httpGet);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            LOG.error(EXCEPTION_NO_RESPONSE, url, e);
        } finally {
            if (httpResponse != null) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            httpGet.releaseConnection();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Provides a wrapper over an {@link HttpClient} eligible for use in the {@code try-with-resources} pattern. This
     * routine may use an HTTP client directly assigned to the current class (e.g., in a test case) or else construct a
     * new one based on the given config
     * @param requestConfig Describes the parameters of the HTTP client if created anew
     * @return A {@link Closeable} object
     */
    private HttpClientWrapper getCloseableHttpClient(RequestConfig requestConfig) {
        if (httpClient == null) {
            return new HttpClientWrapper(HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build());
        }
        return httpClient;
    }

    /**
     * Parses the provided string into a {@link JsonNode} entity; traverses to a nested subnode if {@code suffix} is
     * provided
     * @param source String value representing the parseable content
     * @param suffix An optional slash-delimited string representing the path to a nested JSON subnode
     * @return {@code JsonNode} object or null
     */
    private static JsonNode parseJson(String source, String suffix) {
        try {
            JsonNode jsonNode = ObjectConversionUtil.toNodeTree(source);
            if (StringUtils.isBlank(suffix) || jsonNode == null) {
                return jsonNode;
            }
            String[] pathChunks = suffix.split(CoreConstants.SEPARATOR_SLASH);
            for (String field : pathChunks) {
                jsonNode = jsonNode.get(field);
                if (jsonNode == null) {
                    break;
                }
            }
            return jsonNode;
        } catch (IOException e) {
            LOG.error(EXCEPTION_JSON, e);
        }
        return null;
    }

    /**
     * Converts the given JSON entity into a virtual {@link Resource} that represents a datasource option
     * @param request  Current {@code SlingHttpServletRequest}
     * @param jsonNode {@link JsonNode} object containing values for the resource
     * @return {@code Resource} object
     */
    private static Resource createResource(SlingHttpServletRequest request, JsonNode jsonNode) {
        List<Resource> children;
        if (jsonNode.isArray()) {
            children = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(jsonNode.elements(), Spliterator.ORDERED), false)
                .map(HttpOptionSourceResolver::createValueMap)
                .map(valueMap -> new ValueMapResource(request.getResourceResolver(), StringUtils.EMPTY, StringUtils.EMPTY, valueMap))
                .collect(Collectors.toList());
        } else {
            ValueMap valueMap = createValueMap(jsonNode);
            ValueMapResource valueMapResource = new ValueMapResource(
                request.getResourceResolver(),
                StringUtils.EMPTY,
                JcrConstants.NT_UNSTRUCTURED,
                valueMap);
            children = Collections.singletonList(valueMapResource);
        }
        return new ValueMapResource(
            request.getResourceResolver(),
            StringUtils.EMPTY,
            JcrConstants.NT_UNSTRUCTURED,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
    }

    /**
     * Called by {@link HttpOptionSourceResolver#createResource(SlingHttpServletRequest, JsonNode)} to convert a
     * particular {@link JsonNode} into a {@code ValueMap} containing all the keys and values contained in the node
     * @param jsonNode {@link JsonNode} object containing values for the value map
     * @return {@link ValueMap} object
     */
    private static ValueMap createValueMap(JsonNode jsonNode) {
        Map<String, Object> sourceMap = StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(jsonNode.fields(), Spliterator.ORDERED), false)
            .collect(Collectors.toMap(Map.Entry::getKey, field -> field.getValue().asText()));
        return new ValueMapDecorator(sourceMap);
    }

    /* ---------------
       Service classes
       --------------- */

    /**
     * Decorates an instance {@link HttpClient} to make sure it can be used in the {@code try-with-resources} pattern
     */
    private static class HttpClientWrapper implements Closeable {

        private final HttpClient client;

        /**
         * Default constructor
         * @param client A {@code HttpClient} instance
         */
        HttpClientWrapper(HttpClient client) {
            this.client = client;
        }

        /**
         * Retrieves the {@code HttpClient} associated with this instance
         * @return A {@code HttpClient} object
         */
        public HttpClient getClient() {
            return client;
        }

        /**
         * Closes the wrapped {@code HttpClient} if possible
         * @throws IOException if the underlying stream can not be properly closed
         */
        @Override
        public void close() throws IOException {
            if (client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
    }
}
