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
 * @author Alexey Stsefanovich (ala'n)
 *
 * DependsOn plugin debugging utilities.
 * */
(function ($, ns) {
    'use strict';

    const ELEMENT_REF_REGEX = /\$ref([0-9]+)/g;
    const GROUP_REF_REGEX = /\$group([0-9]+)/g;
    function reportReferences(query, prefix = '') {
        const refs = new Set();
        (query.match(ELEMENT_REF_REGEX) || []).forEach((match) => refs.add(match));
        (query.match(GROUP_REF_REGEX) || []).forEach((match) => refs.add(match));

        const elements = ns.ElementReferenceRegistry.refs.filter((ref) => refs.delete(ref.id));
        const groups = ns.GroupReferenceRegistry.refs.filter((ref) => refs.delete(ref.id));
        const missing = [...refs];

        return { elements, groups, missing };
    }

    function logElementDetails($el) {
        const observers = ns.QueryObserver.get($el);
        console.group('Element Details');
        console.log('Element: ', $el.length > 1 ? $el : $el[0]);
        console.group('Observers: %d found', observers.length);
        for (const observer of observers) {
            const { elements, groups, missing } = reportReferences(observer.parsedQuery);
            console.groupCollapsed('%s = %s', observer.action, observer.parsedQuery);
            console.log('\tInstance: ', observer);
            console.log('\tOriginal query: ', observer.query);
            console.log('\tReferences: ', elements.length > 0 ? elements : 'None');
            console.log('\tGroup references: ', groups.length > 0 ? groups : 'None');
            if (missing.length > 0) {
                console.warn(`\tMissing references: ${missing.join(', ')}`);
            }
            console.groupEnd();
        }
        console.groupEnd();

        const references = ns.ElementReferenceRegistry.refs
            .filter((ref) => ref.$el.is($el));
        console.groupCollapsed('References: %d found', references.length);
        console.log(references.length > 0 ? references : 'None');
        console.groupEnd();

        console.groupEnd();
    }

    /**
     * Log debug information to console.
     *
     * @param {HTMLElement | JQuery} el - the element to debug, if not provided, logs all registered references and actions.
     * @param {boolean} [deep=false] - if true, logs all elements with dependson observer within the provided element.
     */
    ns.debug = function (el, deep = false) {
        console.log(`--- DependsOn v${ns.version} ---`);
        if (el) {
            $(el).filter('[data-dependson]')
                .add(deep ? $(el).find('[data-dependson]') : [])
                .each((index, element) => {
                    logElementDetails($(element));
                });
        } else {
            console.log('Group References:');
            ns.GroupReferenceRegistry.refs.forEach((ref) => console.log(ref));
            console.log('Element References:');
            ns.ElementReferenceRegistry.refs.forEach((ref) => console.log(ref));
            console.log('Registered actions: ', ns.ActionRegistry.registeredActionNames);
        }
        console.log('--- DEBUG INFORMATION END ---');
    };
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
