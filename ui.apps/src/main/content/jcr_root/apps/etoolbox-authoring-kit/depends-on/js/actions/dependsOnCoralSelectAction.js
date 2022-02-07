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
 * @author Natasha Gorshunova
 *
 * DependsOn Coral 3 Select Action.
 *
 * A simple action to fill select with options with provided values
 *
 * Action callback params:
 * {Array} options
 * {string} config.selected - a dependsOn query, whose evaluation result should be a value or an array of selected values
 */
(function (Granite, $, DependsOn) {
    'use strict';

    const ACTION_NAME = 'fill-select';

    DependsOn.ActionRegistry.register(ACTION_NAME, function (options, { selected }) {
        if (!options || !options.length) {
            return;
        }

        const $select = this.$el;
        const selectedValue = DependsOn.QueryProcessor.calculateQuery(selected, $select);

        DependsOn.SelectUtils.setOptions($select, options, selectedValue);
        setTimeout(() => $select.trigger('change.dependsOn'));
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);
