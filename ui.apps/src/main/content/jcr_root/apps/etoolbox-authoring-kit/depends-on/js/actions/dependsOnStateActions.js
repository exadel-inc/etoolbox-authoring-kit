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
 * DependsOn Form Field State Basic Actions.
 *
 * Defined actions:
 * - `visible` (or `visibility`) - show the field and the form field wrapper;
 * - `hidden` - hide the field and the form field wrapper;
 * - `required` - set the required state of the field;
 * - `readonly` - set the readonly state of the field;
 * - `disabled` - set the disabled state of the field;
 * - `enabled` - set the enabled state of the field;
 * */
(function ($, ns) {
    'use strict';

    /**
     * Shows the field and the form field wrapper if the query result is truthy
     * query type: boolean
     * */
    ns.ActionRegistry.register('visible', function show(state) {
        ns.ElementAccessors.setVisibility(this.$el, state, this);
    });
    /** `visibility` alias for `visible` action */
    ns.ActionRegistry.registerAlias('visibility', 'visible');

    /**
     * Hides the field and the form field wrapper if the query result is truthy
     * query type: boolean
     * */
    ns.ActionRegistry.register('hidden', function hide(state) {
        ns.ElementAccessors.setVisibility(this.$el, !state, this);
    });

    /**
     * Disable the field if the query result is truthy
     * query type: boolean
     * */
    ns.ActionRegistry.register('disabled', function setDisabled(state) {
        ns.ElementAccessors.setDisabled(this.$el, state, this);
    });

    /**
     * Enable the field if the query result is truthy
     */
    ns.ActionRegistry.register('enabled', function setEnabled(state) {
        ns.ElementAccessors.setDisabled(this.$el, !state, this);
    });

    /**
     * Change the required state of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('required', function setRequired(state) {
        state = ns.ElementAccessors.setRequired(this.$el, state, this);
        // Update label according to state
        ns.ElementAccessors.setLabelRequired(this.$el, state);
    });

    /**
     * Change the readonly state of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('readonly', function setReadonly(state) {
        ns.ElementAccessors.setReadonly(this.$el, state, this);
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
