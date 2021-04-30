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
 * DependsOn Elements Reference Registry.
 *
 * Stores and manages known elements references
 * */
(function (document, $, ns) {
    'use strict';

    const ELEMENT_REF_SQ = ns.createSequence();
    class ElementReference extends ns.ObservedReference {
        /**
         * Initialize the observable element's adapter.
         * Only one instance per element can be initialized.
         * Return the existing adapter if the instance has already been created
         * */
        constructor($el) {
            const instance = $el.data('dependsonsubject');
            if (instance) return instance;

            super(`$ref${ELEMENT_REF_SQ.next()}`);
            this.$el = $el;
            this.name = this.$el.attr('data-dependsonref');

            if (this.name === 'this') {
                console.error('[DependsOn]: "this" reference name is not allowed, it cannot be reached by queries');
            }

            // Initialize data-dependsonref attribute for @this reference to listen change action
            if (!this.name) {
                this.$el.attr('data-dependsonref', '');
            }

            this.$el.data('dependsonsubject', this);
        }

        /**
         * Get the current element's value
         * */
        getReferenceValue() {
            let type = (this.$el.attr('data-dependsonreftype') || '').trim();
            if (!type) {
                type = ns.ElementAccessors.getPreferableType(this.$el);
            }
            return ns.castToType(ns.ElementAccessors.getValue(this.$el), type);
        }

        /**
         * Check if the element reference matches the provided definition
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
         * Check if the element reference is detached from the actual tree
         * @return {boolean}
         * */
        isOutdated() {
            return !this.$el.closest('html').length;
        }
    }

    const refs = [];
    class ElementReferenceRegistry {
        /**
         * Register {ElementReference} by the name and context
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
            const subj = new ElementReference(ns.ElementAccessors.findTarget($el));
            if (refs.indexOf(subj) === -1) refs.push(subj);
            return subj;
        }

        /**
         * Handle events from the referenced target
         * @param {Event} event
         */
        static handleChange(event) {
            const reference = $(event.currentTarget).data('dependsonsubject');
            if (reference && reference.update) {
                reference.update();
            }
        }

        /**
         * Return all known Element References
         * @returns {Array<ElementReference>}
         * */
        static get refs() { return refs; }

        /**
         * Get a Reference instance by element
         * @param {string} refName
         * @param {JQuery | HTMLElement | string} $context
         * @returns {Array<ElementReference>}
         * */
        static getAllByRefName(refName, $context) {
            return refs.filter((ref) => ref.is(refName, $context));
        }

        /**
         * Remove the references that are detached
         * */
        static actualize() {
            for (let i = 0; i < refs.length; ++i) {
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
