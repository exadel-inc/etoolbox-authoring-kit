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
 * @author Liubou Masiuk, Alexey Stsefanovich (ala'n)
 * @version 1.0.0
 *
 * When a multifield is updated, actualizes its field names to their current state.
 * Handles all fields that can be adapted to 'foundation-field' and are not related to other fields (the related fields will be updated automatically)
 * */
(function (window, $) {
    'use strict';

    const MULTIFIELD_SEL = 'coral-multifield';
    const COMPOSITE_MULTIFIELD_SEL = MULTIFIELD_SEL + '[data-granite-coral-multifield-composite]';

    const isComposite = ($field) => $field.is(COMPOSITE_MULTIFIELD_SEL);

    /**
     * Actualizes the names of all fields with the current name prefix and item number: {multifieldName}/item{itemNumber}/{fieldName}.
     * E.g. a field called 'url' is positioned in the second item of './links' field. The actual name is ./links/item2/url
     * @param {JQuery} $multifield Multifield JQuery element
     * @param {String} prefix Current field prefix
     */
    function actualizeNames($multifield, prefix) {
        const isParentComposite = isComposite($multifield);
        const fieldPrefix = prefix || $multifield.adaptTo('foundation-field').getName();

        if (!fieldPrefix) return;

        const items = $multifield[0].items.getAll();
        items.forEach(function (item, i) {
            const itemPrefix = fieldPrefix + (isParentComposite ? `/item${i}/` : '');
            traverse($(item), getFieldProcessor(itemPrefix));
        });
    }

    /** Traverses all fields and applies processor function for all primary (i.e. not related) foundation fields */
    function traverse($root, process) {
        $root.children().each(function () {
            const $field = $(this);
            const fieldApi = $field.adaptTo('foundation-field');
            if (fieldApi && !$field.is('.foundation-field-related')) {
                process($field, fieldApi);
            } else {
                traverse($field, process);
            }
        });
    }

    /**
     * Creates a processor function for a single field.
     * The processor function prepends the prefix to the current field name and call the the actualizer recursively for nested multiifields
     * @param prefix Item prefix that follows '{multifieldName}/item{itemNumber}/' pattern
     */
    function getFieldProcessor(prefix) {
        return ($field, fieldApi) => {
            if (typeof fieldApi.setName !== 'function') return;

            const name = getFieldName($field, fieldApi);
            if (!name) return;

            const finalName = prefix + name;
            fieldApi.setName(finalName);

            if ($field.is(MULTIFIELD_SEL)) {
                actualizeNames($field, isComposite($field) ? finalName : prefix);
            }
        };
    }

    function getFieldName($field, fieldAPI) {
        const name = $field.data('cachedName') || fieldAPI.getName();
        $field.data('cachedName', name);
        return name;
    }

    function actualizeOnReady() {
        Coral.commons.ready(this, (el) => actualizeNames($(el), ''));
    }

    $(document).on('coral-collection:add coral-collection:remove coral-multifield:itemorder',
        COMPOSITE_MULTIFIELD_SEL, actualizeOnReady);

    $(document).on('foundation-contentloaded', function (e) {
        $(COMPOSITE_MULTIFIELD_SEL, e.target).each(actualizeOnReady);
    });
})(window, Granite.$);
