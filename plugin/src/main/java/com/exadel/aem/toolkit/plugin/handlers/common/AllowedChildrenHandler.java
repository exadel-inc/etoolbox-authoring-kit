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
import com.exadel.aem.toolkit.api.annotations.policies.PolicyMergeMode;
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
 * object. The source refers to the {@code updatecomponentlist} listener of the {@code cq:childEditConfig} or {@code
 * cq:editConfig} node of an AEM component
 */
public class AllowedChildrenHandler implements Handler {

    private static final String VALUE_POLICY_RESOLVER_FORMAT = "Granite.PolicyResolver.build('%s')";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new SimpleModule()
            .addSerializer(AllowedChildren.class, new AllowedChildrenSerializer()));

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code
     * Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        source.tryAdaptTo(AllowedChildren[].class).ifPresent(adaptation -> populatePolicies(adaptation, target));
    }

    /**
     * Processes data from {@code AllowedChildren[]} annotations and stores it into 'cq:listeners' node of the provided
     * {@code Target}
     * @param rules  {@code AllowedChildren[]} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    private static void populatePolicies(AllowedChildren[] rules, Target target) {
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
            .attribute(DialogConstants.PN_UPDATE_COMPONENT_LIST, String.format(VALUE_POLICY_RESOLVER_FORMAT, json));
    }

    /**
     * Gets whether the given {@link Target} is a representation of a {@code cq:editConfig} node of an AEM component
     * @param target {@code Target} instance
     * @return True or false
     */
    private static boolean isEditConfig(Target target) {
        return Scopes.CQ_EDIT_CONFIG.equals(target.getScope());
    }

    /* -------------
       Serialization
       ------------- */

    /**
     * Converts {@code List} of {@link AllowedChildren} annotations and boolean value representing target node to the
     * JSON format
     * @param rules        {@code List} of {@link AllowedChildren} annotations
     * @param isEditConfig True indicates that the listener specified in {@code cq:editConfig} is used. Otherwise, the
     *                     listener in {@code cq:childEditConfig} is used
     * @return True or false
     */
    private static String toJson(List<AllowedChildren> rules, boolean isEditConfig) {
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        objectNode.put("isEditConfig", isEditConfig);
        objectNode.set("rules", OBJECT_MAPPER.valueToTree(rules));
        return objectNode.toString();
    }

    /**
     * Represents {@link JsonSerializer} for storing the configuration set up via {@link AllowedChildren} in the content
     * repository
     */
    private static class AllowedChildrenSerializer extends JsonSerializer<AllowedChildren> {

        /**
         * Retrieves a JSON render of the provided {@code AllowedChildren} annotation
         * @param allowedChildren    {@code AllowedChildren} object
         * @param jsonGenerator      Managed {@code JsonGenerator} object
         * @param serializerProvider Managed {@code SerializerProvider} object
         * @throws IOException if the serialization fails
         */
        @Override
        public void serialize(
            AllowedChildren allowedChildren,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

            jsonGenerator.writeStartObject();
            serializeNonEmptyArray("value", allowedChildren.value(), jsonGenerator, serializerProvider);
            serializeNonEmptyArray("pageResourceTypes", allowedChildren.pageResourceTypes(), jsonGenerator, serializerProvider);
            serializeNonEmptyArray("templates", allowedChildren.templates(), jsonGenerator, serializerProvider);
            serializeNonEmptyArray("parentsResourceTypes", allowedChildren.parents(), jsonGenerator, serializerProvider);
            serializeNonEmptyArray("pagePaths", allowedChildren.pagePaths(), jsonGenerator, serializerProvider);
            serializeNonEmptyArray("containers", allowedChildren.resourceNames(), jsonGenerator, serializerProvider);
            if (!PolicyMergeMode.OVERRIDE.equals(allowedChildren.mode())) {
                serializerProvider.defaultSerializeField("mode", allowedChildren.mode(), jsonGenerator);
            }
            jsonGenerator.writeEndObject();
        }

        /**
         * Retrieves a JSON render of the provided array
         * @param fieldName          Name of the field being serialized
         * @param value              Value of the field
         * @param jsonGenerator      Managed {@code JsonGenerator} object
         * @param serializerProvider Managed {@code SerializerProvider} object
         * @throws IOException if the serialization fails
         */
        private void serializeNonEmptyArray(
            String fieldName,
            String[] value,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

            if (value.length == 0) {
                return;
            }
            serializerProvider.defaultSerializeField(fieldName, value, jsonGenerator);
        }
    }
}
