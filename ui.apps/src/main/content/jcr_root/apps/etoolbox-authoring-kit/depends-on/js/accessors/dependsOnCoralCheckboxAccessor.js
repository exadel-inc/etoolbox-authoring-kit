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
 * @author Yana Bernatskaya (YanaBr)
 *
 * Coral 3 Checkbox accessor
 * */
(function ($, ns) {
    const CHECKBOX_SELECTOR = '.coral3-Checkbox';
    const CHECKBOX_LABEL_SELECTOR = '.coral3-Checkbox-description coral-checkbox-label';
    const CHECKBOX_INPUT_SELECTOR = '.coral3-Checkbox-input';

    ns.ElementAccessors.registerAccessor({
        selector: `${CHECKBOX_SELECTOR}`,
        visibility: function ($el, state) {
            $el.find(CHECKBOX_INPUT_SELECTOR).attr('readonly', state ? null : '');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        },
        required: function ($el, state) {
            const $checkboxLabel = $el.find(CHECKBOX_LABEL_SELECTOR);
            ns.toggleAsterisk($checkboxLabel, state);
            ns.ElementAccessors.DEFAULT_ACCESSOR.required($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
