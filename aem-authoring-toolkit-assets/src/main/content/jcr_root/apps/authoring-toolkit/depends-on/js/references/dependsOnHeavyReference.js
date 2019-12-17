/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn plugin Group Observable Reference
 * @extends ObservedReference
 *
 * Define group (heavy) reference.
 * */
(function (document, $, ns) {
    'use strict';

    let referenceIdCounter = 0;

    class HeavyReference extends ns.ObservedReference {
        constructor(refName, $context) {
            super(`$$${referenceIdCounter++}`);
            this.refs = [];
            this.name = refName;
            this.$context = $context;

            this.updateRefList();
        }

        onChange = () => {
            this.update();
        };

        /**
         * Get current element value
         * */
        getValue() {
            return (this.refs || []).map((ref) => ref.value);
        }

        updateRefList() {
            this.refs = ns.ReferenceRegistry.getAllByRefName(this.name, this.$context);
            this.refs.forEach((ref) => ref.subscribe(this.onChange));
        }

        is(refName, $context) {
            return refName === this.name && this.$context.is($context);
        }
    }
    ns.HeavyReference = HeavyReference;

    const refRegistry = [];
    class HeavyReferenceRegistry {
        static registerElement(refName, $context) {
            let ref;
            for (ref of refRegistry) {
                if (ref.is(refName, $context)) return ref;
            }
            refRegistry.push(ref = new ns.HeavyReference(refName, $context));
            return ref;
        }

        static get refs() {
            return refRegistry;
        }
    }
    ns.HeavyReferenceRegistry = HeavyReferenceRegistry;

})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));