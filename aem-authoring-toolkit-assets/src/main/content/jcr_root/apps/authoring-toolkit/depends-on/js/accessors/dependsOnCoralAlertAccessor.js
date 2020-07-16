/**
 * @author Liubou Masiuk
 * @version 2.2.4
 *
 * Coral 3 Alert accessor
 * */
(function ($, ns) {
    const ALERT_SELECTOR = '.coral3-Alert';

    ns.ElementAccessors.registerAccessor({
        selector: `${ALERT_SELECTOR}`,
        preferableType: 'object',
        get: function ($el) {
            const element = $el[0];
            const result = {}
            if (element.header) result.title = element.header.innerText;
            if (element.content) result.text = element.content.innerText;
            return result;
        },
        set: function ($el, value, notify) {
            const element = $el[0];
            if (ns.isObject(value)) {
                value.title && element.set('header', {innerHTML: value.title});
                value.text && element.set('content', {innerHTML: value.text});
                value.variant && element.set('variant', value.variant);
                value.size && element.set('size', value.size);
            } else {
                element.set('content', {innerHTML: value});
            }

            notify && $el.trigger('change');
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));