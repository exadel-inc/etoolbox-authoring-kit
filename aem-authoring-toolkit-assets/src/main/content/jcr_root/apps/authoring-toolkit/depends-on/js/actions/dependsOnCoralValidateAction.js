/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.1.0
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

    const ACTION_NAME = 'validate';

    const DEFAULT_MSG = 'Incorrect data';
    const DEFAULT_INVALID_CLASS = 'dependson-validate-invalid';

    const TARGET_ATTR = 'data-dependson-validate';
    const TARGET_SEL = '[' + TARGET_ATTR + ']';

    // Just return dependsOn validate result and set marker class accordingly
    function checkDependsOnValidator(el) {
        const $el = $(el);
        const instances = $el.data(ns.DependsOnObserver.DATA_STORE);
        const validateInstances = instances.filter((observer) => observer.action === ACTION_NAME);

        let resultMsg = undefined;
        for (let validate of validateInstances) {
            const res = validate.data._validationResult;
            const invalidCls = validate.data.cls || DEFAULT_INVALID_CLASS;
            $el.toggleClass(invalidCls, !!res);
            resultMsg = resultMsg || res;
        }
        return resultMsg;
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
    ns.ActionRegistry.register(ACTION_NAME,function (result, payload) {
        if (!dependsOnValidatorRegistered) register();

        this.$el.attr(TARGET_ATTR, ''); // Mark element for validator

        if (typeof result === 'string') {
            payload._validationResult = result;
        } else {
            payload._validationResult = result ? '' : (payload.msg || DEFAULT_MSG);
        }

        ns.ElementAccessors.updateValidity(this.$el, true); // force validation
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
