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

    // Find and init element references
    ns.initRefs = function () {
        $('[data-dependsonref]').each(function () {
            Coral.commons.ready($(this), ($el) => ns.ReferenceRegistry.registerElement($el));
        });
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
        setTimeout(() => ns.ReferenceRegistry.cleanDetachedRefs());
    };
    $(document).on('foundation-contentloaded', ns.initialize);
    $(document)
        .off('change.dependsOn').on('change.dependsOn', '[data-dependsonref]', ns.ElementReference.handleChange)
        .off('selected.dependsOn').on('selected.dependsOn', '[data-dependsonref]', ns.ElementReference.handleChange);

    // ----
    // Validation control: exclude element and its child from validation in hidden state.
    $(window).adaptTo('foundation-registry').register('foundation.validation.selector', {
        exclusion: '[data-dependson][hidden], [data-dependson-controllable][hidden]'
    });
    $(window).adaptTo('foundation-registry').register('foundation.validation.selector', {
        exclusion: '[data-dependson][hidden] *, [data-dependson-controllable][hidden] *'
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));