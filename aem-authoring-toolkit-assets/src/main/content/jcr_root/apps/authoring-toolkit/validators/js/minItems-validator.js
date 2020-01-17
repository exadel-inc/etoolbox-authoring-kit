(function ($) {

    let DEFAULT_MINITEMS_VALIDATION_MSG = 'A minimum of {1} items is required here.';

    $(window).adaptTo('foundation-registry').register('foundation.validation.validator', {
        selector: '[data-validator-minitems]',
        validate: function (element) {
            let minItems = element.getAttribute("data-validator-minitems");

            if (element.items.length < minItems) {
                let message = element.getAttribute("data-validator-minitems-msg");
                return AATAssets.Utils.format(message || DEFAULT_MINITEMS_VALIDATION_MSG, [element.items.length, minItems]);
            }
        }
    });
})(Granite.$);