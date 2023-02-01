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
    ns.Ai = ns.Ai || {};

    ns.Ai.openRequestDialog = function (setup = {}) {
        if (!setup.payload) {
            return;
        }
        const $dialog = produceDialog(
            {
                id: 'ai-request',
                header: {
                    innerHTML: 'AI Bridge'
                },
                content: {
                    innerHTML: `
                        <vendors class="flex-block">
                            <coral-select class="coral-Form-field grow"></coral-select>
                            <button is="coral-button" variant="secondary" icon="gears"></button>
                        </vendors>
                        <notifications></notifications>
                        <coral-wait size="M"></coral-wait>
                        <options class="grow scrollable"></options>`
                },
                footer: {
                    innerHTML: '<button is="coral-button" variant="secondary" coral-close>Cancel</button>'
                }
            },
            function (e) {
                $dialog.get(0).abortController && $dialog.get(0).abortController.abort();
                runRequest($dialog, Object.assign(setup, { command: $(e.target).val() }));
            },
            setup.acceptDelegate
        );

        populateVendorsList($dialog, setup.variants);
        runRequest($dialog, Object.assign(setup, { command: setup.variants[0].id }));
    };

    function produceDialog(content,  changeCommandDelegate, acceptDelegate) {
        let $dialog = $(document).find('#' + content.id);
        if (!$dialog.length) {
            const newDialog = new Coral.Dialog().set(content);
            document.body.appendChild(newDialog);
            $dialog = $(newDialog);

            $dialog.on('coral-overlay:close', function () {
                $dialog.get(0).abortController && $dialog.get(0).abortController.abort();
            });
            $dialog.get(0).acceptDelegate = acceptDelegate;

            $dialog.find('vendors coral-select').on('change', changeCommandDelegate);

            $dialog.find('vendors button').on('click', function () {
                ns.Ai.openSettingsDialog();
            });

            $dialog.find('options').on('click', 'button', function () {
                const text = $(this).find('coral-list-item-content').text();
                text && $dialog.get(0).acceptDelegate && $dialog.get(0).acceptDelegate(text);
                $dialog.get(0).hide();
            });

        }
        $dialog.get(0).show();
        return $dialog;
    }

    function populateVendorsList($dialog, variants) {
        const $vendors = $dialog.find('vendors coral-select');
        $vendors.attr('disabled', variants.length <= 1);
        const vendorOptions = variants
            .map(variant => {
                const item = new Coral.Select.Item();
                item.content.textContent = variant.title;
                item.value = variant.id;
                return item;
            });
        Coral.commons.ready($vendors.get(0), function () {
            $vendors.get(0).items.clear();
            vendorOptions.forEach(item => $vendors.get(0).items.add(item));
        });
    }

    function populateOptionList($dialog, solution) {
        if (!solution) {
            throw new Error('Empty result received');
        }
        const messages = solution.messages;
        if (Array.isArray(messages) && messages[0]) {
            throw new Error(messages[0]);
        } else if (messages) {
            throw new Error(messages);
        }
        const optionsContent = $dialog.find('options');
        const $list = $('<coral-buttonlist></coral-buttonlist>');
        const options = solution.options || solution;
        if (Array.isArray(options)) {
            for (const option of options) {
                $(`<button is="coral-buttonlist-item" icon="textEdit" value="${option.text || option}">${option.text || option}</button>`).appendTo($list);
            }
        }
        optionsContent.empty();
        optionsContent.append($list);
    }

    function runRequest($dialog, setup) {
        $dialog.removeClass('is-error').addClass('is-loading');
        $dialog.find('notifications').empty();
        $dialog.find('options').empty();

        const command = setup.command || $dialog.find('vendors coral-select').val();
        const payload = setup.payload;

        let title = command.includes('.') ? command.split('.')[0] : command;
        title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();

        const truncatedText = payload.length <= 30 ? payload : payload.substring(0, 27) + '...';
        $dialog.find('coral-dialog-header').text(`${title} "${truncatedText}"`);

        const serviceLink = ns.Ai.getServiceUrl(Object.assign(setup, {command: command}));

        const abortController = new AbortController();
        $dialog[0].abortController = abortController;
        fetch(serviceLink, { signal: abortController.signal })
            .then(res => res.json())
            .then(json => populateOptionList($dialog, json))
            .catch(err => displayError($dialog, err))
            .finally(() => $dialog.removeClass('is-loading'));
    }

    function displayError($dialog, err) {
        if (err.toString().includes('AbortError')) {
            return;
        }
        $dialog.addClass('is-error').removeClass('is-loading');
        $dialog.find('notifications').append(`<coral-alert variant="warning"><coral-alert-content></coral-alert-content>${err}</coral-alert>`);
    }
})(window.eak = window.eak || {});
