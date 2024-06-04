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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyMergeMode;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyTarget;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.utils.ArrayUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

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
        ComponentSource componentSource = source.adaptTo(ComponentSource.class);
        if (componentSource != null) {
            AllowedChildren[] rules = componentSource.getViews()
                .stream()
                .map(viewSource -> viewSource.adaptTo(AllowedChildren[].class))
                .filter(ArrayUtils::isNotEmpty)
                .flatMap(Arrays::stream)
                .toArray(AllowedChildren[]::new);
            populatePolicies(rules, target);
        } else {
            source.tryAdaptTo(AllowedChildren[].class).ifPresent(rules -> populatePolicies(rules, target));
        }
    }

    /**
     * Processes data from {@code AllowedChildren[]} annotations and stores it into 'cq:listeners' node of the provided
     * {@code Target}
     * @param rules  {@code AllowedChildren[]} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    private static void populatePolicies(AllowedChildren[] rules, Target target) {
        List<AllowedChildren> allowedChildrenList = Arrays.stream(rules)
            .filter(rule -> isEditConfig(target) == (PolicyTarget.CURRENT == rule.targetContainer()))
            .map(AllowedChildrenHandler::combineValues)
            .collect(RuleAccumulator::new, RuleAccumulator::add, RuleAccumulator::addAll)
            .getList();
        if (allowedChildrenList.isEmpty()) {
            return;
        }
        String json = toJson(allowedChildrenList, isEditConfig(target))
            .replace("'", "\\\\'");
        target
            .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG)
            .getOrCreateTarget(DialogConstants.NN_LISTENERS)
            .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_LISTENERS)
            .attribute(CoreConstants.PN_UPDATE_COMPONENT_LIST, String.format(VALUE_POLICY_RESOLVER_FORMAT, json));
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
       Merging rules
       ------------- */

    /**
     * Combines the path- (resource type-) related properties of an {@link AllowedChildren} rule to simplify the build-up
     * of a rule being serialized
     * @param rule {@link AllowedChildren} instance
     * @return Modified {@link AllowedChildren} instance
     */
    private static AllowedChildren combineValues(AllowedChildren rule) {
        if (ArrayUtils.isEmpty(rule.classes())) {
            return rule;
        }
        List<String> effectiveValues = new ArrayList<>(Arrays.asList(rule.value()));
        Arrays.stream(rule.classes())
            .map(cls -> PluginRuntime.context().getReflection().getComponent(cls).getJcrPath())
            .filter(StringUtils::isNotBlank)
            .forEach(effectiveValues::add);
        Map<String, Object> modification = Collections.singletonMap(
            CoreConstants.PN_VALUE,
            effectiveValues.stream().distinct().toArray(String[]::new));
        return (AllowedChildren) Metadata.from(rule, modification);
    }

    /**
     * Combines the path- (resource type-) related properties of two different {@link AllowedChildren} rules. This method
     * assumes that {@link #combineValues(AllowedChildren)} has been called before and considers only the contents of the
     * {@code value} property
     * @param left {@link AllowedChildren} instance which stands for the base of combining
     * @param right {@link AllowedChildren} instance which stands for the extension of combining
     * @return The combined {@link AllowedChildren} instance
     */
    private static AllowedChildren combineValues(AllowedChildren left, AllowedChildren right) {
        if (ArrayUtils.isEmpty(right.value())) {
            return left;
        }
        if (ArrayUtils.isEmpty(left.value())) {
            return right;
        }
        String[] mergedValues = Stream.concat(Arrays.stream(left.value()), Arrays.stream(right.value()))
            .distinct()
            .toArray(String[]::new);
        Map<String, Object> modification = Collections.singletonMap(CoreConstants.PN_VALUE, mergedValues);
        return (AllowedChildren) Metadata.from(left, modification);
    }

    /**
     * Called by {@link #populatePolicies(AllowedChildren[], Target)} to reduce the stream of {@link AllowedChildren}
     * rules by merging the items that are only different in their {@code value} properties
     */
    private static class RuleAccumulator {
        private final List<AllowedChildren> rules = new ArrayList<>();

        /**
         * Adds a new {@link AllowedChildren} rule to the accumulator. If a rule with the same properties already exists,
         * the content of the {@code value} property is merged into the existing rule
         * @param rule {@code AllowedChildren} object
         */
        void add(AllowedChildren rule) {
            if (rule == null) {
                return;
            }
            if (rules.isEmpty()) {
                rules.add(rule);
                return;
            }
            AllowedChildren existingMatch = rules.stream().filter(r -> isMatch(r, rule)).findFirst().orElse(null);
            if (existingMatch != null) {
                rules.set(rules.indexOf(existingMatch), combineValues(existingMatch, rule));
            } else {
                rules.add(rule);
            }
        }

        /**
         * Adds all {@link AllowedChildren} rules from another {@code RuleAccumulator} instance to the current one
         * @param other {@code RuleAccumulator} object
         */
        void addAll(RuleAccumulator other) {
            if (other == null || CollectionUtils.isEmpty(other.getList())) {
                return;
            }
            other.getList().forEach(this::add);
        }

        /**
         * Retrieves the list of {@link AllowedChildren} rules accumulated so far
         * @return A non-null {@code List} instance
         */
        List<AllowedChildren> getList() {
            return rules;
        }

        /**
         * Gets whether the two {@link AllowedChildren} rules are equal in all requisites except the {@code value}
         * property
         * @param left  A nullable {@code AllowedChildren} object
         * @param right A nullable {@code AllowedChildren} object
         * @return True or false
         */
        private static boolean isMatch(AllowedChildren left, AllowedChildren right) {
            if (left == null || right == null) {
                return false;
            }
            return left.mode() == right.mode()
                && left.targetContainer() == right.targetContainer()
                && ArrayUtil.equals(left.pagePaths(), right.pagePaths())
                && ArrayUtil.equals(left.pageResourceTypes(), right.pageResourceTypes())
                && ArrayUtil.equals(left.parents(), right.parents())
                && ArrayUtil.equals(left.resourceNames(), right.resourceNames())
                && ArrayUtil.equals(left.templates(), right.templates());
        }
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
            serializeNonEmptyArray(
                "value",
                ArrayUtil.flatten(allowedChildren.value()),
                jsonGenerator,
                serializerProvider);
            serializeNonEmptyArray(
                "pageResourceTypes",
                ArrayUtil.flatten(allowedChildren.pageResourceTypes()),
                jsonGenerator,
                serializerProvider);
            serializeNonEmptyArray(
                "templates",
                ArrayUtil.flatten(allowedChildren.templates()),
                jsonGenerator,
                serializerProvider);
            serializeNonEmptyArray(
                "parentsResourceTypes",
                allowedChildren.parents(),
                jsonGenerator,
                serializerProvider);
            serializeNonEmptyArray(
                "pagePaths",
                ArrayUtil.flatten(allowedChildren.pagePaths()),
                jsonGenerator,
                serializerProvider);
            serializeNonEmptyArray(
                "containers",
                ArrayUtil.flatten(allowedChildren.resourceNames()),
                jsonGenerator,
                serializerProvider);
            if (PolicyMergeMode.OVERRIDE != allowedChildren.mode()) {
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
