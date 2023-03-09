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
    ns.Assistant = ns.Assistant || {};

    ns.Assistant.openSettingsDialog = async function ($callerDialog, acceptDelegate) {
        const $vendors = $callerDialog.find('vendors coral-select');
        if (!$vendors.length) {
            return;
        }

        const facilityVariantId = $vendors.val();
        const facilities = await ns.Assistant.getFacilities();
        let selectedVariant = facilities.flatMap(f => f.variants || f).filter(variant => variant.id === facilityVariantId);
        selectedVariant = selectedVariant.length ? selectedVariant[0] : undefined;
        if (!selectedVariant || !selectedVariant.settings) {
            return;
        }

        const sourceField = ($callerDialog.data(ns.Assistant.DATA_KEY_SETUP) || {}).sourceField;
        const sourceFieldLabel = getSourceFieldLabel(sourceField);

        let dialogContent = '';
        for (const setting of selectedVariant.settings) {
            const settingName = facilityVariantId + '/' + setting.id;
            const settingId = settingName.replace(/[.\/]/g, '-');
            dialogContent += `<label id="label-${settingId}" class="coral-Form-fieldlabel">${setting.title}</label>`;
            if (setting.options) {
                const optionsContent = setting.options.map(option => `<coral-select-item value="${option.id}" ${option.id === setting.defaultValue ? 'selected' : ''}>${option.title}</coral-select-item>`).join('');
                dialogContent += `<coral-select id="${settingId}" name="${settingName}" class="coral-Form-field" labelledby="label-${settingId}">${optionsContent}</coral-select>`;
            } else if (setting.type === 'INTEGER' || setting.type === 'DOUBLE') {
                dialogContent += `<coral-numberinput id="${settingId}" name="${settingName}" class="coral-Form-field" step="${setting.type === 'INTEGER' ? '1' : 'any'}" ${setting.minValue ? 'min="' + setting.minValue + '"' : ''} ${setting.maxValue ? 'max="' + setting.maxValue + '"' : ''} value="${setting.defaultValue}" labelledby="label-${settingId}"></coral-numberinput>`
            } else {
                dialogContent += `<input is="coral-textfield" id="${settingId}" name="${settingName}" value="${setting.defaultValue} labelledby="label-${settingId}"">`;
            }
        }
        const $dialog = produceSettingsDialog({
                id: 'assistant-settings',
                header: {
                    innerHTML: `Settings for "${sourceFieldLabel}"`
                },
                content: {
                    innerHTML: dialogContent
                },
                footer: {
                    innerHTML: '<button is="coral-button" variant="primary">Ok</button><button is="coral-button" variant="secondary" coral-close>Cancel</button>'
                }
            },
            acceptDelegate);

        $dialog.attr(ns.Assistant.ATTR_SOURCE_REF, sourceField);
        loadSettings($dialog);
        $dialog.get(0).show();
    };

    function getSourceFieldLabel(field) {
        const $field = $(`[name="${field}"]`);
        const $fieldLabel = $field.closest('.coral-Form-fieldwrapper').find('label');
        return $fieldLabel.text() || field.replace(/(^\w+|\w+$)/, '');
    }

    function produceSettingsDialog(content, acceptDelegate) {
        let $dialog = $('#' + content.id);
        if (!$dialog.length) {
            const dialog = new Coral.Dialog().set(content);
            dialog.backdrop = 'static';
            dialog.acceptDelegate = acceptDelegate;
            document.body.appendChild(dialog);

            $dialog = $(dialog);
            $dialog.on('click', 'button[variant="primary"]', () => {
                storeSettings($dialog);
                $dialog.get(0).acceptDelegate && $dialog.get(0).acceptDelegate();
                $dialog.get(0).hide();
            });
        } else {
            $dialog.find('coral-dialog-header').html(content.header.innerHTML);
            $dialog.find('coral-dialog-content').html(content.content.innerHTML);
        }
        return $dialog;
    }

    function loadSettings($dialog) {
        const sourceField = $dialog.attr(ns.Assistant.ATTR_SOURCE_REF);
        const settings = ns.Assistant.getSettings(sourceField);
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

    function storeSettings($dialog) {
        const sourceFieldName = $dialog.attr(ns.Assistant.ATTR_SOURCE_REF);
        if (!sourceFieldName) {
            return;
        }
        const newSettings = {};
        $dialog.find('[name]').each(function () {
            const $field = $(this);
            const name = $field.attr('name');
            const value = $field.val();
            if (name && value) {
                newSettings[name] = value;
            }
        });
        ns.Assistant.storeSettings(sourceFieldName, newSettings);
    }
})(window.eak = window.eak || {});
