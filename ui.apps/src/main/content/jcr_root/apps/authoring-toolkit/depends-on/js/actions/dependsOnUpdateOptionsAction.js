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
 * Action to refresh the option set of a Coral3 Select widget with an OptionProvider.
 * Used in cases when path to options node is authored via another widget. Change of this widget's value triggers s
 * new query for options
 */
(function (Granite, $, DependsOn) {

    'use strict';

    /**
     * Finds out whether the two option sets are equal, e.g. to check if the
     * newly collected options are the same as options already present in the option container
     *
     * @param optionSet0 First option set
     * @param optionSet1 Second option set
     * @returns {boolean}
     */
    function optionsAreSame(optionSet0, optionSet1) {
        if (!Array.isArray(optionSet0) || !Array.isArray(optionSet1) || optionSet0.length !== optionSet1.length) {
            return false;
        }
        for (let i = 0; i < optionSet0.length; i++) {
            if (optionSet0[i].value !== optionSet1[i].value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replaces existing option set with a new one in DOM
     *
     * @param select        Select widget to set options for
     * @param newOptions    New option set
     * @param selectedValue The value to mark as selected in the option set
     */
    function replaceOptions(select, newOptions, selectedValue) {
        select.get(0).items.clear();
        newOptions.forEach(function(item) {
            select.get(0).items.add({
                value: item.value,
                content: {
                    textContent: item.text
                },
                selected: (!selectedValue && !item.value) || selectedValue === item.value
            });
        });
    }

    /**
     * Registers {@code update-datasource} action in DependsOn registry.
     * This action performs updating of the option set of the current Select widget

     * @param path      Path value as set in the foreign widget (a path picker) this widget depends on
     * @param options   Options to be passed with an async HTTP request to the custom datasource (same as described
     *                  in the {@code CustomDataSourceServlet} javadoc
     */
    DependsOn.ActionRegistry.register('update-options', function(path, options) {

        // Initialize and check whether critical requisites are accessible; early return if not
        const select = this.$el;

        let resourceEndpoint = select.closest('form').attr('action') || '';
        let valueMember = (select.attr('name') || '').replace(/^[./]+|\/+$/g, '');
        if (valueMember.indexOf('/') > -1 && resourceEndpoint) {
            resourceEndpoint += '/' + valueMember.substring(0, valueMember.lastIndexOf('/'));
            valueMember = valueMember.substring(valueMember.lastIndexOf('/') + 1);
        }
        resourceEndpoint += '.json';

        if (select[0].tagName !== 'CORAL-SELECT' || !path || resourceEndpoint === '.json' || !valueMember) {
            return;
        }

        // Collect options to be passed to the custom datasource endpoint (path to the JCR node to get items from, etc.)
        // and compose a HTTP query string
        options.path = path;
        options.output = 'json';

        let datasourceEndpoint = Granite.HTTP.externalize('/apps/authoring-toolkit/datasources/option-provider');
        $.each(options, function (key, value) {
            datasourceEndpoint += (datasourceEndpoint.indexOf('?') > -1 ? '&' : '?') + key + '=' + value;
        });

        // Receive new options from the datasource; check if they are the same as options already present,
        // and early return in such a case
        $.get(datasourceEndpoint)
            .then(function(newOptions) {
                const existingOptions = select
                    .find('coral-select-item')
                    .map(function(index, item) {
                        return {value: $(item).val()};
                    })
                    .toArray();
                if (optionsAreSame(existingOptions, newOptions)) {
                    return;
                }

                // If option set needs to be changed, cache the currently selected value for this widget from the underlying
                // resource (so that a new option may be be assigned the "selected" state if its value matches),
                // and then replace the option set in DOM
                if (select.attr('data-stored-value')) {
                    replaceOptions(select, newOptions, select.attr('data-stored-value'));
                } else if (!select.attr('data-noCache')) {
                    $.get(resourceEndpoint)
                        .then(function (resource) {
                            const storedValue = resource[valueMember];
                            select.attr('data-stored-value', storedValue);
                            replaceOptions(select, newOptions, select.attr('data-stored-value'));
                        })
                        .fail(function() {
                            replaceOptions(select, newOptions, undefined);
                        });
                } else {
                    replaceOptions(select, newOptions, undefined);
                }
            })
            .fail(function() {
                select.get(0).items.clear();
            });
    });

})(Granite, Granite.$, Granite.DependsOnPlugin);
