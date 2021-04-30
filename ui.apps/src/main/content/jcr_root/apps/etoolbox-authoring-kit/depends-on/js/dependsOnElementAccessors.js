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
 * @author Alexey Stsefanovich (ala'n), Bernatskaya Yana (YanaBr)
 *
 * DependsOn ElementAccessors Registry
 * */
(function ($, ns) {
    'use strict';

    const FIELD_LABEL = '.coral-Form-fieldlabel';
    const FIELD_WRAPPER = '.coral-Form-fieldwrapper';
    const SUBMITTABLES = ':-foundation-submittable';

    const accessorsList = [];
    const DEFAULT_ACCESSOR = {
        preferableType: 'string',
        findTarget: function ($el) {
            if ($el.length > 1) {
                console.warn('[DependsOn]: requested a reference with multiple targets, the first target is used.', $el);
            }
            return $el.first();
        },
        findWrapper: function ($el) {
            return $el.closest(FIELD_WRAPPER);
        },
        get: function ($el) {
            return $el.val() || '';
        },
        set: function ($el, value, notify) {
            if (ns.isObject(value)) {
                value = JSON.stringify(value);
            }
            $el.val(value);
            notify && $el.trigger('change');
        },
        required: function ($el, val) {
            const fieldApi = $el.adaptTo('foundation-field');
            if (fieldApi && typeof fieldApi.setRequired === 'function') {
                fieldApi.setRequired(val);
            } else {
                $el.attr('required', val ? 'true' : null);
            }
            ns.ElementAccessors.updateValidity($el, true);
        },
        visibility: function ($el, state) {
            $el.attr('hidden', state ? null : 'true');
            ns.ElementAccessors.findWrapper($el)
                .attr('hidden', state ? null : 'true')
                .attr('data-dependson-controllable', 'true');
            // Force update validity if the field is hidden
            if (!state) {
                ns.ElementAccessors.updateValidity($el);
            }
            ns.ElementAccessors.clearValidity($el);
            ns.ElementAccessors.updateSubmittables($el.parent());
        },
        disabled: function ($el, state) {
            $el.attr('disabled', state ? 'true' : null);
            ns.ElementAccessors.findWrapper($el).attr('disabled', state ? 'true' : null);

            const fieldAPI = $el.adaptTo('foundation-field');
            // Try to disable field by foundation api
            if (fieldAPI && fieldAPI.setDisabled) {
                fieldAPI.setDisabled(state);
            }
            // Force update validity if field disabled
            if (state) {
                ns.ElementAccessors.updateValidity($el);
            }
            ns.ElementAccessors.clearValidity($el);
        }
    };

    function validate(accessorDescriptor) {
        if (!accessorDescriptor) {
            throw new Error('[DependsOn] Cannot register ElementAccessor. No accessor descriptor was specified');
        }
        if (typeof accessorDescriptor.selector !== 'string') {
            throw new Error('[DependsOn] Cannot register ElementAccessor. Descriptor.selector should exist and be type of string');
        }
    }

    class ElementAccessors {
        /**
         * Default accessor definition
         * @readonly
         * */
        static get DEFAULT_ACCESSOR() { return DEFAULT_ACCESSOR; }

        /**
         * Get $el value
         * @param {JQuery} $el - target element
         * */
        static getValue($el) {
            return ElementAccessors._findAccessorHandler($el, 'get')($el);
        }

        /**
         * Get the preferable type for $el to be cast to
         * @param {JQuery} $el - target element
         * @returns {string}
         * */
        static getPreferableType($el) {
            return ElementAccessors._findAccessorHandler($el, 'preferableType');
        }

        /**
         * Set $el value
         * @param {JQuery} $el - target element
         * @param {*} value - value to set
         * @param {boolean} [notify] - produce change event
         * */
        static setValue($el, value, notify = true) {
            ElementAccessors._findAccessorHandler($el, 'set')($el, value, notify);
        }

        /**
         * Set the required state of $el
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * */
        static setRequired($el, value) {
            ElementAccessors._findAccessorHandler($el, 'required')($el, value);
        }

        /**
         * Set visibility of $el
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * */
        static setVisibility($el, value) {
            ElementAccessors._findAccessorHandler($el, 'visibility')($el, value);
        }

        /**
         * Set the disabled state of $el
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * */
        static setDisabled($el, value) {
            ElementAccessors._findAccessorHandler($el, 'disabled')($el, value);
        }

        /**
         * Find target element to be used as accessors root
         * @param {JQuery} $el - target element
         * @returns {JQuery}
         * */
        static findTarget($el) {
            return ElementAccessors._findAccessorHandler($el, 'findTarget')($el);
        }

        /**
         * Find element wrapper
         * @param {JQuery} $el - target element
         * @returns {JQuery}
         * */
        static findWrapper($el) {
            return ElementAccessors._findAccessorHandler($el, 'findWrapper')($el);
        }

        /**
         * Register an accessor.
         * Accessor descriptor should contain selector property - css selector to determine target element types.
         * @param {object} accessorDescriptor
         * */
        static registerAccessor(accessorDescriptor) {
            validate(accessorDescriptor);
            accessorsList.push(accessorDescriptor);
        }

        /**
         * Triggers validating for the elements that support "foundation-validation"
         * @param {JQuery} $el - target element
         * @param {boolean} [lazy] - true to skip validation at the init stage
         * */
        static updateValidity($el, lazy) {
            const api = $el.adaptTo('foundation-validation');
            if (api && (!lazy || api.isValidated)) {
                api.checkValidity();
                api.updateUI();
            }
        }

        /**
         * Update child submittables
         * @param {JQuery} $el - target element
         * */
        static updateSubmittables($el) {
            $el.adaptTo('foundation-validation-helper').getSubmittables();
        }

        /**
         * Clear validity
         * Exclude all child submittables from validation cache
         * @param {JQuery} $el - target element
         * */
        static clearValidity($el) {
            $el.find(SUBMITTABLES).addBack(SUBMITTABLES).trigger('foundation-validation-valid');
        }

        /**
         * Add the "required" symbol (the asterisk) to an element's label as required by its state
         * @param {JQuery} $el - target element
         * @param {boolean} required - required state
         * */
        static setLabelRequired($el, required) {
            const $label = ns.ElementAccessors.findWrapper($el).find(FIELD_LABEL);
            ns.toggleAsterisk($label, required);
        }

        static _findAccessor($el, type) {
            for (let i = accessorsList.length - 1, accessor; i >= 0; --i) {
                accessor = accessorsList[i];
                if (accessor.selector && $el.is(accessor.selector) && typeof accessor[type] !== 'undefined') {
                    return accessor;
                }
            }
            return ElementAccessors.DEFAULT_ACCESSOR;
        }

        static _findAccessorHandler($el, type) {
            const accessor = ElementAccessors._findAccessor($el, type);
            return (typeof accessor[type] === 'function') ? accessor[type].bind(accessor) : accessor[type];
        }
    }
    ElementAccessors.noop = function () {};

    ns.ElementAccessors = ElementAccessors;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
