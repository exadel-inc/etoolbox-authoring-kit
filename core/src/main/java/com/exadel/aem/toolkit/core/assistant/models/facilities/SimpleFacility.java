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

public abstract class SimpleFacility implements Facility {

    protected static final String ICON_IMAGE_ADD = "imageAdd";
    protected static final String ICON_TEXT_ADD = "textAdd";
    protected static final String ICON_TEXT_EDIT = "textEdit";
    protected static final String ICON_TEXT_REMOVE = "textExclude";

    protected SimpleFacility() {
    }

    @SuppressWarnings("unused") // Used to render JSON output
    public String getVendorName() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<Facility> getVariants() {
        return Collections.emptyList();
    }

    public List<Setting> getSettings() {
        return Collections.emptyList();
    }

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
