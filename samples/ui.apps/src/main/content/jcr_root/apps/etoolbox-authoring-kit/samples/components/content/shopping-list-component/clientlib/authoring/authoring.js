window.ToolKitSamples = window.ToolKitSamples || {};

window.ToolKitSamples.SHOPPING_DISABLED_TEXT = 'Too many weapons for wonderful hero >:C';

window.ToolKitSamples.getShoppingDefaultText = function (checkboxes, currentValue) {
    if (checkboxes.every((item) => item)) {
        return ToolKitSamples.SHOPPING_DISABLED_TEXT;
    }
    return currentValue === ToolKitSamples.SHOPPING_DISABLED_TEXT ? '' : currentValue;
};
