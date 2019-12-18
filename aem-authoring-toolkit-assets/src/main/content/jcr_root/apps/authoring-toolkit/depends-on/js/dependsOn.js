/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn plugin entry point
 *
 * Initialize DependsOnObserver and DependsOnElementReferences
 * */
(function ($, ns) {
    'use strict';

    const $window = $(window);
    const $document = $(document);

    // Find and init element references
    ns.initRefs = function () {
        $('[data-dependsonref]').each(function () {
            ns.ElementReferenceRegistry.registerElement($(this));
        });
        ns.GroupReferenceRegistry.updateGroupReferences();
    };
    // Find and init plugin observers
    ns.initObservers = function () {
        $('[data-dependson]').each(function () {
            Coral.commons.ready($(this), ($el) => ns.DependsOnObserver.init($el));
        });
    };

    ns.initialize = function () {
        ns.initRefs();
        ns.initObservers();

        // Initiate DependsOn GC if reinitialization requested
        setTimeout(() => ns.ElementReferenceRegistry.cleanDetachedRefs());
    };
    $document.on('foundation-contentloaded', ns.initialize);
    $document
        .off('change.dependsOn').on('change.dependsOn', '[data-dependsonref]', ns.ElementReferenceRegistry.handleChange)
        .off('selected.dependsOn').on('selected.dependsOn', '[data-dependsonref]', ns.ElementReferenceRegistry.handleChange);

    // ----
    // Validation control: exclude element and its child from validation in hidden state.
    $window.adaptTo('foundation-registry').register('foundation.validation.selector', {
        exclusion: '[data-dependson][hidden], [data-dependson-controllable][hidden]'
    });
    $window.adaptTo('foundation-registry').register('foundation.validation.selector', {
        exclusion: '[data-dependson][hidden] *, [data-dependson-controllable][hidden] *'
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));