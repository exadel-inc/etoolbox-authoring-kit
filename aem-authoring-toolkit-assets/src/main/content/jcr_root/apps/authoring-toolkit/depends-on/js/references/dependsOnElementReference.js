/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn plugin Elements Reference Registry
 * Store and mange known elements references
 * */
(function (document, $, ns) {
    'use strict';

    let referenceIdCounter = 0;
    class ElementReference extends ns.ObservedReference {
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
            return ns.castToType(ns.ElementAccessors.getValue(this.$el), type);
        };

        /**
         * Check is the Element Reference accepts passed referenced definition
         * @param name {string}
         * @param [$context] {jQuery | HTMLElement | string}
         * @returns {boolean}
         * */
        is(name, $context) {
            if (this.name !== name) return false;
            if ($context) return !!this.$el.closest($context).length;
            return true;
        }
    }

    const refs = [];
    class ElementReferenceRegistry {
        /**
         * Register {ElementReference}
         * Returns the existing if it is already registered
         * */
        static register(name, $context) {
            return ElementReferenceRegistry.registerElement($context.find('[data-dependsonref="' + name + '"]'));
        }

        /**
         * Register {ElementReference}
         * Returns existing if it is already registered
         * */
        static registerElement($el) {
            const subj = new ElementReference($el);
            if (refs.indexOf(subj) === -1) refs.push(subj);
            return subj;
        }

        /**
         * Handle events from referenced target
         * @param event {Event}
         */
        static handleChange(event) {
            const reference = $(event.currentTarget).data('dependsonsubject');
            if (reference && reference.update) {
                reference.update();
            }
        }

        /**
         * Returns all known Element References.
         * @returns {Array<ElementReference>}
         * */
        static get refs() { return refs; }

        /**
         * Get Reference instance by element
         * @param refName {string}
         * @param $context {JQuery | HTMLElement | string}
         * @returns {Array<ElementReference>}
         * */
        static getAllByRefName(refName, $context) {
            return refs.filter((ref) => ref.is(refName, $context));
        }

        /**
         * Remove references that are out of html from registry
         * */
        static cleanDetachedRefs() {
            for (let i = 0; i < refs.length ; ++i) {
                const ref = refs[i];
                // Skip if the referencing element is in actual html
                if (ref.$el.closest('html').length > 0) continue;
                // Delete reference otherwise
                ref.clean();
                refs.splice(i--, 1);
            }
        }
    }
    ns.ElementReferenceRegistry = ElementReferenceRegistry;

})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
