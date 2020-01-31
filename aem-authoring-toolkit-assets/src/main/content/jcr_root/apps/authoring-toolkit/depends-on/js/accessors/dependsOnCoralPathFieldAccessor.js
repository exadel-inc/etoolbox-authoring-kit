/**
 * @author Yana Bernatskaya (YanaBr)
 * @version 2.2.3
 *
 * Coral 3 PathField accessor
 * */
(function ($, ns) {
    const PATHFIELD_SELECTOR = 'foundation-autocomplete[pickersrc]';
    const PATHFIELD_INPUT_SELECTOR = '.coral3-Textfield';

    ns.ElementAccessors.registerAccessor({
        selector: `${PATHFIELD_SELECTOR}`,
        required: function ($el, val) {
            ns.ElementAccessors.DEFAULT_ACCESSOR.required($el.find(PATHFIELD_INPUT_SELECTOR), val);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));