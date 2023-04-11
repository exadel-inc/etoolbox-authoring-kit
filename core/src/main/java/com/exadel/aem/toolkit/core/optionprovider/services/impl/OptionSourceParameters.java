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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Parses and serves user-specified settings for arranging a datasource for the current request
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 * @see OptionProviderService
 */
public class OptionSourceParameters {

    private static final String KEV_VALUE_SEPARATOR_PATTERN = "(?<!\\\\):";
    private static final String INLINE_COLON_PATTERN = "(\\\\)+:";

    private static final String QUERY_KEY_ATTRIBUTE_MEMBERS = "attributeMembers";
    private static final String QUERY_KEY_ATTRIBUTES = "attributes";
    private static final String QUERY_KEY_EXCLUDE_OPTIONS = "exclude";
    private static final String QUERY_KEY_FALLBACK = "fallback";
    private static final String QUERY_KEY_PATH = "path";
    private static final String QUERY_KEY_SORTED = "sorted";
    private static final String QUERY_KEY_TEXT_MEMBER = "textMember";
    private static final String QUERY_KEY_TEXT_TRANSFORM = "textTransform";
    private static final String QUERY_KEY_VALUE_MEMBER = "valueMember";
    private static final String QUERY_KEY_VALUE_TRANSFORM = "valueTransform";

    private final List<PathParameters> pathParameters;
    private List<Pair<String, String>> appendedOptions;
    private List<Pair<String, String>> prependedOptions;
    private List<String> excludeOptions;
    private String selectedValue;
    private boolean sorted;

    /**
     * Default constructor
     */
    private OptionSourceParameters() {
        pathParameters = new ArrayList<>();
    }

    /**
     * Gets the collection of user-defined settings specific for a datasource path
     * @return List of {@link PathParameters} objects
     */
    public List<PathParameters> getPathParameters() {
        return pathParameters;
    }

    /**
     * Gets the user-specified {@code append} setting value
     * @return An array of strings, or null
     */
    public List<Pair<String, String>> getAppendedOptions() {
        return appendedOptions;
    }

    /**
     * Gets the user-specified {@code prepend} setting value
     * @return An array of strings, or null
     */
    public List<Pair<String, String>> getPrependedOptions() {
        return prependedOptions;
    }

