/**
 * @author Yana Bernatskaya (YanaBr)
 * @version 2.2.4
 *
 * Coral 3 DatePicker accessor
 * */
(function ($, ns) {
    const DATEPICKER_SELECTOR = 'coral-datepicker.coral-InputGroup';
    const DATEPICKER_INPUT_SELECTOR = '.coral-InputGroup-input';

    ns.ElementAccessors.registerAccessor({
        selector: `${DATEPICKER_SELECTOR}`,
        visibility: function ($el, state) {
            $el.find(DATEPICKER_INPUT_SELECTOR).attr('type', state ? null : 'hidden');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));