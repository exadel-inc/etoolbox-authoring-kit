/**
 * @author Liubou Masiuk
 * @version 2.2.3
 *
 * Coral 3 Heading accessor
 * */
(function ($, ns) {
    const HEADING_SELECTOR = '.coral-Heading';

    ns.ElementAccessors.registerAccessor({
        selector: `${HEADING_SELECTOR}`,
        get: function ($el) {
            return $el.text() || '';
        },
        set: function ($el, value, notify) {
            $el.text(value);
            notify && $el.trigger('change');
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
