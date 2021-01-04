/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.2.2
 *
 * DependsOn plugin Observed Reference
 * ObservedReference is a base class for references
 *  - stores cached value
 *  - implements ObservedSubject pattern
 * */
(function ($, ns) {
    'use strict';

    class ObservedReference {
        /**
         * @constructor
         * @param {string} id alias to access reference in parsed query
         * */
        constructor(id) {
            this.id = id;
            this._listenersSet = new Set();
        }

        /**
         * Destroy reference
         * */
        remove() {
            this._listenersSet.clear();
        }

        /**
         * Add observer
         * */
        subscribe(listener) {
            if (typeof listener !== 'function') return;
            this._listenersSet.add(listener);
        }

        /**
         * Remove observer
         * */
        unsubscribe(listener) {
            if (typeof listener !== 'function') return;
            this._listenersSet.delete(listener);
        }

        /**
         * Emit change
         * */
        emit() {
            this._listenersSet.forEach((listener) => {
                if (listener.call(null, this)) {
                    this._listenersSet.delete(listener);
                }
            });
        }

        // noinspection JSMethodCanBeStatic
        /**
         * Get element value
         * @abstract
         * */
        getReferenceValue() {
            return null;
        }

        /**
         * Triggers value update.
         * Fetch current value, update cached, notify subscribers if value changed.
         * @param {boolean} [force] - force update
         * */
        update(force) {
            const value = this.getReferenceValue();
            if (force || !ns.isEqual(value, this._value)) {
                this._value = value;
                this.emit();
            }
        }

        /**
         * Returns actual listeners count
         * @returns {number}
         * */
        get listenersCount() {
            return this._listenersSet.size;
        }

        /**
         * Accessor to the cached value of element
         * @returns cached value
         * */
        get value() {
            if (!Object.prototype.hasOwnProperty.call(this, '_value')) {
                return (this._value = this.getReferenceValue());
            }
            return this._value;
        }
    }

    ns.ObservedReference = ObservedReference;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
