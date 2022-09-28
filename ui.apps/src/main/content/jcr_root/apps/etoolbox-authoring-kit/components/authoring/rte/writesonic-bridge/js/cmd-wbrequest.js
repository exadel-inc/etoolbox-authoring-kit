'use strict';
(function (RTE, Class) {
    const SERVICE_ENDPOINT = 'https://api.writesonic.com/v1/business/content/{command}?engine=economy&language=en';

    RTE.commands.WbRequestDialogCommand = new Class({

        extend: RTE.commands.Command,

        toString: 'WbRequestDialogCommand',

        isCommand: function (cmdStr) {
            return cmdStr === 'wbrequest';
        },

        execute: function (execDef) {
            const self = this;
            const { command, params, key } = execDef.value;
            const selectedText = window.getSelection().toString() || execDef.editContext.root.innerText || '';
            if (!selectedText.length) {
                return;
            }

            const onShowHandler = function (dialog) {
                if (!key) {
                    return self.displayDialogError(dialog, 'API key is missing');
                }
                const $dialog = $(dialog);
                const title = command
                    .split('-')
                    .map(chunk => chunk.substring(0, 1).toUpperCase() + chunk.substring(1).toLowerCase())
                    .join(' ');
                const truncatedText = selectedText.length <= 30 ? selectedText : selectedText.substring(0, 27) + '...';
                $dialog.find('coral-dialog-header').text(`${title} "${truncatedText}"`);

                const payloadName = params && params.payloadName ? params.payloadName : 'content_to_rephrase';
                const body = { tone_of_voice: 'excited' };
                body[payloadName] = selectedText;

                fetch(SERVICE_ENDPOINT.replace('{command}', command), {
                    headers: {
                        'content-type': 'application/json',
                        accept: 'application/json',
                        'X-API-KEY': key
                    },
                    body: JSON.stringify(body),
                    method: 'POST'
                })
                    .then(res => res.json())
                    .then(json => self.populateDialogOptionList(dialog, json))
                    .catch(err => self.displayDialogError(dialog, err))
                    .finally(() => $dialog.removeClass('is-loading'));
            };

            const onAcceptHandler = function (dialog, result) {
                dialog.hide();
                if (!result) {
                    return;
                }
                if (!window.getSelection().toString().length) {
                    execDef.component.relayCmd('clear');
                }
                execDef.component.relayCmd('inserthtml', result);
            };

            this.produceDialog({
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
            }, {
                onShow: onShowHandler,
                onAccept: onAcceptHandler
            });
        },

        produceDialog: function (content, handlers = {}) {
            let dialog = document.getElementById(content.id);
            if (!dialog) {
                dialog = new Coral.Dialog().set(content);
                document.body.appendChild(dialog);
                $(dialog).find('options').on('click', 'button', function () {
                    const text = $(this).find('coral-list-item-content').text();
                    handlers.onAccept && handlers.onAccept(dialog, text);
                });
            }

            const $dialog = $(dialog);
            $dialog.removeClass('is-error').addClass('is-loading');
            $dialog.find('notifications').empty();
            $dialog.find('options').empty();

            dialog.show();
            handlers.onShow && handlers.onShow(dialog);
        },

        populateDialogOptionList: function (dialog, options) {
            if (!options) {
                throw new Error('Empty result received');
            } else if (Array.isArray(options.detail) && options.detail[0]) {
                throw new Error(options.detail[0].msg || options.detail);
            }
            const $content = $(dialog).find('options');
            const $list = $('<coral-buttonlist></coral-buttonlist>');
            for (const option of options) {
                $('<button></button>')
                    .attr('is', 'coral-buttonlist-item')
                    .attr('icon', 'textEdit')
                    .attr('value', option.text)
                    .text(option.text)
                    .appendTo($list);
            }
            $content.empty();
            $content.append($list);
        },

        displayDialogError: function (dialog, err) {
            const $dialog = $(dialog);
            $dialog.addClass('is-error').removeClass('is-loading');
            $dialog.find('notifications').append(`<coral-alert variant="warning"><coral-alert-content></coral-alert-content>${err}</coral-alert>`);
        }
    });
    RTE.commands.CommandRegistry.register('wbrequest', RTE.commands.WbRequestDialogCommand);
})(window.CUI.rte, window.Class);
