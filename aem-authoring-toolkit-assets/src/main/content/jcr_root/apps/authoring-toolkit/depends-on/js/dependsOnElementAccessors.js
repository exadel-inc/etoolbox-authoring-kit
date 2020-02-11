/**
 * @author Alexey Stsefanovich (ala'n), Bernatskaya Yana (YanaBr)
 * @version 2.2.2
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
        findTarget: ($el) => {
            if ($el.length > 1) {
                console.warn(`[DependsOn]: requested reference with multiple targets, the first target is used.`, $el);
            }
            return $el.first();
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
            $el.closest(FIELD_WRAPPER)
                .attr('hidden', state ? null : 'true')
                .attr('data-dependson-controllable', 'true');
            // Force update validity if the field is hidden
            if (!state) {
                ns.ElementAccessors.clearValidity($el);
            }
        },
        disabled: function ($el, state) {
            $el.attr('disabled', state ? 'true' : null);

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
        if (!accessorDescriptor)
            throw new Error('[DependsOn] Can not register ElementAccessor. No accessor descriptor specified');
        if (typeof accessorDescriptor.selector !== 'string')
            throw new Error('[DependsOn] Can not register ElementAccessor. Descriptor.selector should exist and be type of string');
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
         * Get preferable type to cast for $el
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
         * Set required state of the $el
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * */
        static setRequired($el, value) {
            ElementAccessors._findAccessorHandler($el, 'required')($el, value);
        }
        /**
         * Set required state of the $el
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * */
        static setVisibility($el, value) {
            ElementAccessors._findAccessorHandler($el, 'visibility')($el, value);
        }
        /**
         * Set disabled state of the $el
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * */
        static setDisabled($el, value) {
            ElementAccessors._findAccessorHandler($el, 'disabled')($el, value);
        }

        /**
         * Find "DependsOn controllable" target
         * @param {JQuery} $el - target element
         * @returns {JQuery}
         * */
        static findTarget($el) {
            return ElementAccessors._findAccessorHandler($el, 'findTarget')($el);
        }

        /**
         * Register accessor.
         * Accessor descriptor should contain selector property - css selector to determine target element types.
         * @param {object} accessorDescriptor
         * */
        static registerAccessor(accessorDescriptor) {
            validate(accessorDescriptor);
            accessorsList.push(accessorDescriptor);
        }

        /**
         * Update validity
         * @param {JQuery} $el - target element
         * @param {boolean} [lazy] - true to skip initial validation
         * */
        static updateValidity($el, lazy) {
            const api = $el.adaptTo('foundation-validation');
            if (api && (!lazy || api.isValidated)) {
                api.checkValidity();
                api.updateUI();
            }
        }
        /**
         * Clear validity
         * Exclude all child submittables from validation cache.
         * @param {JQuery} $el - target element
         * */
        static clearValidity($el) {
            $el.find(SUBMITTABLES).addBack(SUBMITTABLES).trigger('foundation-validation-valid');
        }

        /**
         * Set label asterisk according to state
         * @param {JQuery} $el - target element
         * @param {boolean} required - required state
         * */
        static setLabelRequired($el, required) {
            const $label = $el.closest(FIELD_WRAPPER).find(FIELD_LABEL);
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
    ElementAccessors.noop = function() {};

    ns.ElementAccessors = ElementAccessors;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
