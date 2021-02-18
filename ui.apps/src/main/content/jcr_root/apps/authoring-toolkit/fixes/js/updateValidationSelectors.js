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
 * @version 1.0.0
 *
 * Coral3 validation fixes
 * */

(function () {
    const registry = $(window).adaptTo('foundation-registry');
    const selectors = registry.get('foundation.validation.selector');

    const OVERRIDES = [
        {
            condition: (selector) => selector.submittable,
            rewrite: (selector) => (selector.candidate = selector.candidate
                .split(',')
                .map((candidate) => candidate.trim() + ':not([hidden])')
                .join(','))
        },
        {
            condition: (selector) => selector.submittable === '.coral-RadioGroup',
            rewrite: (selector) => (selector.candidate = '.coral-RadioGroup:not([disabled])')
        },
        {
            condition: (selector) => selector.submittable === '.cq-RichText,.cq-RichText-editable',
            rewrite: (selector) => (selector.candidate = '.cq-RichText:not([disabled]),.cq-RichText-editable:not([disabled])')
        },
        {
            condition: (selector) => selector.submittable === 'coral-multifield',
            rewrite: (selector) => (selector.candidate = 'coral-multifield:not([disabled])')
        },
        {
            condition: (selector) => selector.submittable === '.coral-Autocomplete:not(coral-autocomplete)',
            rewrite: (selector) => (selector.candidate = '.coral-Autocomplete:not(coral-autocomplete):not([disabled])')
        }
    ];

    selectors.forEach((selector) => {
        OVERRIDES
            .filter((rule) => rule.condition(selector))
            .forEach((rule) => rule.rewrite(selector));
    });
})();
