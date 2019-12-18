/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
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
            this.id = id;
            this._listeners = [];
        }

        clean() {
            this._listeners = [];
            delete this.id;
        }

        /**
         * Add observer
         * */
        subscribe(listener) {
            if (typeof listener === 'function' && this._listeners.indexOf(listener) === -1) {
                this._listeners.push(listener);
            }
        }

        /**
         * Emit change
         * */
        emit() {
            this._listeners.forEach((cb) => cb.call(null, this));
        }

        // noinspection JSMethodCanBeStatic
        /**
         * @abstract
         * */
        getValue() {
            return null;
        }

        /**
         * Triggers value update.
         * Fetch current value, update cached, notify subscribers if value changed.
         * @param {Boolean} [force] - force update
         * */
        update(force) {
            const value = this.getValue();
            if (force || !ns.isEqual(value, this._value)) {
                this._value = value;
                this.emit();
            }
        };

        /**
         * Get cached value of element, request current element value if undefined
         * */
        get value() {
            if (!this.hasOwnProperty('_value')) {
                return (this._value = this.getValue());
            }
            return this._value;
        }
    }

    ns.ObservedReference = ObservedReference;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
