/**
 * @author Yana Bernatskaya (YanaBr)
 *
 * Coral 3 ColorField accessor
 * */
(function ($, ns) {
    const COLORFIELD_SELECTOR = '.coral3-ColorInput';
    const COLORFIELD_INPUT_SELECTOR = '.coral3-ColorInput-input';

    ns.ElementAccessors.registerAccessor({
        selector: `${COLORFIELD_SELECTOR}`,
        visibility: function ($el, state) {
            $el.find(COLORFIELD_INPUT_SELECTOR).attr('type', state ? 'text' : 'hidden');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
