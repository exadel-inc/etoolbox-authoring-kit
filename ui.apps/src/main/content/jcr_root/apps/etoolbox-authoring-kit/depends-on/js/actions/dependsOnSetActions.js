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
 * DependsOn Set Value Basic Actions.
 *
 * Set actions:
 * - set - set the field value from the query result;
 * - set-if-blank - set the field value from the query result only if the field value is blank
 * */
(function ($, ns) {
    'use strict';

    /**
     * Set the field value from the query result, skip undefined query results
     * query type: string
     * */
    ns.ActionRegistry.register('set', function setValue(value) {
        if (value !== undefined) {
            ns.ElementAccessors.setValue(this.$el, value);
        }
    });

    /**
     * Set the field value from the query result only if the field value is blank,
     * skip undefined query results
     * query type: string
     * */
    ns.ActionRegistry.register('set-if-blank', function setValueIfBlank(value) {
        const current = ns.ElementAccessors.getValue(this.$el);
        if ((current === '' || current === null || current === undefined) && value !== undefined) {
            ns.ElementAccessors.setValue(this.$el, value);
        }
    });

    /** Set the form field placeholder */
    ns.ActionRegistry.register('placeholder', function setPlaceholder(value) {
        if (value !== undefined) {
            ns.ElementAccessors.setPlaceholder(this.$el, value);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
