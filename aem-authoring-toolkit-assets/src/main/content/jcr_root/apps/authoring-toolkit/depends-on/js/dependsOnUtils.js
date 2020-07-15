/**
 * @author Alexey Stsefanovich (ala'n), Yana Bernatskaya (YanaBr)
 * @version 2.2.4
 *
 * DependsOn plugin utils
 * */
(function ($, ns) {
    'use strict';

    /**
     * Create sequence generator
     * */
    ns.createSequence = function() {
        let index = 1;
        return { next: () => index++ };
    };

    /**
     * Split string by {@param separator} and trim
     * @param {string} value
     * @param {string} [separator] (default ';')
     * @returns {string[]}
     * */
    ns.splitAndTrim = function splitAndTrim(value, separator = ';') {
        return value.split(separator).map((term) => term.trim());
    };

    /**
     * Extended comparison that supports NaN, Arrays and Objects
     * @returns {boolean}
     * */
    ns.isEqual = function isEqual(a, b) {
        if (a === b) return true;
        if (typeof a !== typeof b) return false;
        if (a !== a && b !== b) return true; // Both are NaNs
        if (Array.isArray(a) && Array.isArray(b)) {
            return a.length === b.length && a.every((val, i) => isEqual(val, b[i]));
        }
        if (ns.isObject(a) && ns.isObject(b)) {
            const keysA = Object.keys(a);
            const keysB = Object.keys(b);
            if (keysA.length !== keysB.length) return false;
            return keysA.every(key => isEqual(a[key], b[key]));
        }
        return false;
    };

    /**
     * Cast field value to passed type
     * @param value
     * @param {'boolean'|'boolstring'|'number'|'string'|'json'|'any'} type
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
            case 'json':
                try {
                    return JSON.parse(value);
                } catch (e) {
                    return {};
                }
            default:
                return value;
        }
    };

    /**
     * Find element by provided selector. Use back-forward search:
     * First part of selector will be used to find closest element
     * If the second part after '|>' provided will search back element by second part of selector inside of closest parent
     * founded on the previous state.
     * If 'this' passed as a sel $root will be returned
     * If sel is not provided then result will be $(document).
     *
     * @param {JQuery} $root
     * @param {string} sel
     * */
    ns.findBaseElement = function ($root, sel) {
        if (!sel) return $(document.body);
        if (sel.trim() === 'this') return $root;
        const selParts = sel.split('|>');
        if (selParts.length > 1) {
            return $root.closest(selParts[0].trim()).find(selParts[1].trim());
        } else {
            return $root.closest(sel.trim());
        }
    };

    /**
     * Parse action data params into object
     * @param {HTMLElement} el - target element
     * @param {string} actionName
     * @param {number} [index] - action order if multiple actions of the same type attached
     * @returns {object}
     * */
    ns.parseActionData = function (el, actionName = '', index = 0) {
        const prefix = `data-dependson-${actionName}-`;
        const suffix = index ? `-${index}`: '';

        let attrs = [].slice.call(el.attributes);
        attrs = attrs.filter((attr) => attr.name.slice(0, prefix.length) === prefix);
        attrs = index ?
            attrs.filter((attr) => attr.name.slice(-suffix.length) === suffix) :
            attrs.filter((attr) => !/-(\d+)$/.test(attr.name));

        // Build object
        return attrs.reduce((data, attr) => {
            const name = attr.name.slice(prefix.length, attr.name.length - suffix.length);
            if (name) data[name] = attr.value;
            return data
        }, {});
    };

    /**
     * @param $el {JQuery}
     * @param state {boolean}
     */
    ns.toggleAsterisk = function ($el, state) {
        $el.text($el.text().replace(/\s?\*?$/, state ? ' *': ''));
    }

    /**
     * Check if the passed value is an object
     * @param value - value to check
     * @returns {boolean} true if the value is an object, false otherwise
     * */
    ns.isObject = function (value) {
        return value !== null && typeof value === 'object';
    }
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));