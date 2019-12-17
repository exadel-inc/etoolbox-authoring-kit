/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn plugin Elements Reference Registry
 * Store and mange known references
 * */
(function (document, $, ns) {
    'use strict';

    const refIdMap = {};

    class ReferenceRegistry {
        /**
         * Register ElementReference
         * Returns existing if it is already registered
         * */
        static registerElement($el) {
            const subj = new ns.ElementReference($el);
            refIdMap[subj.id] = subj;
            return subj;
        }

        /**
         * Handle events from referenced target
         * @param event {Event}
         */
        static handleChange(event) {
            const reference = ReferenceRegistry.getByElement($(event.currentTarget));
            if (reference && reference.update) {
                reference.update();
            }
        }

        /**
         * Returns all known References ids.
         * @returns {Array<ElementReference>}
         * */
        static get ids() { return Object.keys(refIdMap); }

        /**
         * Returns all known Element References.
         * @returns {Array<ElementReference>}
         * */
        static get refs() { return Object.keys(refIdMap).map((id) => refIdMap[id]); }

        /**
         * Get Reference instance by id
         * @param id {string}
         * @returns {ElementReference}
         * */
        static getById(id) {
            return refIdMap[id];
        }

        /**
         * Get Reference instance by element
         * @param $el {JQuery}
         * @returns {ElementReference}
         * */
        static getByElement($el) {
            return $el.data('dependsonsubject');
        }

        /**
         * Get Reference instance by element
         * @param refName {string}
         * @param $context {JQuery | HTMLElement | string}
         * @returns {Array<ElementReference>}
         * */
        static getAllByRefName(refName, $context) {
            return ReferenceRegistry.refs.filter((ref) => ref.is(refName, $context));
        }

        /**
         * Remove references that out of html from registry
         * */
        static cleanDetachedRefs() {
            ReferenceRegistry.ids.forEach((id) => {
                const value = refIdMap[id];
                // Skip if referencing element is in actual html
                if (value.$el.closest('html').length > 0) return;
                // Delete reference otherwise
                delete refIdMap[id];
                value.clean();
            });
        }
    }
    ns.ReferenceRegistry = ReferenceRegistry;

})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
