/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 1.0.0
 *
 * Coral 3 RTE accessor
 * */
(function ($, ns) {
    const RTE_CONTAINER = '.cq-RichText';
    const RTE_DATA_INSTANCE = 'rteinstance';
    const RTE_INPUT_SELECTOR = 'input[type="hidden"][data-cq-richtext-input]';
    const RTE_EDITOR_SELECTOR = '.cq-RichText-editable';

    ns.ElementAccessors.registerAccessor({
        selector: `${RTE_CONTAINER} ${RTE_EDITOR_SELECTOR}, ${RTE_CONTAINER} ${RTE_INPUT_SELECTOR}`,
        preferableType: 'string',
        get: function($el) {
            return $el.closest(RTE_CONTAINER).find(RTE_INPUT_SELECTOR).val() || '';
        },
        set: function ($el, value) {
            const $rteContainer = $el.closest(RTE_CONTAINER);
            const rteInstance = $rteContainer.find(RTE_EDITOR_SELECTOR).data(RTE_DATA_INSTANCE);

            $rteContainer.find(RTE_INPUT_SELECTOR).val(value);
            if (rteInstance && typeof rteInstance.setContent === 'function') {
                rteInstance.setContent(value);
            }
        },
        required: function ($el, val) {
            const $rteContainer = $el.closest(RTE_CONTAINER);
            const $rteInput = $rteContainer.find(RTE_INPUT_SELECTOR);
            $rteInput.each(function () {this.required = !!val;});
            $rteContainer.find(RTE_EDITOR_SELECTOR).attr('aria-required', !!val);
            ns.ElementAccessors.updateValidity($rteInput);
        },
        visibility: function ($el, val) {
            const $rteContainer = $el.closest(RTE_CONTAINER);
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($rteContainer, val);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));