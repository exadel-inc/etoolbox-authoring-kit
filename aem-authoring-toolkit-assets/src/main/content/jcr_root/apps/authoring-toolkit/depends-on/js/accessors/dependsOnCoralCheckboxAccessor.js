/**
 * @author Yana Bernatskaya (YanaBr)
 * @version 2.2.3
 *
 * Coral 3 Checkbox accessor
 * */
(function ($, ns) {
    const CHECKBOX_SELECTOR = '.coral3-Checkbox';
    const CHECKBOX_LABEL_SELECTOR = '.coral3-Checkbox-description coral-checkbox-label';

    ns.ElementAccessors.registerAccessor({
        selector: `${CHECKBOX_SELECTOR}`,
        required: function ($el, val) {
            const $checkboxLabel = $el.find(CHECKBOX_LABEL_SELECTOR);
            ns.toggleAsterisk($checkboxLabel, val);
            ns.ElementAccessors.DEFAULT_ACCESSOR.required($el, val);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));