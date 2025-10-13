/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Liubou Masiuk
 *
 * Coral Alert accessor
 * */
(function ($, ns) {
    const ALERT_SELECTOR = 'coral-alert';

    ns.ElementAccessors.registerAccessor({
        selector: `${ALERT_SELECTOR}`,
        preferableType: 'object',
        get: function ($el) {
            const element = $el[0];
            const result = {};
            if (element.header) result.title = element.header.innerText;
            if (element.content) result.text = element.content.innerText;
            return result;
        },
        set: function ($el, value, notify) {
            const element = $el[0];
            if (ns.isObject(value)) {
                value.title && element.set('header', { innerHTML: value.title });
                value.text && element.set('content', { innerHTML: value.text });
                value.variant && element.set('variant', value.variant);
                value.size && element.set('size', value.size);
            } else {
                element.set('content', { innerHTML: value });
            }

            notify && $el.trigger('change');
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
