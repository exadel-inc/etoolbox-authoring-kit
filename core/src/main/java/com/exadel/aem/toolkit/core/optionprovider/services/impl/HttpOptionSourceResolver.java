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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Implements {@link OptionSourceResolver} to facilitate extracting option datasources from HTTP endpoints
 */
class HttpOptionSourceResolver implements OptionSourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(HttpOptionSourceResolver.class);

    private static final Pattern INTERNAL_PATH_PATTERN = Pattern.compile(".+\\.json/(.+)$", Pattern.CASE_INSENSITIVE);

    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";
    private static final int HTTP_TIMEOUT = 10_000;

    private HttpClient httpClient;

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
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resolve(SlingHttpServletRequest request, String uri) {
        String internalPath = getInternalPath(uri);
        String url = StringUtils.removeEnd(uri, internalPath);
        String content = getResponseContent(url);
        JsonNode jsonNode = parseJson(content, internalPath);
        return jsonNode != null ? createResource(request, jsonNode) : null;
    }

    /**
     * Attempts an HTTP request to the given endpoint and retrieves the payload of the response
     * @param url Address of the endpoint
     * @return String value; can be an empty string
     */
    private String getResponseContent(String url) {
        RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectTimeout(HTTP_TIMEOUT)
            .setConnectionRequestTimeout(HTTP_TIMEOUT)
            .setSocketTimeout(HTTP_TIMEOUT)
            .build();

        HttpClient effectiveHttpClient = httpClient != null
            ? httpClient
            : HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.USER_AGENT, HTTP_USER_AGENT);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        HttpResponse httpResponse = null;

        try {
            httpResponse = effectiveHttpClient.execute(httpGet);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            LOG.error("Could not get a response from {}", url, e);
        } finally {
            if (httpResponse != null) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            httpGet.releaseConnection();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Extracts the path to the target node within the JSON structure from the URL. This path can be specified if the
     * option datasource does not begin from the "root" of the JSON structure
     * @param url The URL of the endpoint serving JSON data
     * @return String value; can be an empty string if additional traversing is not needed
     */
    private static String getInternalPath(String url) {
        Matcher matcher = INTERNAL_PATH_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Parses the provided string into a {@link JsonNode} entity; traverses to a nested subnode if {@code suffix} is
     * provided
     * @param source String value representing the parseable content
     * @param suffix An optional slash-delimited string representing the path to a nested JSON subnode
     * @return {@code JsonNode} object or null
     */
    private static JsonNode parseJson(String source, String suffix) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(source);
            if (StringUtils.isBlank(suffix)) {
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
            LOG.error("Could not read or navigate the JSON tree", e);
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
                StringUtils.EMPTY,
                valueMap);
            children = Collections.singletonList(valueMapResource);
        }
        return new ValueMapResource(
            request.getResourceResolver(),
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
    }

    /**
     * Called by {@link HttpOptionSourceResolver#createResource(SlingHttpServletRequest, JsonNode)} to convert a particular
     * {@link JsonNode} into a {@code ValueMap} containing all the keys and values contained in the node
     * @param jsonNode {@link JsonNode} object containing values for the value map
     * @return {@link ValueMap} object
     */
    private static ValueMap createValueMap(JsonNode jsonNode) {
        Map<String, Object> sourceMap = StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(jsonNode.fields(), Spliterator.ORDERED), false)
            .collect(Collectors.toMap(Map.Entry::getKey, field -> field.getValue().asText()));
        return new ValueMapDecorator(sourceMap);
    }
}
