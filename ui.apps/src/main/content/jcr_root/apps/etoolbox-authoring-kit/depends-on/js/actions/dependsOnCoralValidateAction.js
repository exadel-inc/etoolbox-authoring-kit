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
 * DependsOn Coral 3 Validate Action.
 *
 * An additional action that sets the query result as the validation state.
 *
 * If the query result is a string then
 *     a blank string - indicates the valid state;
 *     non-blank string - indicates the invalid state and is used as the validation message.
 *
 * If the query result is a boolean value then
 *     true - indicates the valid state;
 *     false - indicates the invalid state (data-dependson-validate-msg attribute is used as the validation message).
 * Otherwise the result is cast to a boolean.
 *
 * Options:
 * data-dependson-validate-msg - the invalid state message if query result is boolean;
 * data-dependson-validate-cls - the invalid state class, default is 'dependson-validate-invalid';
 * data-dependson-validate-strict - force to set up the validity state after the initial update.
 *
 * NOTE: common data-dependson-validate marker just indicates that the field will be processed
 * by the DependsOn validator
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
        const instances = $el.data(ns.QueryObserver.DATA_STORE);
        const validateInstances = (instances || []).filter((observer) => observer.action === ACTION_NAME);

        let resultMsg;
        for (const validate of validateInstances) {
            const res = validate.data._validationResult;
            const invalidCls = validate.data.cls || DEFAULT_INVALID_CLASS;
            invalidCls && $el.toggleClass(invalidCls, !!res);
            resultMsg = resultMsg || res;
        }
        return resultMsg;
    }

    let dependsOnValidatorRegistered = false;
    // Register validator in registry only if the action is used
    function register() {
        const foundationRegistry = $(window).adaptTo('foundation-registry');
        // Make target 'validatable'
        foundationRegistry.register('foundation.validation.selector', {
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
    ns.ActionRegistry.register(ACTION_NAME, function (result, payload) {
        if (!dependsOnValidatorRegistered) register();

        if (typeof result === 'string') {
            payload._validationResult = result;
        } else {
            payload._validationResult = result ? '' : (payload.msg || DEFAULT_MSG);
        }

        if (this.$el.is(TARGET_SEL)) {
            ns.ElementAccessors.updateValidity(this.$el, !payload.strict);
        } else {
            this.$el.attr(TARGET_ATTR, ''); // Mark element for validator
            ns.ElementAccessors.updateValidity(this.$el, true);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
