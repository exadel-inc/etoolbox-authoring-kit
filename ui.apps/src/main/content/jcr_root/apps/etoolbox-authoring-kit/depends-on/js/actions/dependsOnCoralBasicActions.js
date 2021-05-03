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
 * DependsOn Coral 3 Basic Actions.
 *
 * Defined actions:
 * - visibility - set the field visibility (and also hide the form field wrapper);
 * - required - set the required state of the field;
 * - readonly - set the readonly state of the field;
 * - disabled - set the disabled state of the field;
 * - set - set the field value from the query result;
 * - set-if-blank - set the field value from the query result only if the field value is blank
 * */
(function ($, ns) {
    'use strict';

    /**
     * Change the visibility of the field and the form field wrapper
     * query type: boolean
     * */
    ns.ActionRegistry.register('visibility', function setVisibility(state) {
        ns.ElementAccessors.setVisibility(this.$el, state);
    });

    /**
     * Change the required state of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('required', function setRequired(state) {
        // Update label according to state
        ns.ElementAccessors.setLabelRequired(this.$el, state);
        ns.ElementAccessors.setRequired(this.$el, state);
    });

    /**
     * Change the readonly state of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('readonly', function setReadonly(state) {
        this.$el.attr('readonly', state ? 'true' : null);
    });

    /**
     * Change the disabled state of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('disabled', function setDisabled(state) {
        ns.ElementAccessors.setDisabled(this.$el, state);
    });

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
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
