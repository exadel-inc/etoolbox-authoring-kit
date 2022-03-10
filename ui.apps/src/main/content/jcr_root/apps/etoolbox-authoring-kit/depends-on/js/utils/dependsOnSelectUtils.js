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

/***
 * Coral 3 Select Utils
 * */
(function (Granite, $, DependsOn) {
    'use strict';

    class SelectUtils {
        /**
         * Creates a new Granite option
         *
         * @param option - an option object
         * @param {string} option.text - text to display
         * @param {string} option.value - actual value of an option
         * @param selectedValue - value or array of values to trigger 'selected' state of the option
         */
        static createOption(option, selectedValue) {
            return {
                value: option.value,
                content: {
                    textContent: option.text
                },
                selected: (!selectedValue && !option.value) || selectedValue === option.value || (Array.isArray(selectedValue) && selectedValue.includes(option.value))
            };
        }

        /**
         * Sets a new option set to the Granite Select component
         *
         * @param {JQuery} $select - Select widget to set options for
         * @param {Array} options - new option set, represented by "raw" (non-Granite) entities
         * @param selectedValue - the value to mark as selected in the option set
         */
        static setOptions($select, options, selectedValue) {
            const itemCollection = $select.get(0).items;
            itemCollection.clear();
            options.map(option => SelectUtils.createOption(option, selectedValue)).forEach(option => itemCollection.add(option));
        }
    }

    DependsOn.SelectUtils = SelectUtils;
})(Granite, Granite.$, Granite.DependsOnPlugin);
