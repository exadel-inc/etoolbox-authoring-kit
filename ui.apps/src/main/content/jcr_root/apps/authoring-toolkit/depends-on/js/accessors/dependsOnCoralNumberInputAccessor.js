/**
 * @author Yana Bernatskaya (YanaBr)
 *
 * Coral 3 NumberInput accessor
 * */
(function ($, ns) {
    const NUMBERINPUT_SELECTOR = '.coral3-NumberInput';
    const NUMBERINPUT_INPUT_SELECTOR = '.coral3-NumberInput-input';

    ns.ElementAccessors.registerAccessor({
        selector: `${NUMBERINPUT_SELECTOR}`,
        visibility: function ($el, state) {
            $el.find(NUMBERINPUT_INPUT_SELECTOR).attr('type', state ? 'number' : 'hidden');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
