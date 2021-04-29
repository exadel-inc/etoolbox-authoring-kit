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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONException;
import org.json.JSONWriter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Allows setting custom data source for a widget that is supporting Granite datasources, such as a {@link RadioGroup}
 * or a {@link Select}. Supports a number of settings that are either stored as attributes of the {@code datasource} node
 * in Granite UI setup or passed in an HTTP request as query arguments. Datasource options are rendered from either
 * a node tree, an EToolbox List / ACS List -like node structure ({@code [...]/node/jcr:content/list/[items]}),
 * or a tag folder
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/apps/" + ResourceTypes.OPTION_PROVIDER,
        "sling.servlet.resourceTypes=/apps/" + ResourceTypes.OPTION_PROVIDER,
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
    })
public class OptionProviderServlet extends SlingSafeMethodsServlet {
    private static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";

    private static final String QUERY_KEY_OUTPUT = "output";
    private static final String QUERY_VALUE_JSON = "json";

    private static final String ATTRIBUTE_TEXT = "text";
    private static final String ATTRIBUTE_VALUE = "value";

    private static final String NODE_GRANITE_DATA = "granite:data";

    @Reference
    private transient OptionProviderService optionProvider;

    /**
     * Processes HTTP GET requests to the current endpoint and outputs a {@link SimpleDataSource} or a JSON string
     * according to the query parameters
     * @param request  {@code SlingHttpServletRequest} instance
     * @param response {@code SlingHttpServletResponse} instance
     */
    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {

        List<Resource> options = optionProvider.getOptions(request);

        if (CollectionUtils.isEmpty(options) && isJsonOutput(request)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (isJsonOutput(request)) {
            response.setContentType(CONTENT_TYPE_JSON);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            try {
                response.getWriter().print(getJsonOutput(options));
            } catch (JSONException | NullPointerException e) {
                throw new ServletException(e);
            }
            return;
        }

        DataSource ds = new SimpleDataSource(options.iterator());
        request.setAttribute(DataSource.class.getName(), ds);
    }

    /**
     * Gets whether the current request has the {@code output=json} parameter
     * @param request {@code SlingHttpServletRequest} object
     * @return True or false
     */
    private static boolean isJsonOutput(SlingHttpServletRequest request) {
        RequestParameter jsonParameter = request.getRequestParameter(QUERY_KEY_OUTPUT);
        if (jsonParameter == null) {
            return false;
        }
        return jsonParameter.toString().equalsIgnoreCase(QUERY_VALUE_JSON);
    }

    /**
     * Generates the JSON representation of the options list as requested by the user
     * @param entries List of datasource options
     * @return JSON string
     */
    private static String getJsonOutput(List<Resource> entries) throws IOException, JSONException {
        try (StringWriter stringWriter = new StringWriter()) {
            JSONWriter jsonWriter = new JSONWriter(stringWriter);
            jsonWriter.array();
            for (Resource entry : entries) {
                jsonWriter.object();
                writeResourceAttributes(jsonWriter, entry);
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            return stringWriter.toString();
        }
    }

    /**
     * Called by {@link OptionProviderServlet#getJsonOutput(List)} to create internals on a JSON entity representing
     * a single datasource option
     * @param writer {@code JSONWriter} instance
     * @param entry  {@code Resource} to take data from
     */
    private static void writeResourceAttributes(JSONWriter writer, Resource entry) throws JSONException {
        writer.key(ATTRIBUTE_TEXT).value(entry.getValueMap().get(ATTRIBUTE_TEXT, String.class));
        writer.key(ATTRIBUTE_VALUE).value(entry.getValueMap().get(ATTRIBUTE_VALUE, String.class));
        Resource graniteData = entry.getChild(NODE_GRANITE_DATA);
        if (graniteData == null) {
            return;
        }
        writer.key(NODE_GRANITE_DATA).object();
        for (String attribute : graniteData.getValueMap().keySet()) {
            writer.key(attribute).value(graniteData.getValueMap().get(attribute, String.class));
        }
        writer.endObject();
    }
}
