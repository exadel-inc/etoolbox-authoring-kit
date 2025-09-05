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
(function (window, document, $) {
    'use strict';

    const foundationUi = $(window).adaptTo('foundation-ui');

    /* --------------
       Data functions
       -------------- */

    /**
     * Gets if the configuration has unsaved modifications according to the form metadata
     * @param {jQuery} $form
     * @returns {boolean}
     */
    function isModified($form) {
        return $form.find('#modified').val() === 'true';
    }

    /**
     * Gets if the configuration is published according to the form metadata
     * @param {jQuery} $form
     * @returns {boolean}
     */
    function isPublished($form) {
        return $form.find('#published').val() === 'true';
    }

    /**
     * Triggers asynchronous publishing of the configuration
     * @returns {Promise}
     */
    function publish() {
        return replicate('publish');
    }

    /**
     * Triggers asynchronous replication (either "activate" or "deactivate") of the configuration
     * @param {string} command
     * @returns {Promise}
     */
    function replicate(command) {
        const $form = $('#config');
            const publishPath = $form.attr('action') + `.${command}.html`;
        return $.ajax({
            type: 'POST',
            url: publishPath
        })
            .done(function () {
                reload();
                foundationUi.notify('Success', `Started ${command}ing configuration`, 'success');
            })
            .fail(function (xhr, status, e) {
                const message = (xhr.responseJSON && xhr.responseJSON.message) || e.message || e;
                console.error(`Failed to ${command} configuration: ${message}`);
                foundationUi.notify('Error', `Failed to ${command} configuration: ${message}`, 'error');
            })
            .promise();
    }

    /**
     * Triggers resetting of the configuration to default values
     * @returns {Promise}
     */
    function reset() {
        const configPath = $('#config').attr('action');
        return $.ajax({
            type: 'DELETE',
            url: configPath
        })
            .done(function () {
                reload();
                foundationUi.notify('Success', 'Configuration reset successfully', 'success');
            })
            .fail(function (xhr, status, e) {
                const message = (xhr.responseJSON && xhr.responseJSON.message) || e.message || e;
                console.error('Failed to reset configuration: ' + message);
                foundationUi.notify('Error', 'Failed to reset configuration: ' + message, 'error');
            })
            .promise();

    }

    /**
     * Triggers asynchronous saving of the configuration
     * @returns {Promise}
     */
    function save() {
        const foundationForm = $('#config').adaptTo('foundation-form');
        return foundationForm.submitAsync()
            .done(function () {
                reload();
                foundationUi.notify('Success', 'Configuration saved successfully', 'success');
            })
            .fail(function (xhr, status, e) {
                const message = (xhr.responseJSON && xhr.responseJSON.message) || e.message || e;
                console.error('Failed to update configuration: ' + message);
                foundationUi.notify('Error', 'Failed to update configuration: ' + message, 'error');
            })
            .promise();
    }

    /**
     * Triggers asynchronous unpublishing of the configuration
     * @returns {Promise}
     */
    function unpublish() {
        return replicate('unpublish');
    }

    /* -----------------
       Utility functions
       ----------------- */

    /**
     * Adjusts the states of action buttons according to the form metadata
     */
    function adjustButtonStates() {
        const $form = $('#config');
        $('#button-save').attr('disabled', $form.find('[name]').length === 0);
        $('#button-reset,#button-publish').attr('disabled', !isModified($form));
        $('#button-unpublish').attr('disabled', !isPublished($form));
    }

    /**
     * Shows a prompt dialog and returns a promise resolved with the action taken by the user
     * @param {string} title
     * @param {string} message
     * @returns {Promise}
     */
    function prompt(title, message) {
        return new Promise((resolve) => {
            foundationUi.prompt(
                title,
                message,
                'default',
                [
                    { id: 'yes', text: 'Yes', primary: true },
                    { id: 'no', text: 'No' }
                ],
                (action) => resolve(action)
            );
        });
    }

    /**
     * Reloads the form content and resets the "dirty" state of the form
     */
    function reload() {
        const $form = $('#config');
        const ownPath = $form.find('#ownPath').val();
        $.get(ownPath, function (data, textStatus, xhr) {
            $form.get(0).innerHTML = $(data).get(0).innerHTML;
            adjustButtonStates();
            // This is needed to clear the "dirty" state of the form
            $form.trigger('foundation-form-submit-callback', xhr);
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
        adjustButtonStates();
    }

    /**
     * Handles a click on the "Publish" button
     */
    function onPublishClick() {
        const foundationForm = $('#config').adaptTo('foundation-form');
        let savingRoutine = Promise.resolve();
        if (foundationForm.isDirty()) {
            savingRoutine = prompt('Unsaved changes', 'Save changes before publishing configuration?')
                .then((action) => action === 'yes' ? save() : Promise.resolve());
        }
        savingRoutine.then(publish)
    }

    /**
     * Handles a click on the "Reset" button
     */
    function onResetClick() {
        const $form = $('#config');
        let unpublishingRoutine = Promise.resolve();
        if (isPublished($form)) {
            unpublishingRoutine = prompt('Published configuration', 'This configuration is published. Do you want to unpublish it before resetting?')
                .then((action) => action === 'yes' ? unpublish() : Promise.resolve());
        }
        unpublishingRoutine.then(reset);
    }

    /**
     * Handles a click on the "Save" button
     */
    function onSaveClick() {
        save();
    }

    /**
     * Handles a click on the "Unpublish" button
     */
    function onUnpublishClick() {
        unpublish()
            .then(() => prompt('Unpublished configuration', 'Do you want to also reset this configuration on the current instance?')
                .then((action) => {
                if (action === 'yes') {
                    return reset();
                }
                return Promise.resolve();
            }));
    }

    $(document)
        .off('.eak')
        .one('foundation-contentloaded.eak', onContentLoaded)
        .on('click.eak', '#button-publish', onPublishClick)
        .on('click.eak', '#button-reset', onResetClick)
        .on('click.eak', '#button-save', onSaveClick)
        .on('click.eak', '#button-unpublish', onUnpublishClick);
})(window, document, Granite.$)
