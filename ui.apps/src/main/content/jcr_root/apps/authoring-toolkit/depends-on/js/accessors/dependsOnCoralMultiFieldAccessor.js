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
            return {
                length,
                isEmpty
            };
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
