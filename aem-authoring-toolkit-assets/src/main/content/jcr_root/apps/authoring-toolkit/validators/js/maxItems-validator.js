(function ($) {

    let DEFAULT_MAXITEMS_VALIDATION_MSG = 'A maximum of {1} items is required here.';

    $(window).adaptTo('foundation-registry').register('foundation.validation.validator', {
        selector: '[data-validator-maxitems]',
        validate: function (element) {
            let maxItems = element.getAttribute("data-validator-maxitems");

            if (element.items.length > maxItems) {
                let message = element.getAttribute("data-validator-maxitems-msg");
                return AATAssets.Utils.format(message || DEFAULT_MAXITEMS_VALIDATION_MSG, [element.items.length, maxItems]);
            }
        }
    });
})(Granite.$);