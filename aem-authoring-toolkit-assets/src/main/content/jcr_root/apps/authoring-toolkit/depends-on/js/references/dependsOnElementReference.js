/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.1.0
 *
 * DependsOn plugin Elements Reference Registry
 * Store and manage known elements references
 * */
(function (document, $, ns) {
    'use strict';

    const ELEMENT_REF_SQ = ns.createSequence();
    class ElementReference extends ns.ObservedReference {
        /**
         * Initialize element observable adapter
         * Only one instance per element can be initialized
         * Returns existing adapter if instance already created
         * */
        constructor($el) {
            let instance = $el.data('dependsonsubject');
            if (instance) return instance;

            super(`$ref${ELEMENT_REF_SQ.next()}`);
            this.$el = $el;
            this.name = this.$el.attr('data-dependsonref');

            if (this.name === 'this') {
                console.error('[DependsOn]: "this" reference name is not allowed, it can not be reached by queries');
            }

            this.$el.data('dependsonsubject', this);
        }

        /**
         * Get current element value
         * */
        getReferenceValue() {
            let type = (this.$el.attr('data-dependsonreftype') || '').trim();
            if (!type) {
                type = ns.ElementAccessors.getPreferableType(this.$el);
            }
            return ns.castToType(ns.ElementAccessors.getValue(this.$el), type);
        };

        /**
         * Check is the Element Reference accepts passed referenced definition
         * @param {string} name
         * @param {jQuery | HTMLElement | string} [$context]
         * @returns {boolean}
         * */
        is(name, $context) {
            if (this.name !== name) return false;
            if ($context) return !!this.$el.closest($context).length;
            return true;
        }

        /**
         * Check if reference detached from actual tree
         * @return {boolean}
         * */
        isOutdated() {
            return !this.$el.closest('html').length;
        }
    }

    const refs = [];
    class ElementReferenceRegistry {
        /**
         * Register {ElementReference} by name and context
         * @param {string} name
         * @param {JQuery | HTMLElement} $context
         * @returns {ElementReference} (returns the existing one if it is already registered)
         * */
        static register(name, $context) {
            return ElementReferenceRegistry.registerElement($context.find('[data-dependsonref="' + name + '"]'));
        }

        /**
         * Register {ElementReference} by element
         * @param {JQuery} $el
         * @returns {ElementReference} (returns existing one if it is already registered)
         * */
        static registerElement($el) {
            if ($el.length > 1) {
                console.warn(`[DependsOn]: requested reference with multiple targets, the first target is used.`, $el);
            }
            const subj = new ElementReference($el.first());
            if (refs.indexOf(subj) === -1) refs.push(subj);
            return subj;
        }

        /**
         * Handle events from referenced target
         * @param {Event} event
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
         * @param {string} refName
         * @param {JQuery | HTMLElement | string} $context
         * @returns {Array<ElementReference>}
         * */
        static getAllByRefName(refName, $context) {
            return refs.filter((ref) => ref.is(refName, $context));
        }

        /**
         * Remove references that are detached
         * */
        static actualize() {
            for (let i = 0; i < refs.length ; ++i) {
                const ref = refs[i];
                if (ref.isOutdated()) {
                    ref.remove();
                    refs.splice(i--, 1);
                }
            }
        }
    }
    ns.ElementReferenceRegistry = ElementReferenceRegistry;

})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
