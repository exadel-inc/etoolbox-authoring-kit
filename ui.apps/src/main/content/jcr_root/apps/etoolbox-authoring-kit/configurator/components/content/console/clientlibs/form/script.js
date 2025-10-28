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
(function (window, document, $, ns) {
    'use strict';

    const foundationUi = $(window).adaptTo('foundation-ui');
    let loadedFormValues;

    /* --------------
       Data functions
       -------------- */

    /**
     * Triggers asynchronous publishing of the configuration
     * @returns {Promise}
     */
    async function publish() {
        return await replicate('publish');
    }

    /**
     * Triggers asynchronous replication (either "activate" or "deactivate") of the configuration
     * @param {string} command
     * @returns {Promise}
     */
    async function replicate(command) {
        foundationUi.wait();
        const publishPath = $('#config').attr('action') + `.${command}.html`;
        try {
            await requestAsync('POST', publishPath);
            await reloadAsync();
            foundationUi.notify('Success', `Started ${command}ing configuration`, 'success');
        } catch (e) {
            console.error(`Failed to ${command} configuration:`, e);
            foundationUi.notify('Error', `Failed to ${command} configuration: ${getErrorMessage(e)}`, 'error');
        } finally {
            foundationUi.clearWait();
        }
    }

    /**
     * Resets the configuration to a previously stored initial value set
     * @param {boolean} keepNode
     * @returns {Promise}
     */
    async function reset(keepNode = false) {
        foundationUi.wait();
        let configPath = $('#config').attr('data-cleanup-action');
        if (!configPath.endsWith('/data') && keepNode) {
            configPath += '/data';
        }
        try {
            await requestAsync('POST', configPath, { ':operation': 'delete' });
        } catch (e) {
            console.error('Failed to reset configuration: ', e);
            foundationUi.notify('Error', 'Failed to reset configuration: ' + getErrorMessage(e), 'error');
            foundationUi.clearWait();
            return;
        }
        try {
            const hasChange = await reloadAsyncUntilChange();
            if (hasChange) {
                foundationUi.notify('Success', 'Configuration reset successfully', 'success');
            } else {
                foundationUi.notify('Error', 'There were no changes, or configuration was not reset', 'error');
            }
        } catch (e) {
            console.error('Failed to retrieve configuration update status:', e);
            foundationUi.notify('Error', 'Failed to retrieve configuration update status: ' + getErrorMessage(e), 'error');
        } finally {
            foundationUi.clearWait();
        }
    }

    /**
     * Saves the configuration
     * @returns {Promise}
     */
    async function save() {
        foundationUi.wait();
        try {
            await $('#config').adaptTo('foundation-form').submitAsync();
        } catch (e) {
            console.error('Failed to save configuration:', e);
            foundationUi.notify('Error', 'Failed to save configuration: ' + (e.message || e.statusText || e), 'error');
            foundationUi.clearWait();
            return;
        }
        try {
            const hasChange = await reloadAsyncUntilChange();
            if (hasChange) {
                foundationUi.notify('Success', 'Configuration saved successfully', 'success');
            } else {
                foundationUi.notify('Error', 'No changes or configuration was not saved', 'error');
            }
        } catch (e) {
            console.error('Failed to retrieve configuration update status:', e);
            foundationUi.notify('Error', 'Failed to retrieve configuration update status: ' + getErrorMessage(e), 'error');
        } finally {
            foundationUi.clearWait();
        }
    }

    /**
     * Triggers asynchronous unpublishing of the configuration
     * @returns {Promise}
     */
    async function unpublish() {
        return await replicate('unpublish');
    }

    /* ----------
       Data utils
       ---------- */

    /**
     * Extracts the form values into a key-value object
     * @param $form {JQuery} Form element
     * @returns {object}
     */
    function getFormValues($form) {
        const values = {};
        $form.find('[name]:not([type="hidden"])').each((index, element) => {
            const name = element.getAttribute('name');
            let value = element.value;
            if (element.type === 'checkbox' || element.tagName.toLowerCase() === 'coral-checkbox' ||
                element.type === 'radio' || element.tagName.toLowerCase() === 'coral-radio') {
                value = element.checked || element.hasAttribute('checked');
            }
            if (values[name] === undefined) {
                values[name] = value;
            } else if (Array.isArray(values[name])) {
                values[name].push(value);
            } else {
                values[name] = [values[name], value];
            }
        });
        return values;
    }

    /**
     * Compares two form values dictionaries and returns true if they are equal, false otherwise
     * @param values1 {object} The first dictionary
     * @param values2 {object} The second dictionary
     * @returns {boolean}
     */
    function formValuesEqual(values1, values2) {
        const keys1 = Object.keys(values1);
        const keys2 = Object.keys(values2);
        if (keys1.length !== keys2.length) {
            return false;
        }
        for (const key of keys1) {
            if (Array.isArray(values1[key]) && Array.isArray(values2[key])) {
                if (values1[key].length !== values2[key].length) {
                    return false;
                }
                for (let i = 0; i < values1[key].length; i++) {
                    if (values1[key][i] !== values2[key][i]) {
                        return false;
                    }
                }
            } else if (values1[key] !== values2[key]) {
                return false;
            }
        }
        return true;
    }

    /* -------------
       Network utils
       ------------- */

    /**
     * Reloads the dynamic content and returns a promise resolved with true if a data change was detected, false
     * otherwise
     * @param {boolean} trackChanges If true, the function will check if the change count has changed and return false
     * if it has not
     * @returns {Promise<boolean>} Promise resolved with true if a data change was detected or the {@code trackChanges}
     *     flag is unset, false otherwise
     */
    async function reloadAsync(trackChanges = false) {
        const $reloadContainer = $('[data-reload-target]')
        let $form = $reloadContainer.find('#config');
        const oldChangeCount = $form.length ? Number.parseInt($form.attr('data-change-count'), 10) : 0;

        const reloadTarget = $reloadContainer.attr('data-reload-target');
        let data;
        try {
            data = await requestAsync('GET', reloadTarget);
        } catch (e) {
            console.error('Failed to reload dynamic content: ', e);
            foundationUi.notify('Error', 'Failed to reload dynamic content: ' + getErrorMessage(e), 'error');
            return false;
        }

        $reloadContainer.get(0).innerHTML = $(data).get(0).innerHTML;
        $form = $reloadContainer.find('#config');
        const newChangeCount = $form.length ? Number.parseInt($form.attr('data-change-count'), 10) : 0;
        if (newChangeCount === oldChangeCount && trackChanges) {
            return false;
        }

        loadedFormValues = getFormValues($form);
        adjustButtonStates();
        return true;
    }

    /**
     * Reloads the form content until a change is detected or a maximum number of attempts is reached
     * @returns {Promise<boolean>} Promise resolved with true if a change was detected, false otherwise
     */
    async function reloadAsyncUntilChange() {
        let changed = await reloadAsync(true);
        let attempts = 1;
        while (!changed && attempts++ < 3) {
            await new Promise((resolve) => setTimeout(resolve, 1000));
            changed = await reloadAsync(true);
        }
        return changed;
    }

    /**
     * Performs an asynchronous JQuery request and returns a promise resolved with the response data
     * @param type {string} The request type (e.g. "GET", "POST", etc.)
     * @param url {string} The request URL
     * @param data {any} Optional data to send with the request
     * @returns {Promise<string>}
     */
    function requestAsync(type, url, data) {
        return new Promise((resolve, reject) => {
            return $.ajax({ type, url, data, success: (data) => resolve(data) })
                .fail((xhr, status, e) => reject(e || status));
        });
    }

    /* --------
       UI utils
       -------- */

    /**
     * Adjusts the states of action buttons according to the form metadata
     */
    function adjustButtonStates() {
        const $form = $('#config');
        if ($form.find('[name]').length === 0) {
            $('#button-save').attr('disabled', true);
        } else {
            const currentFormValues = getFormValues($form);
            $('#button-save').attr('disabled', formValuesEqual(loadedFormValues, currentFormValues));
        }
        const isNotModified = $form.attr('data-modified') !== 'true';
        $('#button-reset').attr('disabled', isNotModified);
        $('#button-publish').attr('disabled', isNotModified);
        $('#button-unpublish').attr('disabled', $form.attr('data-published') !== 'true');
    }

    /**
     * Extracts a meaningful error message from an error object
     * @param e {any}
     * @returns {string}
     */
    function getErrorMessage(e) {
        const message = (e.responseJSON && e.responseJSON.message) || e.message || e.statusText || e;
        return message === 'error' ? 'Network error' : message;
    }

    /**
     * Shows a prompt dialog and returns a promise resolved with the action taken by the user
     * @param {string} title
     * @param {string} message
     * @param {string} type
     * @returns {Promise}
     */
    function prompt(title, message, type = 'default') {
        return new Promise((resolve) => {
            foundationUi.prompt(
                title,
                message,
                type,
                [
                    { id: 'yes', text: 'Yes', primary: true },
                    { id: 'no', text: 'No' }
                ],
                (action) => resolve(action)
            );
        });
    }

    /* --------------
       Event handlers
       -------------- */

    /**
     * Handles the "foundation-contentloaded" event
     */
    function onContentLoaded() {
        const $form = $('#config');
        const heading = $form.find('h2').html();
        if (heading) {
            document.title += ' - ' + heading;
            $('#page-title').get(0).innerHTML += ' &ndash; ' + heading;
        }
        loadedFormValues = getFormValues($form);
        adjustButtonStates();
    }

    /**
     * Handles a click on the "Publish" button
     */
    async function onPublishClick() {
        if (await prompt('Publish configuration', 'Publish this configuration?', 'warning') !== 'yes') {
            return;
        }
        const foundationForm = $('#config').adaptTo('foundation-form');
        if (foundationForm.isDirty()) {
            const action = await prompt(
                'Unsaved changes',
                'Save current changes before publishing?');
            if (action === 'yes') {
                await save();
            }
        }
        await publish();
    }

    /**
     * Handles a click on the "Reset" button
     */
    async function onResetClick() {
        const $form = $('#config');
        let keepNode = false;
        if ($form.attr('data-published') === 'true' && $form.attr('data-replicable') === 'true') {
            const action = await prompt(
                'Published configuration',
                'This configuration is published. Do you want to unpublish it before resetting?',
                'warning');
            if (action === 'yes') {
                await unpublish();
            } else {
                keepNode = true;
            }
        }
        await reset(keepNode);
    }

    /**
     * Handles a click on the "Save" button
     */
    async function onSaveClick() {
        await save();
    }

    /**
     * Handles a click on the "Unpublish" button
     */
    async function onUnpublishClick() {
        if (await prompt('Unpublish configuration', 'Unpublish this configuration?', 'warning') !== 'yes') {
            return;
        }
        let action;
        const $form = $('#config');
        if ($form.attr('data-modified') === 'true') {
            action = await prompt('Unpublish configuration', 'Do you want to also reset this configuration on the current instance?');
        }
        await unpublish();
        if (action === 'yes') {
            await reset();
        }
    }

    $(document)
        .off('.eak')
        .one('foundation-contentloaded.eak', onContentLoaded)
        .on('foundation-field-change.eak', adjustButtonStates)
        .on('keyup.eak', 'input,textarea', ns.debounce(adjustButtonStates, 500))
        .on('click.eak', '#button-publish', onPublishClick)
        .on('click.eak', '#button-reset', onResetClick)
        .on('click.eak', '#button-save', onSaveClick)
        .on('click.eak', '#button-unpublish', onUnpublishClick);
})(window, document, Granite.$, Granite.EAKUtils);
