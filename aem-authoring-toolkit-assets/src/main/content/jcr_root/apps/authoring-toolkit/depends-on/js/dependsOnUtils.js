/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn plugin entry point
 *
 * Initialize DependsOnObserver and DependsOnElementReferences
 * */
(function ($, ns) {
    'use strict';

    /**
     * Extended comparison that supports NaN and Arrays
     * @returns {boolean}
     * */
    ns.isEqual = function isEqual(a, b) {
        if (a === b) return true;
        if (typeof a !== typeof b) return false;
        if (a !== a && b !== b) return true; // Both are NaNs
        if (Array.isArray(a) && Array.isArray(b)) {
            return a.length === b.length && a.every((val, i) => isEqual(val, b[i]));
        }
        return false;
    };

    /**
     * Cast field value to passed type
     * @param value
     * @param type {'boolean'|'boolstring'|'number'|'string'|'any'}
     * */
    ns.castToType = function (value, type) {
        switch (type.toLowerCase()) {
            case 'boolean':
                return Boolean(value);
            case 'boolstring':
                return String(value) === 'true';
            case 'number':
                return Number(value);
            case 'string':
                return String(value);
            default:
                return value;
        }
    };
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));