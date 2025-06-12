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
 * @author Alexey Stsefanovich (ala'n)
 *
 * Coral 2 Autocomplete accessor
 * */
(function ($, ns) {
    const AUTOCOMPLETE_SELECTOR = '.coral-Autocomplete';
    const INPUT_SELECTOR = '.js-coral-Autocomplete-textfield';

    ns.ElementAccessors.registerAccessor({
        selector: AUTOCOMPLETE_SELECTOR,
        get: function ($el) {
            const api = $el.adaptTo('foundation-field');
            return api && (api.getValue() || '');
        },
        set: function ($el, value, notify) {
            const api = $el.adaptTo('foundation-field');
            api && api.setValue(value);
            notify && $el.trigger('change');
        },
        placeholder: function ($el, value) {
            $el.find(INPUT_SELECTOR).attr('placeholder', value);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
