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
    ns.initRefs = function (container) {
        $('[data-dependsonref]', container).each(function () {
            ns.ElementReferenceRegistry.registerElement($(this));
        });
        ns.GroupReferenceRegistry.updateGroupReferences();
    };
    // Find and init plugin observers
    ns.initObservers = function (container) {
        $('[data-dependson]', container).each(function () {
            Coral.commons.ready($(this), ($el) => ns.DependsOnObserver.init($el));
        });
    };

    ns.initialize = function (e) {
        const container = e && e.target || document;

        ns.initRefs(container);
        ns.initObservers(container);

        // Initiate DependsOn GC if reinitialization requested
        setTimeout(() => ns.ElementReferenceRegistry.cleanDetachedRefs());
    };

    // Track new component initialization
    $document.off('foundation-contentloaded.dependsOn').on('foundation-contentloaded.dependsOn', ns.initialize);
    // Track reference field changes
    $document
        .off('change.dependsOn').on('change.dependsOn', '[data-dependsonref]', ns.ElementReferenceRegistry.handleChange)
        .off('selected.dependsOn').on('selected.dependsOn', '[data-dependsonref]', ns.ElementReferenceRegistry.handleChange);
    // Track collection change to update dynamic references
    $document
        .off('coral-collection:remove.dependsOn').on('coral-collection:remove.dependsOn', 'coral-multifield', () => {
            ns.ElementReferenceRegistry.cleanDetachedRefs();
            ns.GroupReferenceRegistry.updateGroupReferences();
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