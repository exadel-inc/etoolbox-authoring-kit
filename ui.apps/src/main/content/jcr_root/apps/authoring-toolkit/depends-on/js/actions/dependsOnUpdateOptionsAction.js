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
 * @author Stepan Miakchilo
 *
 * Action to refresh the option set of a Coral3 Select widget with an OptionProvider.
 * Used in cases when path to options node is authored via another widget. Change of this widget's value triggers s
 * new query for options
 */
(function (Granite, $, DependsOn) {

    'use strict';

    const ACTION_NAME = 'update-options';

    const ADDRESS_TERMINATORS = /^[./]+|\/+$/g;

    const OPTION_PROVIDER_ENDPOINT = '/apps/authoring-toolkit/datasources/option-provider';
    const ENDPOINT_EXTENSION = '.json';

    const STORED_VALUE_ATTRIBUTE = 'data-stored-value';

    const ALLOWED_TAG = 'CORAL-SELECT';

    /**
     * Retrieves a valid HTTP endpoint address and a valid property name from the provided address string, which is typically
     * the value of a form's 'action' property, and the provided name parameter that can have a relative path prefix
     *
     * @param address Raw address value
     * @param name    Raw property name parameter
     */
    function extractRequestRequisites(address, name) {
        let resourceAddress = address || '';
        let valueMember = (name || '').replace(ADDRESS_TERMINATORS, '');
        if (valueMember.indexOf('/') > -1) {
            resourceAddress += '/' + valueMember.substring(0, valueMember.lastIndexOf('/'));
            valueMember = valueMember.substring(valueMember.lastIndexOf('/') + 1);
        }
        resourceAddress += ENDPOINT_EXTENSION;
        return [resourceAddress, valueMember];
    }

    /**
     * Constructs a request address string with the query options provided
     *
     * @param options Collection of options authored via DependsOn
     */
    function createDataSourceRequestAddress(options) {
        const datasourceEndpoint = Granite.HTTP.externalize(OPTION_PROVIDER_ENDPOINT);
        const searchParams = new URLSearchParams();
        $.each(options, (key, value) => {
            searchParams.append(key, value);
        });
        return datasourceEndpoint + '?' + searchParams.toString();
    }

    /**
     * Checks the new options retrieved via an HTTP request and assigns them to the Granite Select component provided,
     * optionally restores the selected value
     *
     * @param $select       Select widget to set options for
     * @param options       New option set, represented by "raw" (non-Granite) entities
     * @param resourceAddr  Path to the JCR resource where the currently authored value resides
     * @param valueMember   Property of the JCR resource containing the authored value
     */
    function processNewOptions($select, options, resourceAddr, valueMember) {
        // Receive new options from the datasource; check if they are the same as options already present,
        // and early return in such a case
        const existingOptions = $select
            .find('coral-select-item')
            .map(function(index, item) {
                return {value: $(item).val()};
            })
            .toArray();
        if (DependsOn.isEqual(existingOptions, options)) {
            return;
        }

        // If option set needs to be changed, cache the currently selected value for this widget from the underlying
        // resource (so that a new option may be be assigned the "selected" state if its value matches),
        // and then replace the option set in DOM
        if ($select.attr(STORED_VALUE_ATTRIBUTE)) {
            setOptions($select, options, $select.attr(STORED_VALUE_ATTRIBUTE));
        } else {
            setOptionsAndRestoreSelected($select, options, resourceAddr, valueMember);
        }
    }

    /**
     * Sets a new option set to the Granite Select component
     *
     * @param $select       Select widget to set options for
     * @param options       New option set, represented by "raw" (non-Granite) entities
     * @param selectedValue The value to mark as selected in the option set
     */
    function setOptions($select, options, selectedValue) {
        const itemCollection = $select.get(0).items;
        itemCollection.clear();
        options.map(src => createOption(src, selectedValue)).forEach(option => itemCollection.add(option));
    }

    /**
     * Queries for a stored JCR resource to find the currently selected value and assigns to the Granite select if found
     *
     * @param $select       Select widget to set options for
     * @param options       New option set, represented by "raw" (non-Granite) entities
     * @param resourceAddr  Path to the JCR resource where the currently authored value resides
     * @param valueMember   Property of the JCR resource containing the authored value
     */
    function setOptionsAndRestoreSelected($select, options, resourceAddr, valueMember) {
        $.get(resourceAddr)
            .then(resource => {
                const storedValue = resource[valueMember];
                $select.attr(STORED_VALUE_ATTRIBUTE, storedValue);
                setOptions($select, options, storedValue);
            })
            .fail(() => {
                setOptions($select, options);
            });

    }

    /**
     * Creates a new Granite option
     *
     * @param src           An object having a {@code text} and a {@code value} attribute
     * @param selectedValue The match value to trigger 'selected' state of the option
     */
    function createOption(src, selectedValue) {
        return {
            value: src.value,
            content: {
                textContent: src.text
            },
            selected: (!selectedValue && !src.value) || selectedValue === src.value
        }
    }

    /**
     * Registers {@code update-options} action in DependsOn registry.
     * This action performs updating of the option set of the current Select widget

     * @param path      Path value as set in the foreign widget (a path picker) this widget depends on
     * @param options   Options to be passed with an async HTTP request to the custom datasource (same as described
     *                  in the {@code CustomDataSourceServlet} javadoc
     */
    DependsOn.ActionRegistry.register(ACTION_NAME, function(path, options) {

        // Initialize and check whether critical requisites are accessible; early return if not
        const $select = this.$el;

        const [resourceAddress, valueMember] = extractRequestRequisites(
            $select.closest('form').attr('action'),
            $select.attr('name'));

        if ($select[0].tagName !== ALLOWED_TAG
            || !path
            || resourceAddress === ENDPOINT_EXTENSION
            || !valueMember) {
            return;
        }

        // Collect options to be passed to the custom datasource endpoint (path to the JCR node to get items from, etc.)
        // and compose a HTTP query string
        options.path = path;
        options.output = 'json';
        const dataSourceAddress = createDataSourceRequestAddress(options);

        $.get(dataSourceAddress)
            .then(newOptions => {
                processNewOptions($select, newOptions, resourceAddress, valueMember);
            })
            .fail(() => {
                setOptions($select, []);
            });
    });

})(Granite, Granite.$, Granite.DependsOnPlugin);
