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
    const SUBMITTABLES = ':-foundation-submittable';

    const DEFAULT_ACCESSOR = {};
    const accessorsList = [];

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
        static get DEFAULT_ACCESSOR() {
            return Object.assign({}, DEFAULT_ACCESSOR);
        }

        /** Finds proper accessor by selector */
        static findAccessor($el, type) {
            for (let i = accessorsList.length - 1, accessor; i >= 0; --i) {
                accessor = accessorsList[i];
                if ($el.is(accessor.selector) && typeof accessor[type] !== 'undefined') {
                    return accessor;
                }
            }
            return DEFAULT_ACCESSOR;
        }

        /** Returns bind proper accessor or property by type */
        static getAccessor($el, type) {
            const accessor = this.findAccessor($el, type);
            return (typeof accessor[type] === 'function') ? accessor[type].bind(accessor) : accessor[type];
        }

        /** Returns manged state accessor wrapper */
        static getManagedAccessor($el, type, inverted) {
            const accessor = this.getAccessor($el, type);
            return ns.ManagedStateHelper.wrap(accessor, type, inverted);
        }

        /**
         * Registers an accessor.
         * Accessor descriptor should contain selector property - css selector to determine target element types.
         * @param {object} accessorDescriptor
         * */
        static registerAccessor(accessorDescriptor) {
            validate(accessorDescriptor);
            if (accessorDescriptor.selector === '*') {
                // Default accessors are merged
                Object.assign(DEFAULT_ACCESSOR, accessorDescriptor);
            } else {
                // Custom accessors are added to the list
                accessorsList.push(accessorDescriptor);
            }
        }

        /**
         * Get $el value
         * @param {JQuery} $el - target element
         * */
        static getValue($el) {
            return ElementAccessors.getAccessor($el, 'get')($el);
        }

        /**
         * Get the preferable type for $el to be cast to
         * @param {JQuery} $el - target element
         * @returns {string}
         * */
        static getPreferableType($el) {
            return ElementAccessors.getAccessor($el, 'preferableType');
        }

        /**
         * Set $el value
         * @param {JQuery} $el - target element
         * @param {*} value - value to set
         * @param {boolean} [notify] - produce change event
         * */
        static setValue($el, value, notify = true) {
            return ElementAccessors.getAccessor($el, 'set')($el, value, notify);
        }

        /** Sets form field placeholder */
        static setPlaceholder($el, value) {
            return ElementAccessors.getAccessor($el, 'placeholder')($el, value);
        }

        /**
         * Sets visibility of $el.
         * Can act as managed state handler, so is will respect the actor that requested the change.
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * @param {object} [actor] - component that requests change
         * */
        static setVisibility($el, value, actor) {
            return this.getManagedAccessor($el, 'visibility', true)($el, value, actor);
        }

        /**
         * Sets the required state of $el
         * Can act as managed state handler, so will respect the actor that requested the change.
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * @param {object} [actor] - component that requests change
         * */
        static setRequired($el, value, actor) {
            return this.getManagedAccessor($el, 'required')($el, value, actor);
        }

        /**
         * Sets the readonly state of $el.
         * Can act as managed state handler, so is will respect the actor that requested the change.
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * @param {object} [actor] - component that requests change
         * */
        static setReadonly($el, value, actor) {
            return this.getManagedAccessor($el, 'readonly')($el, value, actor);
        }

        /**
         * Set the disabled state of $el.
         * Can act as managed state handler, so is will respect the actor that requested the change.
         * @param {JQuery} $el - target element
         * @param {boolean} value - state to set
         * @param {object} [actor] - component that requests change
         * */
        static setDisabled($el, value, actor) {
            return this.getManagedAccessor($el, 'disabled')($el, value, actor);
        }

        /**
         * Find target element to be used as accessors root
         * @param {JQuery} $el - target element
         * @returns {JQuery}
         * */
        static findTarget($el) {
            return ElementAccessors.getAccessor($el, 'findTarget')($el);
        }

        /**
         * Find element wrapper
         * @param {JQuery} $el - target element
         * @returns {JQuery}
         * */
        static findWrapper($el) {
            return ElementAccessors.getAccessor($el, 'findWrapper')($el);
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

        // Legacy aliases
        /** @deprecated alias for setDisabled */
        static get requestDisable() { return ElementAccessors.setDisabled; }
    }

    ns.ElementAccessors = ElementAccessors;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
