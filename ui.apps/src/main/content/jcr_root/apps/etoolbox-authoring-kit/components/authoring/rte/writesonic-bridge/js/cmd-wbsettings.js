'use strict';
(function (RTE, Class) {

    RTE.commands.WbSettingsDialogCommand = new Class({

        extend: RTE.commands.Command,

        toString: 'WbSettingsDialogCommand',

        isCommand: function (cmdStr) {
            return cmdStr === 'wbsettings';
        },

        execute: function (execDef) {
            const { engine, language, tone} = execDef.value;

            const engineOptions = engine.options.map(item => `<coral-select-item>${item}</coral-select-item>`).join('');
            const languageOptions = language.options.map(item => `<coral-select-item>${item}</coral-select-item>`).join('');
            const toneOptions = tone.options.map(item => `<coral-select-item>${item}</coral-select-item>`).join('');

            this.produceDialog({
                id: 'writesonic-settings',
                header: {
                    innerHTML: 'Writesonic Settings'
                },
                content: {
                    innerHTML: `
                            <label id="label-engine" class="coral-Form-fieldlabel">Engine</label>
                            <coral-select data-prop="eak.writesonic.engine" class="coral-Form-field" labelledby="label-engine">${engineOptions}</coral-select>
                            <label id="label-language" class="coral-Form-fieldlabel">Language</label>
                            <coral-select data-prop="eak.writesonic.language" class="coral-Form-field" labelledby="label-language">${languageOptions}</coral-select>
                            <label id="label-tone" class="coral-Form-fieldlabel">Tone of voice</label>
                            <coral-select data-prop="eak.writesonic.tone" class="coral-Form-field" labelledby="label-tone">${toneOptions}</coral-select>
                    `
                },
                footer: {
                    innerHTML: '<button is="coral-button" variant="primary">Ok</button><button is="coral-button" variant="secondary" coral-close>Cancel</button>'
                }
            }, execDef.value);
        },

        produceDialog: function (content, settings) {
            let $dialog = $('#' + content.id);
            if (!$dialog.length) {
                const newDialog = new Coral.Dialog().set(content);
                newDialog.backdrop = 'static';
                document.body.appendChild(newDialog);
                $dialog = $(newDialog);
            }
            $dialog.on('coral-overlay:open', () => {
                this.loadSettings($dialog, settings);
            })

            $dialog.on('click', 'button[variant="primary"]', () => {
                this.storeSettings($dialog, settings);
                $dialog[0].hide();
            });

            $dialog[0].show();
        },

        loadSettings: function ($dialog, settings) {
            for (const setting of Object.values(settings)) {
                $dialog.find(`[data-prop="${setting.propId}"]`).val(localStorage.getItem(setting.propId) || setting.defaultValue);
            }
        },

        storeSettings: function ($dialog, settings) {
            for (const setting of Object.values(settings)) {
                const value = $dialog.find(`[data-prop="${setting.propId}"]`).val();
                if (value) {
                    localStorage.setItem(setting.propId, value);
                } else {
                    localStorage.removeItem(setting.propId);
                }
            }
        }
    });
    RTE.commands.CommandRegistry.register('wbsettings', RTE.commands.WbSettingsDialogCommand);
})(window.CUI.rte, window.Class);
