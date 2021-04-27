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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Implements {@link OptionProviderService} to prepare option sets for Granite-compliant custom data sources
 * used in Granite UI widgets
 */
@Component(service = OptionProviderService.class)
public class OptionProviderServiceImpl implements OptionProviderService {

    private static final String FULL_STRING_MATCH_TEMPLATE = "^%s$";
    private static final String USER_WILDCARD_PATTERN = "(?<![\\\\'])\\*";
    private static final String REGEXP_WILDCARD_PATTERN = ".*";


    /**
    * {@inheritDoc}
     */
    @Override
    public List<Resource> getOptions(SlingHttpServletRequest request) {
        Set<Option> options = new LinkedHashSet<>();
        List<Option> result;

        // Parse user-specified datasource settings from the request and/or underlying "datasource" resource,
        OptionSourceParameters parameters = OptionSourceParameters.forRequest(request);

        // For each of the datasource paths, try retrieve a list of JCR-stored options
        for (PathParameters pathParameters : parameters.getPathParameters()) {
            Resource datasourceResource = OptionSourceResolver.resolve(request, pathParameters.getPath(), pathParameters.getFallbackPath());
            if (datasourceResource == null || datasourceResource instanceof NonExistingResource) {
                continue;
            }
            options.addAll(getOptions(datasourceResource, pathParameters));
        }

        // Transform the resulting collection to sortable list and sort it as optioned by user
        result = new ArrayList<>(options);
        if (parameters.isSorted()) {
            result.sort(Option.COMPARATOR);
        }

        // Extract "prepended" and "appended" options from the user-provided params; preserve only those of them
        // that do not have values already present in the "original" list
        result.addAll(0, getExtraOptions(request.getResource().getResourceResolver(),
                parameters.getPrependedOptions(),
                options));
        result.addAll(getExtraOptions(request.getResource().getResourceResolver(),
                parameters.getAppendedOptions(),
                options));

        // Remove options that are specified in "exclude" parameter
        removeExcludedOptions(result, parameters.getExcludedOptions());

        // Set "selected" flag to appropriate option(s) if "selected value" parameter is specified
        if (StringUtils.isNotBlank(parameters.getSelectedValue())) {
            result.stream()
                    .filter(option -> StringUtils.equals(option.getValue(), parameters.getSelectedValue())
                            || StringUtils.equals(option.getText(), parameters.getSelectedValue()))
                    .forEach(Option::select);
        }

        // Render the resulting collection
        return result.stream()
                .map(Option::toValueMapEntry)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Called from {@link OptionProviderServiceImpl#getOptions(SlingHttpServletRequest)} to extract a list
     * of {@link Option} items from the particular datasource
     * @param dataSource {@code Resource} instance representing selected datasource
     * @param parameters Path-related user settings that came with the request
     * @return {@code List<DataSourceOption>} object, or an empty list
     */
    private List<Option> getOptions(Resource dataSource,
                                    PathParameters parameters) {
        final String defaultValueMember = !OptionSourceResolver.isTagCollection(dataSource)
                ? CoreConstants.PN_VALUE
                : CoreConstants.PARAMETER_ID;
        return StreamSupport.stream(dataSource.getChildren().spliterator(), false)
                .filter(child -> !child.getName().equals(JcrConstants.JCR_CONTENT)) // jcr:content nodes are excluded
                .map(child -> Option.builder()                                      // from the option sources
                        .resource(child)
                        .textMember(parameters.getTextMember())
                        .valueMember(StringUtils.defaultString(parameters.getValueMember(), defaultValueMember))
                        .attributeMembers(parameters.getAttributeMembers())
                        .attributes(parameters.getAttributes())
                        .textTransform(parameters.getTextTransform())
                        .valueTransform(parameters.getValueTransform())
                        .build())
                .filter(Option::isValid)
                .collect(Collectors.toList());
    }

    /**
     * Generates a list of user-specified extra options that are outside the normal JCR structure
     * @param resourceResolver {@code ResourceResolver} instance to create "virtual" datasource entry resource
     * @param optionStrings    Array of strings representing extra options
     * @param skip             List of the [already existing] options to omit in the extra options collection
     * @return List of "virtual" datasource items resources that can be merged to the original resource list
     */
    private static List<Option> getExtraOptions(ResourceResolver resourceResolver,
                                                String[] optionStrings,
                                                Set<Option> skip) {
        if (optionStrings == null) {
            return Collections.emptyList();
        }
        return Stream.of(optionStrings)
                .map(option -> option.split(OptionSourceParameters.KEV_VALUE_SEPARATOR_PATTERN, 2))
                .filter(parts -> ArrayUtils.getLength(parts) == 2 && StringUtils.isNotBlank(parts[0]))
                .map(parts -> Pair.of(
                        parts[0].trim().replaceAll(OptionSourceParameters.INLINE_COLON_PATTERN, CoreConstants.SEPARATOR_COLON),
                        parts[1].trim().replaceAll(OptionSourceParameters.INLINE_COLON_PATTERN, CoreConstants.SEPARATOR_COLON)))
                .filter(partsPair -> skip.stream().noneMatch(opt -> partsPair.getRight().equals(opt.getValue())))
                .map(partsPair -> Option.builder()
                        .resourceResolver(resourceResolver)
                        .text(partsPair.getLeft())
                        .value(partsPair.getRight())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Browses through the provided list of options and removes the items that match the user-provided {@code exclude}
     * setting
     * @param options Collection of {@code Option} objects to test
     * @param excludeStrings Value of the user-specified {@code exclude} setting
     */
    private static void removeExcludedOptions(List<Option> options, String[] excludeStrings) {
        if (CollectionUtils.isEmpty(options) || ArrayUtils.isEmpty(excludeStrings)) {
            return;
        }
        List<Pattern> patterns = Arrays.stream(excludeStrings)
            .filter(StringUtils::isNotBlank)
            .map(str -> String.format(FULL_STRING_MATCH_TEMPLATE, str.replaceAll(USER_WILDCARD_PATTERN, REGEXP_WILDCARD_PATTERN)))
            .map(str -> Pattern.compile(str, Pattern.CASE_INSENSITIVE))
            .collect(Collectors.toList());

        List<Option> excludedOptions = options
            .stream()
            .filter(option -> patterns.stream().anyMatch(pattern -> pattern.matcher(option.getValue()).matches()
                || pattern.matcher(option.getText()).matches()))
            .collect(Collectors.toList());
        options.removeAll(excludedOptions);
    }
}
