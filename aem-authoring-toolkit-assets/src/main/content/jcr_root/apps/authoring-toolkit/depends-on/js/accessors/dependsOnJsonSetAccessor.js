/**
 * @author Liubou Masiuk
 * @version 2.2.4
 *
 * Provides 'set' accessor for Coral textfield and hidden field that allows storing serialized objects
 * */
(function ($, ns) {
    const SELECTOR = '.coral3-Textfield,input[type="hidden"]';

    ns.ElementAccessors.registerAccessor({
        selector: `${SELECTOR}`,
        set: function ($el, value, notify) {
            if (ns.isObject(value)) {
                value = JSON.stringify(value);
            }
            $el.val(value);
            notify && $el.trigger('change');
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));