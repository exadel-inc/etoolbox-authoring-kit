/**
 * @author Alexey Stsefanovich (ala'n), Bernatskaya Yana (YanaBr)
 *
 * CQ Coral3 based FileUpload accessor
 * */
(function ($, ns) {
    const SELECTOR = '.cq-FileUpload, .coral3-FileUpload';
    const FILEUPLOAD_FILE_REFERENCE = '[data-cq-fileupload-parameter="filereference"]';
    const FILEUPLOAD_INPUT_SELECTOR = '.coral3-FileUpload-input';

    ns.ElementAccessors.registerAccessor({
        selector: SELECTOR,
        preferableType: 'string',
        get: function ($el) {
            return $el.find(FILEUPLOAD_FILE_REFERENCE).val();
        },
        // set: function ($el, value) {
        //     return $el.find(FILEUPLOAD_FILE_REFERENCE).val(value);
        // },
        visibility: function ($el, state) {
            $el.find(FILEUPLOAD_INPUT_SELECTOR).attr('type', state ? 'file' : 'hidden');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        },
        disabled: function ($el, val) {
            ns.ElementAccessors.DEFAULT_ACCESSOR.disabled($el, val);

            $el.find(':input, button').each(function () {
                this.disabled = val;
            });
        }
    });

    // CQ Coral3 Fixes
    $(document).on('click', `${SELECTOR} [coral-fileupload-clear]`, function () {
        // Clear handler is not producing change event so handle it manually
        $(this).closest(SELECTOR).trigger('change');
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
