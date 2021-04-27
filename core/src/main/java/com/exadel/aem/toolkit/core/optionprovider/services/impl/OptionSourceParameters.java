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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Parses and serves user-specified settings for arranging a datasource for the current request
 * @see OptionProviderService
 */
public class OptionSourceParameters {

    static final String KEV_VALUE_SEPARATOR_PATTERN = "(?<!\\\\):";
    static final String INLINE_COLON_PATTERN = "\\\\?:";

    private static final String QUERY_KEY_APPEND_OPTIONS = "append";
    private static final String QUERY_KEY_ATTRIBUTE_MEMBERS = "attributeMembers";
    private static final String QUERY_KEY_ATTRIBUTES = "attributes";
    private static final String QUERY_KEY_EXCLUDE_OPTIONS = "exclude";
    private static final String QUERY_KEY_FALLBACK_PATH = "fallbackPath";
    private static final String QUERY_KEY_PREPEND_OPTIONS = "prepend";
    private static final String QUERY_KEY_PATH = "path";
    private static final String QUERY_KEY_SORTED = "sorted";
    private static final String QUERY_KEY_TEXT_MEMBER = "textMember";
    private static final String QUERY_KEY_TEXT_TRANSFORM = "textTransform";
    private static final String QUERY_KEY_VALUE_MEMBER = "valueMember";
    private static final String QUERY_KEY_VALUE_TRANSFORM = "valueTransform";

    private static final String NODE_NAME_DATASOURCE = "datasource";

    private final List<PathParameters> pathParameters;
    private String[] appendOptions;
    private String[] prependOptions;
    private String[] excludeOptions;
    private String selectedValue;
    private boolean sorted;

    private OptionSourceParameters() {
        pathParameters = new ArrayList<>();
    }

    /**
     * Gets the collection of user-defined settings specific for a datasource path
     * @return List of {@link PathParameters} objects
     */
    List<PathParameters> getPathParameters() {
        return pathParameters;
    }

    /**
     * Gets the user-specified {@code append} setting value
     * @return Array of strings, or null
     */
    String[] getAppendedOptions() {
        return appendOptions;
    }

    /**
     * Gets the user-specified {@code prepend} setting value
     * @return Array of strings, or null
     */
    String[] getPrependedOptions() {
        return prependOptions;
    }

    /**
     * Gets the user-specified {@code exclude} setting value
     * @return Array of strings, or null
     */
    String[] getExcludedOptions() {
        return excludeOptions;
    }

    /**
     * Gets the user-specified {@code selectedValue} setting value
     * @return String value
     */
    String getSelectedValue() {
        return selectedValue;
    }

    /**
     * Gets whether alphabetical sorting of options will be performed
     * @return True or false
     */
    boolean isSorted() {
        return sorted;
    }

