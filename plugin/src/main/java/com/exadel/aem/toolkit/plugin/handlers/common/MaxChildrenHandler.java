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

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.policies.MaxChildren;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyTarget;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object. The source refers to the {@code resolvemaxchildern} listener of the {@code
 * cq:editConfig} node of an AEM component
 */
public class MaxChildrenHandler implements Handler {
    /**
     * Name of the listener to resolve max children limit for `limited-parsys` js module
     */
    public static final String MAX_LIMIT_RESOLVER_NAME = "resolvemaxchildern";

    /**
     * Format of default max children limit resolver
     */
    private static final String MAX_LIMIT_RESOLVER_FORMAT = "() => %d";


    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code
     * Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        source.tryAdaptTo(MaxChildren.class).ifPresent(adaptation -> populate(adaptation, target));
    }

    /**
     * Processes data from {@code MaxChildren} annotation and stores it into 'cq:listeners' node of the provided
     * {@code Target}
     * @param rule  {@code MaxChildren} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    private static void populate(MaxChildren rule, Target target) {
        if (isEditConfig(target) == (PolicyTarget.CURRENT == rule.targetContainer())) {
            target
                .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG)
                .getOrCreateTarget(DialogConstants.NN_LISTENERS)
                .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_LISTENERS)
                .attribute(MAX_LIMIT_RESOLVER_NAME, String.format(MAX_LIMIT_RESOLVER_FORMAT, rule.value()));
        }
    }

    /**
     * Gets whether the given {@link Target} is a representation of a {@code cq:editConfig} node of an AEM component
     * @param target {@code Target} instance
     * @return True if the target is a {@code cq:editConfig} node; otherwise, false
     */
    private static boolean isEditConfig(Target target) {
        return Scopes.CQ_EDIT_CONFIG.equals(target.getScope());
    }
}
