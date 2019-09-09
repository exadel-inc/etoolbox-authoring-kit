/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 1.0.0
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
         * Handle events from referenced target
         * @param event {Event}
         */
        static handleChange(event) {
            const reference = $(event.currentTarget).data('dependsonsubject');
            if (reference && reference.update) {
                reference.update();
            }
        }

        constructor($el) {
            let instance = $el.data('dependsonsubject');
            if (instance) return instance;

            super(referenceIdCounter++);
            this.$el = $el;
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
    }
    ns.ElementReference = ElementReference;

    $(document)
        .off('change.dependsOn').on('change.dependsOn', '[data-dependsonref]', ElementReference.handleChange)
        .off('selected.dependsOn').on('selected.dependsOn', '[data-dependsonref]', ElementReference.handleChange);

})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