    /**
     * Creates an instance of {@link OptionSourceParameters} for the current request
     * @param request {@code SlingHttpServletRequest} instance
     * @return {@code RequestParameters} object
     */
    static OptionSourceParameters forRequest(SlingHttpServletRequest request) {
        final OptionSourceParameters result = new OptionSourceParameters();
        final ValueMap repository = getParameterRepository(request);

        List<String> pathRelatedKeys = repository.keySet().stream()
            .filter(key -> key.startsWith(QUERY_KEY_PATH)
                && !repository.get(key, StringUtils.EMPTY).isEmpty())
            .sorted()
            .collect(Collectors.toList());

        for (String pathKey : pathRelatedKeys) {
            String suffix = StringUtils.substringAfter(pathKey, QUERY_KEY_PATH);
            result.pathParameters.add(
                PathParameters.builder()
                    .path(repository.get(pathKey, String.class))
                    .fallbackPath(
                        repository.get(QUERY_KEY_FALLBACK_PATH + suffix, String.class),
                        repository.get(QUERY_KEY_FALLBACK_PATH, String.class)
                    )
                    .textMember(
                        repository.get(QUERY_KEY_TEXT_MEMBER + suffix, String.class),
                        repository.get(QUERY_KEY_TEXT_MEMBER, String.class)
                    )
                    .valueMember(
                        repository.get(QUERY_KEY_VALUE_MEMBER + suffix, String.class),
                        repository.get(QUERY_KEY_VALUE_MEMBER, String.class)
                    )
                    .attributeMembers(
                        repository.get(QUERY_KEY_ATTRIBUTE_MEMBERS + suffix, String[].class),
                        repository.get(QUERY_KEY_ATTRIBUTE_MEMBERS, String[].class)
                    )
                    .attributes(
                        repository.get(QUERY_KEY_ATTRIBUTES + suffix, String[].class),
                        repository.get(QUERY_KEY_ATTRIBUTES, String[].class)
                    )
                    .textTransform(getTransformValue(
                        repository.get(QUERY_KEY_TEXT_TRANSFORM + suffix, String.class),
                        repository.get(QUERY_KEY_TEXT_TRANSFORM, String.class)
                    ))
                    .valueTransform(getTransformValue(
                        repository.get(QUERY_KEY_VALUE_TRANSFORM + suffix, String.class),
                        repository.get(QUERY_KEY_VALUE_TRANSFORM, String.class)
                    ))
                    .build()
            );
        }

        result.appendOptions = repository.get(QUERY_KEY_APPEND_OPTIONS, String[].class);
        result.prependOptions = repository.get(QUERY_KEY_PREPEND_OPTIONS, String[].class);
        result.excludeOptions = repository.get(QUERY_KEY_EXCLUDE_OPTIONS, String[].class);

        result.selectedValue = repository.get(CoreConstants.PN_SELECTED, String.class);
        result.sorted = repository.get(QUERY_KEY_SORTED, Boolean.FALSE.toString()).equalsIgnoreCase(Boolean.TRUE.toString());

        return result;
    }

    /**
     * Called from {@link OptionSourceParameters#forRequest(SlingHttpServletRequest)} to compile a collection of user-set
     * parameters that are stored in the current {@code SlingHttpServletRequest}'s query parameter map, or else in the
     * current {@code Resource}'s {@code datasource} node, if such exists (so that the former overlays the latter).
     * From {@code datasource} node, array-types values are extracted, and from the request, string values are extracted
     * and split by a comma
     * @param request {@code SlingHttpServletRequest} instance
     * @return {@code ValueMap} value
     */
    private static ValueMap getParameterRepository(SlingHttpServletRequest request) {
        ValueMap result = new ValueMapDecorator(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
        Resource datasourceChild = request.getResource().getChild(NODE_NAME_DATASOURCE);
        if (datasourceChild != null) {
            result.putAll(datasourceChild.getValueMap());
        }
        if (StringUtils.endsWithIgnoreCase(request.getRequestPathInfo().getResourcePath(), ResourceTypes.OPTION_PROVIDER)) {
            request.getRequestParameterMap().forEach((k, v) -> extractRequestParameter(v)
                .ifPresent(value -> result.put(k, value.contains(CoreConstants.SEPARATOR_COMMA)
                    ? value.split(CoreConstants.SEPARATOR_COMMA)
                    : value)));
        }
        return result;
    }

    /**
     * Tries to extract and decode a {@code SlingHttpServletRequest} query parameter from the parameters array
     * @param parameters {@code RequestParameter} array as returned by {@link SlingHttpServletRequest#getRequestParameterMap()}
     * @return Optional string value
     */
    private static Optional<String> extractRequestParameter(RequestParameter[] parameters) {
        if (ArrayUtils.isEmpty(parameters)) {
            return Optional.empty();
        }
        try {
            return Optional.of(URLDecoder.decode(parameters[0].getString(), StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 encoding is supported by Java standard
            return Optional.empty();
        }
    }

    /**
     * Retrieves a {@link StringTransformation} instance from the user-provided string
     * @param source    User-provided value
     * @param altSource Alternative source to use if the {@code source} is blank
     * @return {@code StringTransformation} value
     */
    private static StringTransformation getTransformValue(String source, String altSource) {
        String effectiveSource = StringUtils.defaultIfBlank(source, altSource);
        StringTransformation result = EnumUtils.getEnum(StringTransformation.class,
            StringUtils.defaultIfBlank(effectiveSource, StringTransformation.NONE.toString()).toUpperCase());
        if (result == null) {
            result = StringTransformation.NONE;
        }
        return result;
    }
}
