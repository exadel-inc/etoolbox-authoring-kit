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
package com.exadel.aem.toolkit.core.assistant.models.facilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a facility that does not have variants
 * @see Facility
 */
public abstract class SimpleFacility implements Facility {

    protected static final String ICON_IMAGE_ADD = "imageAdd";
    protected static final String ICON_TEXT_ADD = "textAdd";
    protected static final String ICON_TEXT_EDIT = "textEdit";
    protected static final String ICON_TEXT_REMOVE = "textExclude";

    protected static final String EXCEPTION_INVALID_REQUEST = "Invalid request";

    /**
     * Default constructor
     */
    protected SimpleFacility() {
    }

    /**
     * Retrieves the name of the vendor 9a built-in or a 3rd-party service that provides this facility
     */
    @SuppressWarnings("unused") // Used to render JSON output
    public String getVendorName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public final List<Facility> getVariants() {
        return Collections.emptyList();
    }

    /**
     * Retrieves an optional set of properties (or else, "flags") that may affect the display and interaction with the
     * current {@code Facility} in the UI
     * @return An optional map with string-typed keys and arbitrary values
     */
    public Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }

    /**
     * Returns a list of {@link Setting}s for this facility
     * @return A non-null {@code List} instance; can be empty
     */
    public List<Setting> getSettings() {
        return Collections.emptyList();
    }

    /**
     * Extracts from the provided request the arguments for the execution of this facility
     * @param request {@link SlingHttpServletRequest} instance
     * @return {@link ValueMap} containing the arguments
     */
    protected static ValueMap getArguments(SlingHttpServletRequest request) {
        RequestParameterMap parameters = request.getRequestParameterMap();
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, RequestParameter[]> entry : parameters.entrySet()) {
            RequestParameter[] values = entry.getValue();
            if (values == null) {
                continue;
            }
            if (values.length == 1) {
                result.put(entry.getKey(), values[0].getString());
            } else {
                result.put(entry.getKey(), Arrays.stream(values).map(RequestParameter::getString).toArray(String[]::new));
            }
        }
        return new ValueMapDecorator(result);
    }
}
