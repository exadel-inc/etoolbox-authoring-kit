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

import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the "updatecomponentlist" listener of the {@code cq:childEditConfig} or {@code cq:editConfig} node of an AEM component
 */
public class AllowedChildrenHandler implements Handler {

    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(AllowedChildren.class,
            (JsonSerializer<AllowedChildren>) AllowedChildrenHandler::serialize).create();

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
                .replace("'", "\\'");
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
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("isEditConfig", new JsonPrimitive(isEditConfig));
        jsonObject.add("rules", GSON.toJsonTree(rules));
        return jsonObject.toString();
    }

    /**
     * Converts {@link AllowedChildren} annotation to the {@link JsonElement}
     * @param allowedChildren {@link AllowedChildren} object used for data retrieval
     * @param type The actual type of the source object
     * @param context Context for serialization
     * @return JsonElement corresponding to the AllowedChildren annotation
     */
    private static JsonElement serialize(AllowedChildren allowedChildren, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("value", context.serialize(allowedChildren.value()));
        result.add("pageResourceTypes", context.serialize(allowedChildren.pageResourceTypes()));
        result.add("templates", context.serialize(allowedChildren.templates()));
        result.add("parentsResourceTypes", context.serialize(allowedChildren.parents()));
        result.add("pagePaths", context.serialize(allowedChildren.pagePaths()));
        result.add("containers", context.serialize(allowedChildren.resourceNames()));
        return result;
    }
}
