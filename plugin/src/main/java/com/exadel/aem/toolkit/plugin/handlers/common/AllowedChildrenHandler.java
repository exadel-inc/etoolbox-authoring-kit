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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the "updatecomponentlist" listener of the {@code cq:childEditConfig} or {@code cq:editConfig} node of an AEM component
 */
public class AllowedChildrenHandler implements Handler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule()
                    .addSerializer(AllowedChildren.class, createSerializer()));

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        source.tryAdaptTo(AllowedChildren[].class).ifPresent(adaptation -> populatePolicies(adaptation, target));
    }

    /**
     * Processes data from {@code AllowedChildren[]} annotations and stores it into 'cq:listeners' node of the provided {@code Target}
     * @param rules {@code AllowedChildren[]} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    private void populatePolicies(AllowedChildren[] rules, Target target) {
        List<AllowedChildren> allowedChildrenList = Arrays.stream(rules)
                .filter(ac -> isEditConfig(target) == ac.applyToCurrent())
                .collect(Collectors.toList());
        if (allowedChildrenList.isEmpty()) {
            return;
        }
        String json = toJson(allowedChildrenList, isEditConfig(target))
                .replace("'", "\\\\'");
        target
                .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG)
                .getOrCreateTarget(DialogConstants.NN_LISTENERS)
                .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_LISTENERS)
                .attribute(DialogConstants.PN_UPDATE_COMPONENT_LIST, String.format(DialogConstants.VALUE_POLICY_RESOLVER_FORMAT, json));
    }

    /**
     * Gets whether the given {@link Target} is a representation of a {@code cq:editConfig} node of an AEM component
     * @param target {@code Target} instance
     * @return True or false
     */
    private boolean isEditConfig(Target target) {
        return Scopes.CQ_EDIT_CONFIG.equals(target.getScope());
    }

    /**
     * Converts {@code List} of {@link AllowedChildren} annotations and boolean value representing target node to JSON format.
     * @param rules {@code List} of {@link AllowedChildren} annotations
     * @param isEditConfig True indicates that listener specified in {@code cq:editConfig}; otherwise, in {@code cq:childEditConfig}
     * @return True or false
     */
    private String toJson(List<AllowedChildren> rules, boolean isEditConfig) {
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        objectNode.put("isEditConfig", isEditConfig);
        objectNode.set("rules", OBJECT_MAPPER.valueToTree(rules));
        return objectNode.toString();
    }

    /**
     * Creates {@link JsonSerializer} to serialize {@link AllowedChildren} annotation to the Json
     * @return JsonSerializer corresponding to the AllowedChildren annotation
     */
    private static JsonSerializer<AllowedChildren> createSerializer() {
        return new JsonSerializer<AllowedChildren>() {
            @Override
            public void serialize(AllowedChildren allowedChildren, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeStartObject();
                serializeNonEmptyStringArray("value", allowedChildren.value(), jsonGenerator, serializerProvider);
                serializeNonEmptyStringArray("pageResourceTypes", allowedChildren.pageResourceTypes(), jsonGenerator, serializerProvider);
                serializeNonEmptyStringArray("templates", allowedChildren.templates(), jsonGenerator, serializerProvider);
                serializeNonEmptyStringArray("parentsResourceTypes", allowedChildren.parents(), jsonGenerator, serializerProvider);
                serializeNonEmptyStringArray("pagePaths", allowedChildren.pagePaths(), jsonGenerator, serializerProvider);
                serializeNonEmptyStringArray("containers", allowedChildren.resourceNames(), jsonGenerator, serializerProvider);
                jsonGenerator.writeEndObject();
            }

            private void serializeNonEmptyStringArray(String fieldName,
                                                      String[] value,
                                                      JsonGenerator jsonGenerator,
                                                      SerializerProvider serializerProvider) throws IOException {
                if (value.length == 0) {
                    return;
                }
                serializerProvider.defaultSerializeField(fieldName, value, jsonGenerator);
            }
        };
    }
}
