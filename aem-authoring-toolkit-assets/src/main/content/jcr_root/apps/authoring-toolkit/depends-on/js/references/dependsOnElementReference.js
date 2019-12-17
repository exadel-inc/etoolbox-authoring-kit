/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn plugin Element Observable Reference
 * @extends ObservedReference
 *
 * Define simple element reverence. Observe form input value change.
 * */
(function (document, $, ns) {
    'use strict';

    let referenceIdCounter = 0;

    class ElementReference extends ns.ObservedReference {
        /**
         * Cast field value to the type
         * */
        static castToType(val, type) {
            switch (type.toLowerCase()) {
                case 'boolean':
                    return Boolean(val);
                case 'boolstring':
                    return String(val) === 'true';
                case 'number':
                    return Number(val);
                case 'string':
                    return String(val);
                default:
                    return val;
            }
        }

        /**
         * Initialize element observable adapter
         * Only one instance per element can be initialized
         * Returns existing adapter if instance already created
         * */
        constructor($el) {
            let instance = $el.data('dependsonsubject');
            if (instance) return instance;

            super(`$${referenceIdCounter++}`);
            this.$el = $el;
            this.name = $el.data('dependsonref');

            this.$el.data('dependsonsubject', this);
        }

        /**
         * Get current element value
         * */
        getValue() {
            let type = (this.$el.attr('data-dependsonreftype') || '').trim();
            if (!type) {
                type = ns.ElementAccessors.getPreferableType(this.$el);
            }
            return ElementReference.castToType(ns.ElementAccessors.getValue(this.$el), type);
        };

        /**
         * Check is the Element Reference accept passed referenced definition
         * @param name {string}
         * @param [$context] {jQuery | HTMLElement | string}
         * @returns {boolean}
         * */
        is(name, $context) {
            if (this.name !== name) return false;
            if ($context) {
                return !!this.$el.closest($context).length;
            }
            return true;
        }
    }
    ns.ElementReference = ElementReference;

})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
