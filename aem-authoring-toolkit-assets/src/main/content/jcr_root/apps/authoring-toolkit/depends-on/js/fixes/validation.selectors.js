(function () {
    const registry = $(window).adaptTo("foundation-registry");
    const selectors = registry.get("foundation.validation.selector");

    const OVERRIDES = [
        {
            condition: (selector) => selector.submittable === '.coral-RadioGroup',
            rewrite: (selector) => selector.candidate = '.coral-RadioGroup:not([disabled])'
        }
    ];

    selectors.forEach((selector) => {
        OVERRIDES
            .filter((rule) => rule.condition(selector))
            .forEach((rule) => rule.rewrite(selector));
    });
})();