/**
 * @author Yana Bernatskaya (YanaBr)
 * @version 2.2.4
 *
 * Coral 3 Checkbox accessor
 * */
(function ($, ns) {
    const CHECKBOX_SELECTOR = '.coral3-Checkbox';
    const CHECKBOX_LABEL_SELECTOR = '.coral3-Checkbox-description coral-checkbox-label';
    const CHECKBOX_INPUT_SELECTOR = '.coral3-Checkbox-input';

    ns.ElementAccessors.registerAccessor({
        selector: `${CHECKBOX_SELECTOR}`,
        visibility: function ($el, state) {
            $el.find(CHECKBOX_INPUT_SELECTOR).attr('type', state ? 'checkbox' : 'hidden');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        },
        required: function ($el, state) {
            const $checkboxLabel = $el.find(CHECKBOX_LABEL_SELECTOR);
            ns.toggleAsterisk($checkboxLabel, state);
            ns.ElementAccessors.DEFAULT_ACCESSOR.required($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));