/**
 * @author Yana Bernatskaya (YanaBr)
 * @version 2.2.3
 *
 * Coral 3 MultiField accessor
 * */
(function ($, ns) {
    const MULTIFIELD_SELECTOR = '.coral3-Multifield';

    ns.ElementAccessors.registerAccessor({
        selector: `${MULTIFIELD_SELECTOR}`,
        preferableType: 'object',
        get: ($el) => {
            const element = $el[0];
            const length = element ? element.items.length : 0;
            const isEmpty = !element || !length;
            return  {
                length,
                isEmpty
            };
        },
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));