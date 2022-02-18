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

(function (ns) {
    'use strict';

    ns.policyResolver = ns.policyResolver || {};

    ns.policyResolver.build = function (rules = testJson) {
        return function (cell, allowed, componentList) {
            resolve(componentList, this, allowed, rules);
        };
    };

    function resolve(componentList, editable, allowed, configJson = testJson) {
        console.log('we are here');

        const config = JSON.parse(configJson);
        const settings = getCurrentSettings(editable, ns.author, !config.isEditConfig);
        const applicableRule = config.rules.find(rule => isRuleApplied(rule, settings, componentList));

        if (applicableRule) {
            allowed.length = 0;
            allowed.push(...applicableRule.value);
        }
    }

    function getCurrentSettings(editable, author, skipFirstParent) {
        return {
            template: author.pageInfo.editableTemplate ? author.pageInfo.editableTemplate : '',
            pageResType: author.pageInfo.pageResourceType,
            parentsResTypes: editable.getAllParents().map(parent => parent.type).slice(skipFirstParent ? 1 : 0).reverse(),
            pagePath: editable.path.substring(0, editable.path.indexOf('/jcr:content')),
            container: editable.path.substring(editable.path.lastIndexOf('/') + 1)
        };
    }

    function isRuleApplied(rule, settings, componentList) {
        return checkTemplate(rule.templates ? rule.templates : [], settings.template) &&
            checkPageResType(rule.pageResourceTypes ? rule.pageResourceTypes : [], settings.pageResType) &&
            checkParent(rule.parentsResourceTypes ? rule.parentsResourceTypes : [], settings.parentsResTypes, componentList) &&
            checkPath(rule.pagePaths ? rule.pagePaths : [], settings.pagePath) &&
            checkContainer(rule.containers ? rule.containers : [], settings.container);
    }

    function checkTemplate(templates, curTemplate) {
        if (!templates.length || !curTemplate) {
            return true;
        }
        return templates.some(template => match(template, curTemplate));
    }

    function checkPageResType(resTypes, curResType) {
        return commonCheck(resTypes, curResType);
    }

    function checkParent(parentsResTypes, curParents, componentList) {
        return !parentsResTypes.length || parentsResTypes.some(parents => parentMatch(parents, curParents, componentList));
    }

    function checkPath(pagePaths, curPagePath) {
        return commonCheck(pagePaths, curPagePath);
    }

    function checkContainer(containers, curContainer) {
        return commonCheck(containers, curContainer);
    }

    function commonCheck(array, curValue) {
        return !array.length || array.some(element => match(element, curValue));
    }

    function parentMatch(parents, curParents, componentList) {
        const tmp = parents.split(/\s+/);
        if (tmp.length === 1) {
            return resolveGroup(tmp[0], curParents.slice(-1)[0], componentList);
        }
        for (const parent of curParents) {
            if (resolveGroup(tmp[0], parent, componentList)) {
                tmp.shift();
            }
            if (tmp.length === 0) {
                return true;
            }
        }
        return false;
    }

    function resolveGroup(parameter, setting, componentList) {
        if (parameter.startsWith('group:')) {
            const arr = getComponentsResTypesByGroup(parameter.substring(6), componentList);
            return arr.includes(setting);
        }
        return match(parameter, setting);
    }

    function match(parameter, setting) {
        if (parameter.startsWith('*') && parameter.endsWith('*')) {
            return setting.includes(parameter.substring(1, parameter.length - 1));
        }
        if (parameter.startsWith('*')) {
            return setting.endsWith(parameter.substring(1));
        }
        if (parameter.endsWith('*')) {
            return setting.startsWith(parameter.substring(0, parameter.length - 1));
        }
        return setting === parameter;
    }

    function getComponentsResTypesByGroup(group, componentList) {
        return componentList.filter(comp => comp.componentConfig.group === group).map(comp => comp.componentConfig.resourceType);
    }
}(Granite));
