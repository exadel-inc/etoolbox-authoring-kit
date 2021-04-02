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
 * @author Bernatskaya Yana (YanaBr)
 *
 * Coral 3 PathField accessor
 * */
(function ($, ns) {
    const PATHFIELD_SELECTOR = 'foundation-autocomplete';

    ns.ElementAccessors.registerAccessor({
        selector: PATHFIELD_SELECTOR,
        disabled: function ($el, state) {
            $el.attr('aria-disabled', state ? 'true' : null);
            ns.ElementAccessors.DEFAULT_ACCESSOR.disabled($el, state);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
