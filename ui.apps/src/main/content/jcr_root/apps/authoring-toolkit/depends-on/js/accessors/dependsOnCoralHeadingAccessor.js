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
 * Coral 3 Heading accessor
 * */
(function ($, ns) {
    const HEADING_SELECTOR = '.coral-Heading';

    ns.ElementAccessors.registerAccessor({
        selector: `${HEADING_SELECTOR}`,
        get: function ($el) {
            return $el.text() || '';
        },
        set: function ($el, value, notify) {
            $el.text(value);
            notify && $el.trigger('change');
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
