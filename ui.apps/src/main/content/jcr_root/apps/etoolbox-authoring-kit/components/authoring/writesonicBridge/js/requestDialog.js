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
(function (ns) {
    'use strict';
    ns.Writesonic = ns.Writesonic || {};

    ns.Writesonic.openRequestDialog = function (options = {}) {
        if (!options.payload) {
            return;
        }
        const $dialog = produceDialog({
            id: 'writesonic-request',
            header: {
                innerHTML: 'Writesonic Bridge'
            },
            content: {
                innerHTML: '<notifications></notifications><coral-wait size="M"></coral-wait><options></options>'
            },
            footer: {
                innerHTML: '<button is="coral-button" variant="secondary" coral-close>Cancel</button>'
            }
        }, options.acceptDelegate);
        runRequest($dialog, options);
    };

    function produceDialog(content, acceptDelegate) {
        let $dialog = $(document).find('#' + content.id);
        if (!$dialog.length) {
            const newDialog = new Coral.Dialog().set(content);
            document.body.appendChild(newDialog);
            $dialog = $(newDialog);
            $dialog.find('options').on('click', 'button', function () {
                const text = $(this).find('coral-list-item-content').text();
                text && $dialog[0].acceptDelegate && $dialog[0].acceptDelegate(text);
                $dialog[0].hide();
            });
        }

        $dialog.removeClass('is-error').addClass('is-loading');
        $dialog.find('notifications').empty();
        $dialog.find('options').empty();
        $dialog[0].acceptDelegate = acceptDelegate;

        $dialog[0].show();
        return $dialog;
    }

    function runRequest($dialog, options) {
        const { key, command, params, tone, payload } = options;
        if (!key) {
            return displayError($dialog, 'API key is missing');
        }
        const title = command
            .split('-')
            .map(chunk => chunk.substring(0, 1).toUpperCase() + chunk.substring(1).toLowerCase())
            .join(' ');
        const truncatedText = payload.length <= 30 ? payload : payload.substring(0, 27) + '...';
        $dialog.find('coral-dialog-header').text(`${title} "${truncatedText}"`);

        const effectiveEndpoint = ns.Writesonic.getEndpoint(options);

        const payloadName = params && params.payloadName ? params.payloadName : ns.Writesonic.defaultPayloadName;
        const body = { tone_of_voice: tone };
        body[payloadName] = payload;

        fetch(effectiveEndpoint, {
            headers: {
                'content-type': 'application/json',
                accept: 'application/json',
                'X-API-KEY': key
            },
            body: JSON.stringify(body),
            method: 'POST'
        })
            .then(res => res.json())
            .then(json => populateOptionList($dialog, json))
            .catch(err => displayError($dialog, err))
            .finally(() => $dialog.removeClass('is-loading'));
    }

    function populateOptionList($dialog, options) {
        if (!options) {
            throw new Error('Empty result received');
        } else if (Array.isArray(options.detail) && options.detail[0]) {
            throw new Error(options.detail[0].msg || options.detail);
        } else if (options.detail) {
            throw new Error(options.detail);
        }
        const $content = $dialog.find('options');
        const $list = $('<coral-buttonlist></coral-buttonlist>');
        for (const option of options) {
            $(`<button is="coral-buttonlist-item" icon="textEdit" value="${option.text}">${option.text}</button>`).appendTo($list);
        }
        $content.empty();
        $content.append($list);
    }

    function displayError($dialog, err) {
        $dialog.addClass('is-error').removeClass('is-loading');
        $dialog.find('notifications').append(`<coral-alert variant="warning"><coral-alert-content></coral-alert-content>${err}</coral-alert>`);
    }
})(window.eak = window.eak || {});
