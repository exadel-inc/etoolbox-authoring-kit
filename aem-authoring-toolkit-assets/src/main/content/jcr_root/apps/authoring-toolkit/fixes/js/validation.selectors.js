/**
 * @author Bernatskaya Yana (YanaBr)
 * @version 2.2.4
 *
 * Coral3 fixes
 * */

(function () {
    const registry = $(window).adaptTo("foundation-registry");
    const selectors = registry.get("foundation.validation.selector");

    const OVERRIDES = [
        {
            condition: (selector) => selector.submittable,
            rewrite: (selector) => selector.candidate = selector.candidate
                .split(',')
                .map((candidate) => candidate.trim() + ':not([hidden])')
                .join(',')
        },
        {
            condition: (selector) => selector.submittable === '.coral-RadioGroup',
            rewrite: (selector) => selector.candidate = '.coral-RadioGroup:not([disabled])'
        },
        {
            condition: (selector) => selector.submittable === '.cq-RichText,.cq-RichText-editable',
            rewrite: (selector) => selector.candidate = '.cq-RichText:not([disabled]),.cq-RichText-editable:not([disabled])'
        },
        {
            condition: (selector) => selector.submittable === 'coral-multifield',
            rewrite: (selector) => selector.candidate = 'coral-multifield:not([disabled])'
        },
        {
            condition: (selector) => selector.submittable === '.coral-Autocomplete:not(coral-autocomplete)',
            rewrite: (selector) => selector.candidate = '.coral-Autocomplete:not(coral-autocomplete):not([disabled])'
        }
    ];

    selectors.forEach((selector) => {
        OVERRIDES
            .filter((rule) => rule.condition(selector))
            .forEach((rule) => rule.rewrite(selector));
    });
})();