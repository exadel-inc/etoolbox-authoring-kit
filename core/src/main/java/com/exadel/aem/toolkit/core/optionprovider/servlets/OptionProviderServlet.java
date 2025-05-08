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
package com.exadel.aem.toolkit.core.optionprovider.servlets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Allows setting a custom data source for a widget that works with Granite datasources, such as a {@link RadioGroup}
 * or a {@link Select}. Supports a number of settings that are either stored as attributes of the {@code datasource} node
 * in Granite UI setup or passed to an HTTP request as query arguments. Datasource options are rendered from either
 * a node tree, an EToolbox List / ACS List-like node structure ({@code [...]/node/jcr:content/list/[items]}),
 * or a tag folder
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/apps/" + ResourceTypes.OPTION_PROVIDER,
        "sling.servlet.resourceTypes=/apps/" + ResourceTypes.OPTION_PROVIDER,
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    })
public class OptionProviderServlet extends SlingSafeMethodsServlet {

    private static final String QUERY_KEY_OUTPUT = "output";
    private static final String QUERY_VALUE_JSON = "json";

    @Reference
    private transient OptionProviderService optionProvider;

    /**
     * Processes HTTP GET requests to the current endpoint and outputs a {@link SimpleDataSource} or a JSON string
     * according to the query parameters
     * @param request  {@code SlingHttpServletRequest} instance
     * @param response {@code SlingHttpServletResponse} instance
     */
    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response)
        throws ServletException, IOException {

        List<Resource> options = optionProvider.getOptions(request);

        if (!isJsonOutput(request)) {
            DataSource ds = new SimpleDataSource(options.iterator());
            request.setAttribute(DataSource.class.getName(), ds);
            return;
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setContentType(CoreConstants.CONTENT_TYPE_JSON);
        if (CollectionUtils.isEmpty(options)) {
            response.getWriter().write(CoreConstants.ARRAY_OPENING + CoreConstants.ARRAY_CLOSING);
            return;
        }
        try {
            response.getWriter().print(getJsonOutput(options));
        } catch (JsonProcessingException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Gets whether the current request is for JSON output judging by the request path or parameters
     * @param request {@code SlingHttpServletRequest} object
     * @return True or false
     */
    private static boolean isJsonOutput(SlingHttpServletRequest request) {
        if (QUERY_VALUE_JSON.equalsIgnoreCase(request.getRequestPathInfo().getExtension())) {
            return true;
        }
        RequestParameter jsonParameter = request.getRequestParameter(QUERY_KEY_OUTPUT);
        if (jsonParameter == null) {
            return false;
        }
        return jsonParameter.toString().equalsIgnoreCase(QUERY_VALUE_JSON);
    }

    /**
     * Generates the JSON representation of the option list as requested by the user
     * @param options List of datasource options
     * @return JSON string
     * @throws JsonProcessingException in case JSON serialization fails
     */
    private static String getJsonOutput(List<Resource> options) throws JsonProcessingException {
        List<Map<String, Object>> jsonEntries = options
            .stream()
            .map(resource -> {
                ValueMap valueMap = resource.getValueMap();
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put(CoreConstants.PN_TEXT, valueMap.get(CoreConstants.PN_TEXT));
                entry.put(CoreConstants.PN_VALUE, valueMap.get(CoreConstants.PN_VALUE));
                appendResourceAttributes(entry, resource);
                return entry;
            })
            .collect(Collectors.toList());
        return new ObjectMapper().writeValueAsString(jsonEntries);
    }

    /**
     * Called by {@link OptionProviderServlet#getJsonOutput(List)} to create the attribute section on a JSON entity
     * representing a single datasource option
     * @param container {@code Map} object that accumulates JSON data
     * @param entry     {@code Resource} to take data from
     */
    private static void appendResourceAttributes(Map<String, Object> container, Resource entry) {
        Resource graniteData = entry.getChild(CoreConstants.NN_GRANITE_DATA);
        if (graniteData == null) {
            return;
        }
        Map<String, String> attributes = graniteData.getValueMap().entrySet().stream()
            .filter(e -> e.getValue() != null)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().toString()));
        container.put(CoreConstants.NN_GRANITE_DATA, attributes);
    }
}
