/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn plugin Group Reference Registry
 * Store and mange known group references
 * */
(function (document, $, ns) {
    'use strict';

    let referenceIdCounter = 0;
    class GroupReference extends ns.ObservedReference {
        constructor(refName, $context) {
            super(`$$${referenceIdCounter++}`);
            this.refs = [];
            this.name = refName;
            this.$context = $context;

            this.updateRefList();
        }

        /**
         * Child reference changed
         * */
        onChange = () => { this.update(); };

        /**
         * Get current element value
         * */
        getValue() {
            return (this.refs || []).map((ref) => ref.value);
        }

        /**
         * Update child references list
         * */
        updateRefList() {
            this.refs = ns.ElementReferenceRegistry.getAllByRefName(this.name, this.$context);
            this.refs.forEach((ref) => ref.subscribe(this.onChange));
        }

        /**
         * Check if the reference accept passed definition
         * */
        is(refName, $context) {
            return refName === this.name && this.$context.is($context);
        }
    }

    const refs = [];
    class GroupReferenceRegistry {
        /**
         * Register {GroupReference}
         * Returns existing if it is already registered
         * */
        static register(refName, $context) {
            for (let ref of refs) {
                if (ref.is(refName, $context)) return ref;
            }
            const newRef = new GroupReference(refName, $context);
            refs.push(newRef);
            return newRef;
        }

        /**
         * Returns all known Group References.
         * @returns {Array<GroupReference>}
         * */
        static get refs() { return refs; }

        static updateGroupReferences() {
            refs.forEach((ref) => {
                ref.updateRefList();
                ref.update();
            });
        }
    }
    ns.GroupReferenceRegistry = GroupReferenceRegistry;

})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));