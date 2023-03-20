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

    const DATA_KEY_SETTINGS_DELEGATE = 'eak-assistant-settings-delegate';
    const DATA_KEY_SETTINGS_UI_OPTIONS = 'eak-assistant-settings-ui-options';

    const CLS_SETTINGS_MEMBER = 'assistant-settings';
    const CLS_SETTINGS_FIELD = 'eak-settings-field';

    ns.Assistant = ns.Assistant || {};

    ns.Assistant.openSettingsUi = async function ($callerDialog, acceptDelegate, options = {}) {
        const setup = $callerDialog.data(ns.Assistant.DATA_KEY_SETUP);

        const currentFacilityId = setup.command || setup.variants[0].id;
        const currentFacilitySettings = await getCurrentFacilitySettings(currentFacilityId);
        if (!currentFacilitySettings) {
            return;
        }

        const transientSettings = [].concat(options.doNotSave);
        let dialogContent = '';
        for (const setting of currentFacilitySettings) {
            if (options.display && !options.display.includes(setting.id)) {
                continue;
            }
            if (setting.persistence === 'transient' && !transientSettings.includes(setting.id)) {
                transientSettings.push(setting.id);
            }
            const settingName = currentFacilityId + ns.Assistant.FACILITY_SETTING_SEPARATOR + setting.id;
            const settingId = settingName.replace(/[.\/]/g, '-');
            dialogContent += `<label id="label-${settingId}" class="coral-Form-fieldlabel">${setting.title}</label>`;
            if (setting.options) {
                const optionsContent = setting.options.map(option => `<coral-select-item value="${option.id}" ${option.id === setting.defaultValue ? 'selected' : ''}>${option.title}</coral-select-item>`).join('');
                dialogContent += `<coral-select id="${settingId}" name="${settingName}" class="coral-Form-field ${CLS_SETTINGS_FIELD}" labelledby="label-${settingId}">${optionsContent}</coral-select>`;
            } else if (setting.type === 'integer' || setting.type === 'double') {
                dialogContent += `<coral-numberinput id="${settingId}" name="${settingName}" class="coral-Form-field ${CLS_SETTINGS_FIELD}" step="${setting.type === 'INTEGER' ? '1' : 'any'}" ${setting.minValue ? 'min="' + setting.minValue + '"' : ''} ${setting.maxValue ? 'max="' + setting.maxValue + '"' : ''} value="${setting.defaultValue}" labelledby="label-${settingId}"></coral-numberinput>`
            } else {
                dialogContent += `<input is="coral-textfield" id="${settingId}" name="${settingName}" value="${setting.defaultValue}" class="coral-Form-field ${CLS_SETTINGS_FIELD}" labelledby="label-${settingId}"">`;
            }
        }
        produceSettingsContent(
            $callerDialog,
            {
                header: {
                    innerHTML: `Settings for "${getSourceFieldLabel(setup.sourceField)}"`
                },
                content: {
                    innerHTML: dialogContent
                },
                footer: {
                    innerHTML: '<button is="coral-button" variant="primary">OK</button><button is="coral-button" variant="secondary">Cancel</button>'
                }
            },
            acceptDelegate);

        $callerDialog.data(
            DATA_KEY_SETTINGS_UI_OPTIONS,
            Object.assign(options, { sourceField: setup.sourceField, doNotSave: transientSettings }));
        $callerDialog.data(
            DATA_KEY_SETTINGS_DELEGATE,
            acceptDelegate);
        $callerDialog.attr(ns.Assistant.ATTR_ASSISTANT_MODE, 'settings');
        loadSettings($callerDialog);
    };

    async function getCurrentFacilitySettings(facilityVariantId) {
        const facilities = await ns.Assistant.getFacilities();
        const selectedVariant = facilities.flatMap(f => f.variants || f).filter((variant) => variant.id === facilityVariantId)[0];
        return selectedVariant?.settings;
    }

    function getSourceFieldLabel(field) {
        const $field = $(`[name="${field}"]`);
        const $fieldLabel = $field.closest('.coral-Form-fieldwrapper').find('label');
        return $fieldLabel.text() || field.replace(/(^\w+|\w+$)/, '');
    }

    function produceSettingsContent($callerDialog, content) {
        let $settingsContentElements = $callerDialog.find(`.${CLS_SETTINGS_MEMBER}`);

        if ($settingsContentElements.length) {
            $settingsContentElements.get(0).innerHTML = content.header.innerHTML;
            $settingsContentElements.get(1).innerHTML = content.content.innerHTML;
            return;
        }

        const outerDialog = new Coral.Dialog().set(content);
        $settingsContentElements = $(outerDialog).find('coral-dialog-header,coral-dialog-content,coral-dialog-footer');
        $settingsContentElements.addClass(`${CLS_SETTINGS_MEMBER}`);
        const settingsHeader = $settingsContentElements.get(0);
        const settingsContent = $settingsContentElements.get(1);
        const settingsFooter = $settingsContentElements.get(2);
        $callerDialog.find('coral-dialog-header').parent().append(settingsHeader);
        $callerDialog.find('.coral3-Dialog-wrapper').append(settingsContent).append(settingsFooter);
        $callerDialog.on('keypress', `.${CLS_SETTINGS_FIELD}`, handleKeyPress);

        $(settingsFooter).find('button[variant="primary"]').on('click', handleAcceptButtonClick);
        $(settingsFooter).find('button[variant="secondary"]').on('click', handleCloseButtonClick);
    }

    function loadSettings($dialog) {
        const sourceFieldName = $dialog.data(DATA_KEY_SETTINGS_UI_OPTIONS).sourceField;
        const settings = ns.Assistant.getSettings(sourceFieldName);
        for (const setting of Object.keys(settings)) {
            const $settingsField =  $dialog.find(`[name="${setting}"]`);
            if (!$settingsField.length) {
                continue;
            }
            Coral.commons.ready($settingsField.get(0), function () {
                $settingsField.val(settings[setting]);
            });
        }
    }

    function collectSettings($dialog) {
        const settingsObject = {};
        $dialog.find(`.${CLS_SETTINGS_FIELD}[name]`).each(function () {
            const $field = $(this);
            const name = $field.attr('name');
            const value = $field.val();
            if (name && value) {
                settingsObject[name] = value;
            }
        });
        return settingsObject;
    }

    function storeSettings($dialog, settingsObject, transientSettings) {
        const sourceFieldName = $dialog.data(DATA_KEY_SETTINGS_UI_OPTIONS).sourceField;
        if (!sourceFieldName) {
            return;
        }
        ns.Assistant.storeSettings(sourceFieldName, settingsObject, transientSettings);
    }

    function stripPrefixes(settingsObject) {
        const result = {};
        for (const key of Object.keys(settingsObject)) {
            const strippedKey = key.includes(ns.Assistant.FACILITY_SETTING_SEPARATOR)
                ? key.split(ns.Assistant.FACILITY_SETTING_SEPARATOR)[1]
                : key;
            result[strippedKey] = settingsObject[key];
        }
        return result;
    }

    function handleKeyPress (e) {
        if (e.key !== 'Enter') {
            return;
        }
        $(e.target)
            .closest('coral-dialog')
            .find(`coral-dialog-footer.${CLS_SETTINGS_MEMBER} button[variant="primary"]`)
            .trigger('click');
    }

    function handleAcceptButtonClick () {
        const $dialog = $(this).closest('coral-dialog');
        const settingsObject = collectSettings($dialog);
        storeSettings($dialog, settingsObject, $dialog.data(DATA_KEY_SETTINGS_UI_OPTIONS).doNotSave);
        $dialog.attr(ns.Assistant.ATTR_ASSISTANT_MODE, 'options');
        $dialog.data(DATA_KEY_SETTINGS_DELEGATE)
        && $dialog.data(DATA_KEY_SETTINGS_DELEGATE)(stripPrefixes(settingsObject));
    }

    function handleCloseButtonClick() {
        const $dialog = $(this).closest('coral-dialog');
        const settingsUiOptions = $dialog.data(DATA_KEY_SETTINGS_UI_OPTIONS);
        if (settingsUiOptions.closesParent) {
            $dialog[0].hide();
        } else {
            $dialog.attr(ns.Assistant.ATTR_ASSISTANT_MODE, 'options');
        }
    }

})(window.eak = window.eak || {});
