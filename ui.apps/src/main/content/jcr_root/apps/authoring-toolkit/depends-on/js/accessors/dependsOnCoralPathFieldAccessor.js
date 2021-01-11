/**
 * @author Bernatskaya Yana (YanaBr)
 * @version 2.2.3
 *
 * Coral 3 PathField accessor
 * */
(function ($, ns) {
    const PATHFIELD_SELECTOR = 'foundation-autocomplete';

    ns.ElementAccessors.registerAccessor({
        selector: PATHFIELD_SELECTOR,
        disabled: function ($el, state) {
            $el.attr('aria-disabled', state ? 'true' : null);
            ns.ElementAccessors.DEFAULT_ACCESSOR.disabled($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));