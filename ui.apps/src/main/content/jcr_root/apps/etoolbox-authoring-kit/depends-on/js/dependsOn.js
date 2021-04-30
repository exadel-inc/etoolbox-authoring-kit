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
 * DependsOn plugin entry point.
 *
 * Initializes QueryObserver and ElementReferences
 * */
(function ($, ns) {
    'use strict';

    const $window = $(window);
    const $document = $(document);

    // Version to track actual DependsOn version from code/browser console
    ns.version = '2.5.0';

    /**
     * DependsOn entry point
     * Initialize DependsOn for container
     * @param {HTMLElement} container
     * @param {function} [callback]
     * */
    ns.initialize = function (container, callback) {
        // Wait core-components:ready for new content and then plan initialization task
        Coral.commons.ready(container, () => setTimeout(() => {
            // GC and update simple references first
            ns.ElementReferenceRegistry.actualize();
            $('[data-dependsonref]', container).each((i, el) => ns.ElementReferenceRegistry.registerElement($(el)));
            // GC for group references
            ns.GroupReferenceRegistry.actualize();
            // Observers initialization
            $('[data-dependson]', container).each((i, el) => ns.QueryObserver.init($(el)));
            // Execute callback if provided
            (typeof callback === 'function') && callback();
        }));
    };

    // Track new component initialization
    $document.off('foundation-contentloaded.dependsOn')
        .on('foundation-contentloaded.dependsOn', (e) => ns.initialize((e && e.target) || document));

    // Track reference field changes
    const handleChange = ns.ElementReferenceRegistry.handleChange;
    $document
        .off('change.dependsOn').on('change.dependsOn', '[data-dependsonref]', handleChange)
        .off('selected.dependsOn').on('selected.dependsOn', '[data-dependsonref]', handleChange);

    // Track input event
    const handleChangeDebounced = $.debounce(750, handleChange);
    $document
        .off('input.dependsOn').on('input', '[data-dependsonref]:not([data-dependsonreflazy])', handleChangeDebounced);

    // Track collection change to update dynamic references
    $document
        .off('coral-collection:remove.dependsOn coral-collection:add.dependsOn').on('coral-collection:remove.dependsOn coral-collection:add.dependsOn', 'coral-multifield', (e) => {
            // We should actualize references on coral-collection:remove and coral-collection:add too.
            ns.ElementReferenceRegistry.handleChange(e);
            ns.ElementReferenceRegistry.actualize();
            ns.GroupReferenceRegistry.actualize();
        });

    // ----
    // Validation control: exclude element and its child from validation in hidden state.
    $window.adaptTo('foundation-registry').register('foundation.validation.selector', {
        exclusion: '[data-dependson][hidden], [data-dependson-controllable][hidden]'
    });
    $window.adaptTo('foundation-registry').register('foundation.validation.selector', {
        exclusion: '[data-dependson][hidden] *, [data-dependson-controllable][hidden] *'
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
