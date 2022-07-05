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
package com.exadel.aem.toolkit.api.annotations.policies;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows specifying the set of allowed child components for an AEM container component. Every {@code AllowedChildren}
 * annotation represents a single rule. One can specify any number of rules that are considered in the given order. If
 * an "empty" rule is specified, the current container doesn't allow any children
 * @see AllowedChildrenConfig
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllowedChildrenConfig.class)
public @interface AllowedChildren {

    /**
     * Used to specify the allowed children for the container. This property accepts an array of strings. Every item can
     * represent either a resource type or a group. E.g.: <br> {@code "acme/components/content/button", "group:My
     * Components"}
     * <p> If this property is not specified, no children components are allowed.
     * @return Zero or more {@code String} values, non-blank
     */
    String[] value();

    /**
     * Used to specify page templates to which the current rule is applied. If this setting is skipped, the rule applies
     * to any template.
     * <p>You can use wildcards, e.g. {@code "/conf/acme/settings/wcm/*", "*templates/design2/homepage",
     * "*templates/design2/*"}.
     * @return Optional {@code String} value, or an array of strings
     */
    String[] templates() default {};

    /**
     * Used to specify page resource types to which the current rule is applied. If this setting is skipped, the rule
     * applies to any page.
     * <p>You can use wildcards, e.g. {@code "acme/pages/design2/*", "*design2/homepage",
     * "*pages/design2/*"}.
     * @return Optional {@code String} value, or an array of strings
     */
    String[] pageResourceTypes() default {};

    /**
     * Used to specify resource types and/or groups of parent components. The rule is applied if these components are
     * present in the hierarchy of current resource. If this setting is skipped, the rule is not restricted by parents.
     * <p>You can use wildcards, e.g. {@code "acme/pages/design2/*", "*design2/homepage",
     * "*pages/design2/*"}.
     * <p><u>Note</u>: every item can represent either a single parent or a "chain" - a succession of
     * parents/groups. In this case, successive parents are divided with a space. E.g. {@code "acme/pages/design2/*
     * *design2/homepage *pages/design2/*"} means that among the parents of the current resource there must be component
     * with resource type matching "acme/pages/design2/*", and below it there must be another component with resource
     * type matching "*design2/homepage". They need not be direct parent and child: any level of nesting is allowed.
     * This works much like a sequence of CSS selectors.
     * <p><u>Note</u>: If you use sequences of parents within one entry and one of them is a group with a space in its
     * name, you should escape it with single quotes or backticks. E.g. {@code "acme/pages/design2/* group:'My Group
     * Name' group:GroupName *pages/design2/*"}
     * @return Optional {@code String} value, or an array of strings
     */
    String[] parents() default {};

    /**
     * Used to specify paths of the pages to which the current rule should be applied. If this setting is skipped, the
     * rule applies to any page.
     * <p>You can use wildcards, e.g. {@code "we-retail/language-masters/en/*", "*en/experience",
     * "*language-masters/en*"}.
     * @return Optional {@code String} value, or an array of strings
     */
    String[] pagePaths() default {};

    /**
     * Used to specify node names of "parsys-like" containers to which the current rule should be applied. If this
     * setting is skipped, the rule applies to any container
     * @return Optional {@code String} value, or an array of strings
     */
    String[] resourceNames() default {};

    /**
     * Used to specify target node for rules. If set to {@link PolicyTarget#CURRENT} the rule applies
     * to the current annotated component. Otherwise, the rule is applied to a container nested within the current
     * component. E.g. if the current component is an inheritor of parsys, setting {@code targetContainer} to {@link
     * PolicyTarget#CURRENT} means that the rule affects which components can be added to the current
     * one. But if the current component contains a parsys inside you need to skip {@code targetContainer} or set it to
     * {@link PolicyTarget#CHILD} so that the rule applies to the parsys.
     * <p><u>Note</u>: if the component contains more than one parsys, you can specify the particular target for the
     * rule using the {@code resourceNames} setting
     * @return {@link PolicyTarget} value
     */
    PolicyTarget targetContainer() default PolicyTarget.CHILD;

    /**
     * Used to specify the mode of merging policies defined via {@link AllowedChildren#value()} with original component
     * policies (designs).
     * <br>{@link PolicyMergeMode#OVERRIDE} (default) removes from the component selection menu all the entries that are
     * defined by the conventional policy (design) and shows only the entries defined by {@code AllowedChildren};
     * <br>{@link PolicyMergeMode#MERGE} adds entries defined by {@code AllowedChildren} to the original list
     * @return One of the {@link PolicyMergeMode} options
     */
    PolicyMergeMode mode() default PolicyMergeMode.OVERRIDE;
}