    /**
     * Gets the user-specified {@code exclude} setting value
     * @return An array of strings, or null
     */
    public List<String> getExcludedOptions() {
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
    public boolean isSorted() {
        return sorted;
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates an instance of {@link OptionSourceParameters} for the current request
     * @param request {@code SlingHttpServletRequest} instance
     * @return {@code RequestParameters} object
     */
    public static OptionSourceParameters forRequest(SlingHttpServletRequest request) {
        final OptionSourceParameters result = new OptionSourceParameters();
        final ValueMap repository = getParameterRepository(request);

        List<String> pathRelatedKeys = repository.keySet().stream()
            .filter(key -> key.startsWith(QUERY_KEY_PATH)
                && !repository.get(key, StringUtils.EMPTY).isEmpty())
            .sorted()
            .collect(Collectors.toList());

        for (String pathKey : pathRelatedKeys) {
            String suffix = StringUtils.substringAfter(pathKey, QUERY_KEY_PATH);
            PathParameters.Builder pathParametersBuilder = PathParameters.builder();
            pathParametersBuilder
                .path(repository.get(pathKey, String.class))

                .textMember(repository.get(QUERY_KEY_TEXT_MEMBER, String.class))
                .textMember(repository.get(QUERY_KEY_TEXT_MEMBER + suffix, String.class))

                .valueMember(repository.get(QUERY_KEY_VALUE_MEMBER, String.class))
                .valueMember(repository.get(QUERY_KEY_VALUE_MEMBER + suffix, String.class))

                .attributeMembers(toList(repository.get(QUERY_KEY_ATTRIBUTE_MEMBERS, String[].class)))
                .attributeMembers(toList(repository.get(QUERY_KEY_ATTRIBUTE_MEMBERS + suffix, String[].class)))

                .attributes(toNameValuePairs(repository.get(QUERY_KEY_ATTRIBUTES, String[].class), true))
                .attributes(toNameValuePairs(repository.get(QUERY_KEY_ATTRIBUTES + suffix, String[].class), true))

                .textTransform(getTransformValue(
                    repository.get(QUERY_KEY_TEXT_TRANSFORM + suffix, String.class),
                    repository.get(QUERY_KEY_TEXT_TRANSFORM, String.class)
                ))
                .valueTransform(getTransformValue(
                    repository.get(QUERY_KEY_VALUE_TRANSFORM + suffix, String.class),
                    repository.get(QUERY_KEY_VALUE_TRANSFORM, String.class)
                ))

                .isFallback(Boolean.TRUE.equals(repository.get(QUERY_KEY_FALLBACK, Boolean.class)))
                .isFallback(Boolean.TRUE.equals(repository.get(QUERY_KEY_FALLBACK + suffix, Boolean.class)));

            result.pathParameters.add(pathParametersBuilder.build());
        }

        result.appendedOptions = toNameValuePairs(repository.get(CoreConstants.PN_APPEND, String[].class), false);
        result.prependedOptions = toNameValuePairs(repository.get(CoreConstants.PN_PREPEND, String[].class), false);
        result.excludeOptions = toList(repository.get(QUERY_KEY_EXCLUDE_OPTIONS, String[].class));

        result.selectedValue = repository.get(CoreConstants.PN_SELECTED, String.class);
        result.sorted = repository.get(QUERY_KEY_SORTED, Boolean.FALSE.toString()).equalsIgnoreCase(Boolean.TRUE.toString());

        return result;
    }

    /**
     * Called from {@link OptionSourceParameters#forRequest(SlingHttpServletRequest)} to compile a collection of
     * user-set parameters that are stored in the current {@code SlingHttpServletRequest}'s query parameter map, or else
     * in the current {@code Resource}'s {@code datasource} node, if such exists (so that the former overlays the
     * latter). From {@code datasource} node, array-types values are extracted, and from the request, string values are
     * extracted and split by a comma
     * @param request {@code SlingHttpServletRequest} instance
     * @return {@code ValueMap} value
     */
    private static ValueMap getParameterRepository(SlingHttpServletRequest request) {
        ValueMap result = new ValueMapDecorator(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
        Resource datasourceChild = request.getResource().getChild(CoreConstants.NN_DATASOURCE);
        if (datasourceChild != null) {
            result.putAll(datasourceChild.getValueMap());
        }

        if (StringUtils.endsWithIgnoreCase(
            request.getRequestPathInfo().getResourcePath(),
            ResourceTypes.OPTION_PROVIDER)) {

            RequestParameterMap requestParameterMap = request.getRequestParameterMap();
            for (Map.Entry<String, RequestParameter[]> entry : requestParameterMap.entrySet()) {
                Optional<String> optionalRequestParameter = getRequestParameter(entry.getValue());
                if (!optionalRequestParameter.isPresent()) {
                    continue;
                }
                String value = optionalRequestParameter.get();
                if ((StringUtils.startsWith(value, CoreConstants.ARRAY_OPENING)
                    && StringUtils.endsWith(value, CoreConstants.ARRAY_CLOSING))
                    || !value.contains(CoreConstants.SEPARATOR_COMMA)) {
                    result.put(entry.getKey(), value);
                } else {
                    result.put(entry.getKey(), value.split(CoreConstants.SEPARATOR_COMMA));
                }
            }
        }
        return result;
    }

    /**
     * Tries to extract and decode a {@code SlingHttpServletRequest} query parameter from the parameters array
     * @param parameters {@code RequestParameter} array as returned by
     *                   {@link SlingHttpServletRequest#getRequestParameterMap()}
     * @return Optional string value
     */
    private static Optional<String> getRequestParameter(RequestParameter[] parameters) {
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
     * @param altSource An alternative source to use if the {@code source} is blank
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

    /* --------------------------
       Utility conversion methods
       -------------------------- */

    /**
     * Converts an array into a {@code List} instance. Takes care of the possible {@code null} argument
     * @param value An arbitrary array
     * @return A non-null {@code List} instance; may be empty
     * @param <T> Type of the argument
     */
    private static <T> List<T> toList(T[] value) {
        if (ArrayUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return Arrays.asList(value);
    }

    /**
     * Converts an array of colon-separated name-value pairs (similar to {@code One:one, Two:two}) into a collection of
     * {@code Pair} objects
     * @param value Original values. A non-empty array is expected
     * @param escapeSpace True to replace {@code " "} with a hyphen in the name part (useful for attribute names)
     * @return A list of pairs; can be an empty list
     */
    private static List<Pair<String, String>> toNameValuePairs(String[] value, boolean escapeSpace) {
        if (ArrayUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return Arrays
            .stream(value)
            .map(entry -> entry.split(OptionSourceParameters.KEV_VALUE_SEPARATOR_PATTERN, 2))
            .filter(parts -> ArrayUtils.isNotEmpty(parts)  && StringUtils.isNotEmpty(parts[0]))
            .map(parts -> {
                String left = parts[0].trim();
                if (escapeSpace) {
                    left = left.replaceAll(OptionSourceParameters.INLINE_COLON_PATTERN, CoreConstants.SEPARATOR_HYPHEN);
                }
                String right = parts.length > 1
                    ? parts[1].replaceAll(OptionSourceParameters.INLINE_COLON_PATTERN, CoreConstants.SEPARATOR_COLON).trim()
                    : left;
                return Pair.of(left, right);
            })
            .collect(Collectors.toList());
    }
}
