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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.exadel.aem.toolkit.core.optionprovider.OptionProviderConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

/**
 * Implements {@link OptionSourceResolver} to transfer the directly provided name-value pairs into the options data
 * source
 */
class InlineOptionSourceResolver implements OptionSourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(InlineOptionSourceResolver.class);

    private static final String EXCEPTION_COULD_NOT_PARSE = "Could not parse the inline options";

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resolve(SlingHttpServletRequest request, PathParameters params) {
        ArrayNode jsonArray;
        try {
             jsonArray = (ArrayNode) ObjectConversionUtil.toNodeTree(params.getPath());
        } catch (IOException | ClassCastException e) {
            LOG.error(EXCEPTION_COULD_NOT_PARSE, e);
            return null;
        }

        List<Resource> children = new ArrayList<>();
        for (Iterator<JsonNode> nodes = jsonArray.elements(); nodes.hasNext();) {
            JsonNode node = nodes.next();
            if (!(node instanceof ObjectNode)) {
                continue;
            }
            ValueMapBuilder valueMapBuilder = new ValueMapBuilder();
            for (Iterator<String> propertyNames = node.fieldNames(); propertyNames.hasNext();) {
                String propertyName = propertyNames.next();
                String propertyValue = node.get(propertyName).asText();
                valueMapBuilder.put(propertyName, propertyValue);
                if (propertyName.equals(params.getTextMember()) && StringUtils.isNotEmpty(propertyValue)) {
                    valueMapBuilder.put(OptionProviderConstants.PARAMETER_NAME, propertyValue);
                }
            }
            ValueMap valueMap = valueMapBuilder.build();
            Resource child = new ValueMapResource(
                request.getResourceResolver(),
                valueMap.get(OptionProviderConstants.PARAMETER_NAME, StringUtils.EMPTY),
                JcrConstants.NT_UNSTRUCTURED,
                valueMap);
            children.add(child);
        }
        return new ValueMapResource(
            request.getResourceResolver(),
            StringUtils.EMPTY,
            JcrConstants.NT_UNSTRUCTURED,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
    }
}
