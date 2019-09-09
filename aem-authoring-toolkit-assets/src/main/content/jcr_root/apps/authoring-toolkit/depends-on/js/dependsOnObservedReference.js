/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 1.0.0
 *
 * DependsOn plugin Observed Reference
 * ObservedReference is a base class for references
 *  - stores cached value
 *  - implements ObservedSubject pattern
 * */
(function ($, ns) {
    'use strict';

    class ObservedReference {
        constructor(id) {
            this.id = '$' + id;
            this.listeners = [];
        }

        clean() {
            this.listeners = [];
            delete this.id;
        }

        /**
         * Add observer
         * */
        subscribe(listener) {
            if (typeof listener === 'function' && this.listeners.indexOf(listener) === -1) {
                this.listeners.push(listener);
            }
        }

        /**
         * Emmit change
         * */
        emit() {
            this.listeners.forEach((cb) => cb.call(null, this));
        }

        // noinspection JSMethodCanBeStatic
        /**
         * @abstract
         * */
        getValue() {
            return null;
        }

        /**
         * Get cached value of element, request current element value if undefined
         * */
        getCachedValue() {
            if (!this.hasOwnProperty('value')) {
                return (this.value = this.getValue());
            }
            return this.value;
        };

        /**
         * Triggers value update.
         * Fetch current value, update cached, notify subscribers if value changed.
         * @param {Boolean} [force] - force update
         * */
        update(force) {
            const value = this.getValue();
            if (force || this.value !== value) {
                this.value = value;
                this.emit();
            }
        };
    }

    ns.ObservedReference = ObservedReference;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
