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
                .getOrCreateTarget(DialogConstants.NN_LISTENERS)
                .attribute(DialogConstants.PN_UPDATE_COMPONENT_LIST, String.format(DialogConstants.VALUE_POLICY_RESOLVER_FORMAT, json));
    }

    private boolean isEditConfig(Target target) {
        return Scopes.CQ_EDIT_CONFIG.equals(target.getScope());
    }

    private String toJson(List<AllowedChildren> rules, boolean isEditConfig) {
        return new Gson().toJson(new Result(
                rules.stream().map(this::annotationToMap).collect(Collectors.toList()),
                isEditConfig)
        );
    }

    private Map<String, String[]> annotationToMap(AllowedChildren allowedChildren) {
        Map<String, String[]> map = new HashMap<>(6);
        map.put("value", allowedChildren.value());
        map.put("pageResourceTypes", allowedChildren.pageResourceTypes());
        map.put("templates", allowedChildren.templates());
        map.put("parentsResourceTypes", allowedChildren.parentsResourceTypes());
        map.put("pagePaths", allowedChildren.pagePaths());
        map.put("containers", allowedChildren.resourceNames());
        return map;
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
