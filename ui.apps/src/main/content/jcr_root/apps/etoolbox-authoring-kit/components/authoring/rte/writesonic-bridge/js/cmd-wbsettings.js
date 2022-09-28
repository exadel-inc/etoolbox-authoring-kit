'use strict';
(function (RTE, Class) {
    RTE.commands.WbSettingsDialogCommand = new Class({

        extend: RTE.commands.Command,

        toString: 'WbSettingsDialogCommand',

        isCommand: function (cmdStr) {
            return cmdStr === 'wbsettings';
        },

        execute: function () {
            this.produceDialog({
                id: 'writesonic-settings',
                header: {
                    innerHTML: 'Writesonic Settings'
                },
                content: {
                    innerHTML: `
                            <label id="label-num-options" class="coral-Form-fieldlabel">Number of options</label>
                            <coral-numberinput class="coral-Form-field" labelledby="label-num-options" value="5"></coral-numberinput>`
                },
                footer: {
                    innerHTML: '<button is="coral-button" variant="primary" coral-close>Ok</button>'
                }
            });
        },

        produceDialog: function (content, handlers = {}) {
            let dialog = document.getElementById(content.id);
            if (!dialog) {
                dialog = new Coral.Dialog().set(content);
                document.body.appendChild(dialog);
            }
            dialog.show();
            handlers.onShow && handlers.onShow(dialog);
        }
    });
    RTE.commands.CommandRegistry.register('wbsettings', RTE.commands.WbSettingsDialogCommand);
})(window.CUI.rte, window.Class);
