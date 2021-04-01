window.AATSamples = window.AATSamples || {};

window.AATSamples.SHOPPING_DISABLED_TEXT = 'Too many weapons for wonderful hero >:C';

window.AATSamples.getShoppingDefaultText = function (checkboxes, currentValue) {
    if (checkboxes.every((item) => item)) {
        return AATSamples.SHOPPING_DISABLED_TEXT;
    }
    return currentValue === AATSamples.SHOPPING_DISABLED_TEXT ? '' : currentValue;
};
