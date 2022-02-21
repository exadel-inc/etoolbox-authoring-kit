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

import com.exadel.aem.toolkit.api.annotations.main.ac.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllowedChildrenHandler implements Handler {

    private static final Gson GSON = new Gson();

    @Override
    public void accept(Source source, Target target) {
        source.tryAdaptTo(AllowedChildren[].class).ifPresent(adaptation -> populatePolicies(adaptation, target));
    }

    private void populatePolicies(AllowedChildren[] rules, Target target) {
        List<AllowedChildren> allowedChildrenList = Arrays.stream(rules)
                .filter(ac -> isEditConfig(target) == ac.applyToCurrent())
                .collect(Collectors.toList());
        if (allowedChildrenList.isEmpty()) {
            return;
        }
        String json = toJson(allowedChildrenList, isEditConfig(target));
        target
                .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG)
                .getOrCreateTarget(DialogConstants.NN_LISTENERS)
                .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_LISTENERS)
                .attribute(DialogConstants.PN_UPDATE_COMPONENT_LIST, String.format(DialogConstants.VALUE_POLICY_RESOLVER_FORMAT, json));
    }

    private boolean isEditConfig(Target target) {
        return Scopes.CQ_EDIT_CONFIG.equals(target.getScope());
    }

    private String toJson(List<AllowedChildren> rules, boolean isEditConfig) {
        return GSON.toJson(new Result(
                rules.stream().map(this::annotationToMap).collect(Collectors.toList()),
                isEditConfig)
        );
    }

    private Map<String, String[]> annotationToMap(AllowedChildren allowedChildren) {
        Map<String, String[]> map = new HashMap<>(6);
        map.put("value", allowedChildren.value());
        map.put("pageResourceTypes", nullIfEmpty(allowedChildren.pageResourceTypes()));
        map.put("templates", nullIfEmpty(allowedChildren.templates()));
        map.put("parentsResourceTypes", nullIfEmpty(allowedChildren.parentsResourceTypes()));
        map.put("pagePaths", nullIfEmpty(allowedChildren.pagePaths()));
        map.put("containers", nullIfEmpty(allowedChildren.resourceNames()));
        return map;
    }

    private String[] nullIfEmpty(String[] arr) {
        return arr.length != 0 ? arr : null;
    }

    private static class Result {

        private final boolean isEditConfig;
        private final List<Map<String, String[]>> rules;

        public Result(List<Map<String, String[]>> rules, boolean isEditConfig) {
            this.isEditConfig = isEditConfig;
            this.rules = rules;
        }

    }
}
