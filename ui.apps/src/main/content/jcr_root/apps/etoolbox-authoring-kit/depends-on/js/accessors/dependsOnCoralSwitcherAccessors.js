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
 * @author Alexey Stsefanovich (ala'n), Yana Bernatskaya (YanaBr)
 *
 * Coral Single Radio Button/Checkbox/Switch
 * */
(function ($, ns) {
    const SWITCH_INPUT_SELECTOR = 'input[type=radio], input[type=checkbox]';
    const SWITCH_LABEL_SELECTOR = 'coral-checkbox-label, coral-radio-label';

    // Coral Single Radio Button/Checkbox/Switch
    ns.ElementAccessors.registerAccessor({
        selector: 'coral-radio, coral-checkbox, coral-switch',
        preferableType: 'boolean',
        get: function ($el) {
            return $el[0].checked;
        },
        set: function ($el, val, notify) {
            $el.prop('checked', val);
            notify && $el.trigger('change');
        },
        visibility: function ($el, state) {
            $el.children(SWITCH_INPUT_SELECTOR).attr('readonly', state ? null : '');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        },
        required: function ($el, state) {
            const $checkboxLabel = $el.find(SWITCH_LABEL_SELECTOR);
            if ($checkboxLabel.length) ns.toggleAsterisk($checkboxLabel, state);
            ns.ElementAccessors.DEFAULT_ACCESSOR.required($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
