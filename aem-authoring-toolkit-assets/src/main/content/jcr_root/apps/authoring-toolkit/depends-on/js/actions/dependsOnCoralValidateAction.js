/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 1.0.0
 *
 * DependsOn Coral 3 Validate Actions
 * Additional action which sets query result as validation state
 *
 * If the query result is string then
 *     blank string - indicates valid state
 *     not blank string - indicates invalid state and used as validation massage
 * If the query result is boolean then
 *     true - indicates valid state
 *     false - indicates invalid state (data-dependson-validate-msg attribute is used as validation message)
 * Otherwise the result is cast to boolean.
 *
 * NOTE: common data-dependson-validate value indicates invalid state & message
 * */
(function ($, ns) {
    'use strict';

    const INVALID_CLASS = 'dependsOnValidate-invalid';
    const DATA_MSG_ATTR = 'data-dependson-validate-msg';
    const DATA_MARKER_ATTR = 'data-dependson-validate';
    const TARGET_SEL = '[' + DATA_MARKER_ATTR + ']';


    // Just return dependsOn validate result and set marker class accordingly
    function checkDependsOnValidator(el) {
        const marker = el.getAttribute(DATA_MARKER_ATTR) || '';
        el.classList.remove(INVALID_CLASS);
        if (marker.length) {
            el.classList.add(INVALID_CLASS);
            return marker;
        }
    }

    let dependsOnValidatorRegistered = false;
    // Register validator in registry only if acton is used
    function register() {
        const foundationRegistry = $(window).adaptTo("foundation-registry");
        // Make target 'validateable'
        foundationRegistry.register("foundation.validation.selector", {
            submittable: TARGET_SEL,
            candidate: TARGET_SEL + ':not([disabled])',
            exclusion: '[data-dependson][hidden] *'
        });
        // Register validator
        foundationRegistry.register('foundation.validation.validator', {
            selector: TARGET_SEL,
            validate: checkDependsOnValidator
        });
        dependsOnValidatorRegistered = true;
    }

    // Action itself
    // Provide validator result
    ns.ActionRegistry.register('validate',function (result) {
        if (!dependsOnValidatorRegistered) {
            register();
        }
        if (typeof result === 'string') {
            this.$el.attr(DATA_MARKER_ATTR, result);
        } else {
            this.$el.attr(DATA_MARKER_ATTR, result ? '' : this.$el.attr(DATA_MSG_ATTR));
        }
        ns.ElementAccessors.updateValidity(this.$el, true); // force validation
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
