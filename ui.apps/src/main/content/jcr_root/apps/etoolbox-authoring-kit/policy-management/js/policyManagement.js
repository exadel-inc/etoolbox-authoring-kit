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

/**
 * Provides methods for managing policies set up via {@code AllowedChildren} or a similar mechanism
 */
(function (ns) {
    'use strict';

    ns.PolicyResolver = ns.PolicyResolver || {};

    ns.PolicyResolver.cache = new Map();

    ns.PolicyResolver.build = function (rules) {
        return function (cell, allowed, componentList) {
            resolve(allowed, componentList, this, rules);
        };
    };

    /**
     * Modifies the list of allowed components for the current container according to the set of rules
     * @param allowed - array of allowed components; modified within the method by reference
     * @param componentList - list of all components available in the instance
     * @param editable - current container
     * @param configJson - serialized set of rules
     */
    function resolve(allowed, componentList, editable, configJson) {
        if (ns.PolicyResolver.cache.has(editable.path)) {
            allowed.length = 0;
            allowed.push(...ns.PolicyResolver.cache.get(editable.path));
            return;
        }

        const config = JSON.parse(configJson);
        const settings = getContainerProperties(editable, ns.author, !config.isEditConfig);
        const applicableRule = config.rules.find(rule => isRuleApplicable(rule, settings, componentList));

        if (applicableRule) {
            applyRule(applicableRule, allowed);
        }
        ns.PolicyResolver.cache.set(editable.path, allowed);
    }

    /**
     * Retrieves properties of the current container
     * @param editable - current container
     * @param graniteAuthor - native Granite object used to retrieve page data
     * @param skipFirstParent - true if the policy applied via childEditConfig so that the user does not need to specify
     * the well-known container in the rule. E.g., when adding a rule to the component that has a nested parsys we don't
     * need to specify the name of the component itself
     */
    function getContainerProperties(editable, graniteAuthor, skipFirstParent) {
        return {
            template: graniteAuthor.pageInfo.editableTemplate ? graniteAuthor.pageInfo.editableTemplate : '',
            pageResType: graniteAuthor.pageInfo.pageResourceType,
            parentsResTypes: editable.getAllParents().map(parent => parent.type).slice(skipFirstParent ? 1 : 0).reverse(),
            pagePath: editable.path.substring(0, editable.path.indexOf('/jcr:content')),
            container: editable.path.substring(editable.path.lastIndexOf('/') + 1)
        };
    }

    /**
     * Gets whether the current rule matches the container judging by the container/page properties
     * @param rule - object denoting the current rule
     * @param properties - as retrieved from {@code getContainerProperties()}
     * @param componentList - list of all components available in the instance
     */
    function isRuleApplicable(rule, properties, componentList) {
        return arrayContains(rule.pageResourceTypes, properties.pageResType) &&
            arrayContains(rule.pagePaths, properties.pagePath) &&
            arrayContains(rule.containers, properties.container) &&
            checkTemplate(rule.templates, properties.template) &&
            checkParent(rule.parentsResourceTypes, properties.parentsResTypes, componentList);
    }

    /**
     * Gets whether the current template is contained within the array of templates.
     * Always returns {@code true} for pages based on static templates
     * @param templates - array of templates specified in the rule
     * @param value - resource type of the current template (empty for pages based on static templates)
     */
    function checkTemplate(templates, value) {
        if (!templates || !templates.length || !value) {
            return true;
        }
        return templates.some(template => isMatching(template, value));
    }

    /**
     * Gets whether the given value is present within the array
     * @param array - array of identifiers
     * @param value - current identifier
     */
    function arrayContains(array, value) {
        return !array || !array.length || array.some(element => isMatching(element, value));
    }

    /**
     * Gets whether the current parents is contained within the array of parents.
     * Always returns {@code true} for pages based on static templates
     * @param parentsResTypes
     * @param values
     * @param componentList
     */
    function checkParent(parentsResTypes, values, componentList) {
        return !parentsResTypes || !parentsResTypes.length || parentsResTypes.some(parents => parentMatch(parents, values, componentList));
    }

    /**
     * Gets whether the particular entry of the {@code parents} rule setting matches the property as retrieved from the
     * {@code getContainerProperties()} method
     * @param parentsEntry - entry representing single parent or a succession of parents for the current container
     * @param parentsResTypes - resource types of all parents of the current container (from the most remote parent to
     *     the closest one)
     * @param componentList - list of all components available in the instance
     */
    function parentMatch(parentsEntry, parentsResTypes, componentList) {
        const parentsSetting = getParentsAsArray(parentsEntry);
        for (const parent of parentsResTypes) {
            if (isMatchingGroupOrResourceType(parentsSetting[0], parent, componentList)) {
                parentsSetting.shift();
            }
            if (parentsSetting.length === 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Splits the particular entry of the {@code parents} rule setting into an array
     * @param value - raw string setting
     */
    function getParentsAsArray(value) {
        const chunks = value.split(/\s+/);
        const groupStartPositions = [];
        for (let pos = 0; pos < chunks.length; pos++) {
            if (/^group:['"`]/.test(chunks[pos])) {
                groupStartPositions.push(pos);
            }
        }

        if (!groupStartPositions.length) {
            return chunks;
        }

        for (const startPos of groupStartPositions) {
            const escapingChar = chunks[startPos].substring(6, 7);
            if (endsWithEscapingChar(chunks[startPos], escapingChar)) {
                continue;
            }
            for (let nextPos = startPos + 1; nextPos < chunks.length; nextPos++) {
                const isEnding = endsWithEscapingChar(chunks[nextPos], escapingChar);
                chunks[startPos] += ' ' + chunks[nextPos];
                chunks[nextPos] = '';
                if (isEnding) {
                    break;
                }
            }
        }
        return chunks.filter(chunk => chunk).map(chunk => chunk.replace(/(['"`]){2}/g, '$1'));
    }

    /**
     * Gets whether the given string ends with an escaping char
     * @param value - string value
     * @param char - escaping char
     */
    function endsWithEscapingChar(value, char) {
        return value &&
            value.lastIndexOf(char) === value.length - 1 &&
            (value.length < 2 || value[value.length - 2] !== char);
    }

    /**
     * Gets whether the given chunk of the {@code parents} rule entry represents a component group or component
     * resource type that matches the parent
     * @param parentsChunk - an element of the {@code parents} rule entry
     * @param parentResType - resource type of the given parent
     * @param componentList - list of all components available in the instance
     */
    function isMatchingGroupOrResourceType(parentsChunk, parentResType, componentList) {
        return parentsChunk.startsWith('group:') ?
            isMatchingGroup(parentsChunk, parentResType, componentList) :
            isMatching(parentsChunk, parentResType);
    }

    /**
     * Gets whether the given chunk of the {@code parents} rule entry represents a component group that matches the
     * parent
     * @param parentsChunk - an element of the {@code parents} rule entry
     * @param parentResType - resource type of the given parent
     * @param componentList - list of all components available in the instance
     */
    function isMatchingGroup(parentsChunk, parentResType, componentList) {
        let groupName = parentsChunk.substring(6);
        if (groupName.startsWith('\'') || groupName.startsWith('`')) {
            groupName = groupName.substring(1, groupName.length - 1);
        }
        const arr = getComponentsResTypesByGroup(groupName, componentList);
        return arr.includes(parentResType);
    }

    /**
     * Retrieves array of resource types of components with given group name
     * @param group - group name
     * @param componentList - list of all components available in the instance
     */
    function getComponentsResTypesByGroup(group, componentList) {
        return componentList.filter(comp => comp.componentConfig.group === group).map(comp => comp.componentConfig.resourceType);
    }

    /**
     * Gets whether the given rule setting matches the property retrieved from the {@code getContainerProperties()}
     * method
     * @param setting - rule setting
     * @param property - container property
     */
    function isMatching(setting, property) {
        if (setting.startsWith('*') && setting.endsWith('*')) {
            return property.includes(setting.substring(1, setting.length - 1));
        }
        if (setting.startsWith('*')) {
            return property.endsWith(setting.substring(1));
        }
        if (setting.endsWith('*')) {
            return property.startsWith(setting.substring(0, setting.length - 1));
        }
        return property === setting.replace(/^\/?(apps)?\//, '');
    }

    /**
     * Modifies the list of allowed components for the current container according to the mode of specified rule
     * @param rule - matched rule
     * @param allowed - array of allowed components; modified within the method by reference
     */
    function applyRule(rule, allowed) {
        switch (rule.mode) {
            case 'MERGE':
                if (rule.value) {
                    allowed.push(...rule.value);
                }
                break;
            case 'EXCLUDE':
                if (rule.value) {
                    allowed.remove(rule.value);
                }
                break;
            default:
                allowed.length = 0;
                if (rule.value) {
                    allowed.push(...rule.value);
                }
        }
    }
}(Granite));
