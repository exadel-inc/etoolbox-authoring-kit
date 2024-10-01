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

    const FORM_FIELDS_SELECTOR = 'input, textarea, select';
    const CORAL_FIELD_SELECTOR = '.coral-Form-field';

    /**
     * Creates CSS selector to find by name with support of simple wildcards
     * @param path
     */
    function createSearchQuery(path) {
        if (path.endsWith('*')) return `[name^="${path.slice(0, -1)}"]`;
        if (path.startsWith('*')) return `[name$="${path.slice(1)}"]`;
        return `[name="${path}"]`;
    }

    /**
     * Find form field by a path. If the path is not provided, returns $root.
     * Supports simple wildcard syntax to find fields by name start / end:
     *  - *name - find fields with name ending with 'name'
     *  - name* - find fields with name starting with 'name'
     *  - name - find fields with name 'name'
     *
     * @param {jQuery} $root - root element to search in
     * @param {string} [path] - path to the target field
     * */
    function resolveTarget($root, path) {
        if (!path) return $root;
        const $fields = $root.find(FORM_FIELDS_SELECTOR).filter(createSearchQuery(path));
        // Resolves input field to coral form field
        return $fields.closest(CORAL_FIELD_SELECTOR);
    }

    /**
     * Set the field value from the query result, skip undefined query results
     * query type: string
     * optional parameters:
     *  - path: string - path to the target field (supports wildcards)
     * */
    ns.ActionRegistry.register('set', function setValue(value, { path }) {
        if (value === undefined) return;
        const $target = resolveTarget(this.$el, path);
        ns.ElementAccessors.setValue($target, value);
    });

    /**
     * Set the field value from the query result only if the field value is blank,
     * skip undefined query results
     * query type: string
     * optional parameters:
     *  - path: string - path to the target field (supports wildcards)
     * */
    ns.ActionRegistry.register('set-if-blank', function setValueIfBlank(value, { path }) {
        if (value === undefined) return;
        const $target = resolveTarget(this.$el, path);
        const current = ns.ElementAccessors.getValue($target);
        if (current === '' || current === null || current === undefined) {
            ns.ElementAccessors.setValue($target, value);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
