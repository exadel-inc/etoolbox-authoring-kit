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

const CLS_ERROR = 'is-error';
const CLS_LOADING = 'is-loading';

const SELECTOR_DISABLED_ON_ERROR = '.disabled-on-error';
const SELECTOR_DISABLED_ON_LOADING = '.disabled-on-loading';
const SELECTOR_NOTIFICATIONS = 'notifications';
const SELECTOR_WAIT = 'coral-wait';

(function (ns, editmode) {
    'use strict';

    Object.assign(ns.Assistant, {

        CLS_ASSISTANT_DIALOG: 'eak-assistant-dialog',
        DATA_KEY_ABORT_CONTROLLER: 'eak-assistant-abort-controller',

        setLoadingState: function ($dialog, status) {
            $dialog.removeClass(CLS_ERROR).addClass(CLS_LOADING).find(SELECTOR_DISABLED_ON_LOADING).attr('disabled', true);
            const $coralWait = $dialog.find(SELECTOR_WAIT);
            if (status) {
                $coralWait.html(`<span class="status">${status}</span>`);
            } else {
                $coralWait.empty();
            }
            return $dialog;
        },


        unsetLoadingState: function ($dialog) {
            $dialog.removeClass(CLS_LOADING).find(SELECTOR_DISABLED_ON_LOADING).removeAttr('disabled');
            $dialog.find(SELECTOR_WAIT).empty();
            return $dialog;
        },

        setErrorState: function ($dialog, err) {
            if (err.toString().includes('AbortError')) {
                return;
            }
            ns.Assistant.unsetLoadingState($dialog).addClass(CLS_ERROR);
            $dialog.find(SELECTOR_DISABLED_ON_ERROR).attr('disabled', true);
            $dialog.find(SELECTOR_NOTIFICATIONS)
                .empty()
                .append(`<coral-alert variant="warning"><coral-alert-content>${err}</coral-alert-content></coral-alert>`);
            return $dialog;

        },

        unsetErrorState: function ($dialog) {
            $dialog.removeClass(CLS_ERROR)
            $dialog.find(SELECTOR_DISABLED_ON_ERROR).removeAttr('disabled');
            $dialog.find(SELECTOR_NOTIFICATIONS).empty();
            return $dialog;
        },

        abortNetworkRequest: function ($dialog) {
            const abortController = $dialog.data(ns.Assistant.DATA_KEY_ABORT_CONTROLLER);
            abortController && abortController.abort();
        }
    });

    $(document)
        .on('coral-overlay:close', `.${ns.Assistant.CLS_ASSISTANT_DIALOG}`, handleDialogClose);

    function handleDialogClose() {
        const $this = $(this);
        const existingAbortController = $this.data(ns.Assistant.DATA_KEY_ABORT_CONTROLLER);
        existingAbortController && existingAbortController.abort();
    }

})(window.eak = window.eak || {}, window.Granite.author);
