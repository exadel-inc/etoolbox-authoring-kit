/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 1.3.0
 *
 * DependsOn ElementAccessors Registry
 * */
(function ($, ns) {
    'use strict';

    const FIELD_LABEL = '.coral-Form-fieldlabel';
    const FIELD_WRAPPER = '.coral-Form-fieldwrapper';

    const accessorsList = [];
    const DEFAULT_ACCESSOR = {
        preferableType: 'string',
        get: function ($el) {
            return $el.val() || '';
        },
        set: function ($el, value) {
            $el.val(value);
        },
        required: function ($el, val) {
            $el.attr('aria-required', val ? 'true' : null);
            ns.ElementAccessors.updateValidity($el, true);
        },
        visibility: function ($el, state) {
            $el.attr('hidden', state ? null : 'true');
            $el.closest(FIELD_WRAPPER)
                .attr('hidden', state ? null : 'true')
                .attr('data-dependson-controllable', 'true');
            if (!state) {
                ns.ElementAccessors.clearValidity($el);
            }
        },
        disabled: function ($el, state) {
            const fieldAPI = $el.adaptTo('foundation-field');

            $el.attr('disabled', state ? 'true' : null);
            $el.closest(FIELD_WRAPPER).attr('disabled', state ? 'true' : null);
            if (!state) {
                ns.ElementAccessors.clearValidity($el);
            }

            //disable field's inner input
            if(fieldAPI && fieldAPI.setDisabled) {
                fieldAPI.setDisabled(state);
            }
        }
    };

    function validate(accessorDescriptor) {
        if (!accessorDescriptor)
            throw new Error('[DependsOn] Can not register ElementAccessor. No accessor descriptor specified');
        if (typeof accessorDescriptor.selector !== 'string')
            throw new Error('[DependsOn] Can not register ElementAccessor. Descriptor.selector should be type of string');
    }

    class ElementAccessors {
        /**
         * Default accessor definition
         * */
        static get DEFAULT_ACCESSOR() { return DEFAULT_ACCESSOR; }

        static getValue($el) {
            return ElementAccessors._findAccessorHandler($el, 'get')($el);
        }
        static getPreferableType($el) {
            return ElementAccessors._findAccessorHandler($el, 'preferableType');
        }
        static setValue($el, value) {
            ElementAccessors._findAccessorHandler($el, 'set')($el, value);
        }
        static setRequired($el, value) {
            ElementAccessors._findAccessorHandler($el, 'required')($el, value);
        }
        static setVisibility($el, value) {
            ElementAccessors._findAccessorHandler($el, 'visibility')($el, value);
        }
        static setDisabled($el, value) {
            ElementAccessors._findAccessorHandler($el, 'disabled')($el, value);
        }

        static registerAccessor(accessorDescriptor) {
            validate(accessorDescriptor);
            accessorsList.push(accessorDescriptor);
        }

        /**
         * Update validity
         * @param $el {JQueryElement}
         * @param [lazy] {boolean} - true to skip initial validation
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
         * @param $el {JQueryElement}
         * */
        static clearValidity($el) {
            $el.find(':-foundation-submittable').trigger('foundation-validation-valid');
        }

        /**
         * Set label asterisk according to state
         * @param $el {JQueryElement}
         * @param required {boolean}
         * */
        static setLabelRequired($el, required) {
            const $label = $el.closest(FIELD_WRAPPER).find(FIELD_LABEL);
            $label.text($label.text().replace(/\s?\*?$/, required ? ' *': ''));
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
