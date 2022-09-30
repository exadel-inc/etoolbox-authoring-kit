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

    ns.Writesonic.openSettingsDialog = function () {
        const engineOptions = ns.Writesonic.settings.engine.options.map(item => `<coral-select-item>${item}</coral-select-item>`).join('');
        const languageOptions = ns.Writesonic.settings.language.options.map(item => `<coral-select-item>${item}</coral-select-item>`).join('');
        const toneOptions = ns.Writesonic.settings.tone.options.map(item => `<coral-select-item>${item}</coral-select-item>`).join('');

        produceSettingsDialog({
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
        }, ns.Writesonic.settings);
    };

    function produceSettingsDialog(content, settings) {
        let $dialog = $('#' + content.id);
        if (!$dialog.length) {
            const newDialog = new Coral.Dialog().set(content);
            newDialog.backdrop = 'static';
            document.body.appendChild(newDialog);
            $dialog = $(newDialog);
        }
        $dialog.on('coral-overlay:open', () => {
            loadSettings($dialog, settings);
        });

        $dialog.on('click', 'button[variant="primary"]', () => {
            storeSettings($dialog, settings);
            $dialog[0].hide();
        });

        $dialog[0].show();
    }

    function loadSettings($dialog, settings) {
        for (const setting of Object.values(settings)) {
            $dialog.find(`[data-prop="${setting.propId}"]`).val(localStorage.getItem(setting.propId) || setting.defaultValue);
        }
    }

    function storeSettings($dialog, settings) {
        for (const setting of Object.values(settings)) {
            const value = $dialog.find(`[data-prop="${setting.propId}"]`).val();
            if (value) {
                localStorage.setItem(setting.propId, value);
            } else {
                localStorage.removeItem(setting.propId);
            }
        }
    }
})(window.eak = window.eak || {});
