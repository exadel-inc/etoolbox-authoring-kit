/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.2.2
 *
 * DependsOn plugin Group Reference Registry
 * Store and manage known group references
 * */
(function (document, $, ns) {
    'use strict';

    const GROUP_REF_SQ = ns.createSequence();
    class GroupReference extends ns.ObservedReference {
        constructor(name, $context) {
            super(`$group${GROUP_REF_SQ.next()}`);
            this.refs = [];
            this.name = name;
            this.$context = $context;
            this.onChange = this.update.bind(this);

            this.updateRefList();
        }

        /**
         * Destroy reference
         * */
        remove() {
            super.remove();
            delete this.refs;
            delete this.$context;
        }

        /**
         * Get current element value
         * */
        getReferenceValue() {
            return (this.refs || []).map((ref) => ref.value);
        }

        /**
         * Update child references list
         * */
        updateRefList() {
            this.refs.forEach((ref) => ref.unsubscribe(this.onChange));
            this.refs = ns.ElementReferenceRegistry.getAllByRefName(this.name, this.$context);
            this.refs.forEach((ref) => ref.subscribe(this.onChange));
        }

        /**
         * Check if the reference accepts passed definition
         * @param {string} name
         * @param {jQuery | HTMLElement | string} [$context]
         * @returns {boolean}
         * */
        is(name, $context) {
            return name === this.name && this.$context.is($context);
        }

        /**
         * Check if group reference have listeners and actual context
         * @returns {boolean}
         * */
        isOutdated() {
            return !this.listenersCount || !this.$context.closest('html').length;
        }
    }

    let refs = [];
    class GroupReferenceRegistry {
        /**
         * Register {GroupReference}
         * Returns existing if it is already registered
         * */
        static register(name, $context) {
            for (const ref of refs) {
                if (ref.is(name, $context)) return ref;
            }
            const newRef = new GroupReference(name, $context);
            refs.push(newRef);
            return newRef;
        }

        /**
         * Returns all known Group References.
         * @returns {Array<GroupReference>}
         * */
        static get refs() { return refs; }

        /**
         * Remove outdated references and update actual ones
         * */
        static actualize() {
            refs = refs.filter((ref) => {
                if (ref.isOutdated()) {
                    ref.remove();
                    return false;
                }
                ref.updateRefList();
                ref.update();
                return true;
            });
        }
    }
    ns.GroupReferenceRegistry = GroupReferenceRegistry;
})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
