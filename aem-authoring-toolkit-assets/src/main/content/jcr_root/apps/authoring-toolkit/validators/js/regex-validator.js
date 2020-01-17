(function ($) {

    let DEFAULT_REGEXP_VALIDATION_MSG = 'Value is not valid.';

    $(window).adaptTo('foundation-registry').register('foundation.validation.validator', {
        selector: '[data-validator-regex]',
        validate: function (element) {
            let regexpText = element.getAttribute("data-validator-regex");
            let regexp = new RegExp(regexpText);
            let currentValue = element.value || '';

            if (!regexp.test(currentValue)) {
                let message = element.getAttribute("data-validator-regex-msg");
                return AATAssets.Utils.format(message || DEFAULT_REGEXP_VALIDATION_MSG, [currentValue]);
            }
        }
    });
})(Granite.$);