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

    const SERVICE_ENDPOINT = '/apps/etoolbox-authoring-kit/assistant/exec?cmd={command}{settings}';
    const FACILITIES_ENDPOINT = '/apps/etoolbox-authoring-kit/assistant/facilities';
    const SUFFIX_SETTINGS = '_eakAssistantSettings';

    ns.Assistant = ns.Assistant || {};
    Object.assign(ns.Assistant, {
        getServiceLink: function (setup = {}) {
            const settingsPart = setup.settings
                ? Object.keys(setup.settings).map(key => '&' + key + '=' + encodeURIComponent(setup.settings[key])).join('')
                : '';
            return SERVICE_ENDPOINT
                .replace('{command}', setup.command)
                .replace('{settings}', settingsPart);
        },

        getFacilities: async function () {
            if (!this.facilities) {
                if (!this.facilitiesPromise) {
                    this.facilitiesPromise = loadFacilities();
                    this.facilitiesPromise.then((result) => {
                        this.facilities = result;
                        delete this.facilitiesPromise;
                    });
                }
                return this.facilitiesPromise;
            }
            return this.facilities;
        },

        getSettings: function (sourceField, prefix) {
            let fieldValue;
            if (sourceField instanceof jQuery) {
                fieldValue = sourceField.val();
            } else {
                const fieldName = sourceField + (sourceField.endsWith('/') ? SUFFIX_SETTINGS.replace(/^_+/, '') : SUFFIX_SETTINGS);
                fieldValue = $(`[name="${fieldName}"]`).val();
            }
            if (!fieldValue) {
                return {};
            }
            const parsedSettings = JSON.parse(fieldValue);
            if (!prefix) {
                return parsedSettings;
            }
            const filteredSettings = {};
            for (const key of Object.keys(parsedSettings)) {
                if (!key.includes('/')) {
                    filteredSettings[key] = parsedSettings[key];
                } else if (key.startsWith(prefix + '/')) {
                    filteredSettings[key.split('/')[1]] = parsedSettings[key];
                }
            }
            return filteredSettings;
        },

        storeSettings: function (sourceFieldName, settingsOrKey, value) {
            const fieldName = sourceFieldName + (sourceFieldName.endsWith('/') ? SUFFIX_SETTINGS.replace(/^_+/, '') : SUFFIX_SETTINGS);
            const $settingsField = $(`[name="${fieldName}"]`);
            const existingSettings = this.getSettings($settingsField);
            if (typeof settingsOrKey === 'string' || settingsOrKey instanceof String) {
                existingSettings[settingsOrKey] = value;
            } else if (typeof settingsOrKey === 'object' && settingsOrKey !== null) {
                Object.assign(existingSettings, settingsOrKey);
            }
            $settingsField.val(JSON.stringify(existingSettings));
        }
    });

    async function loadFacilities() {
        return new Promise((resolve) => {
            fetch(FACILITIES_ENDPOINT)
                .then((res) => res.json())
                .then((facilitiesAndVendors) => {
                    const vendorsMap = {};
                    (facilitiesAndVendors.vendors || []).forEach((item) => vendorsMap[item.name] = item.logo);
                    if (Array.isArray(facilitiesAndVendors.facilities)) {
                        facilitiesAndVendors.facilities
                            .flatMap((facility) => Array.isArray(facility.variants) ? facility.variants : [facility])
                            .forEach((item) => item.vendorLogo = vendorsMap[item.vendorName]);
                    }
                    resolve(facilitiesAndVendors.facilities)
                })
                .catch((e) => {
                    console.error('[Assistant] Could not retrieve facilities', e);
                    resolve([]);
                });
        })
    }
})(window.eak = window.eak || {});
