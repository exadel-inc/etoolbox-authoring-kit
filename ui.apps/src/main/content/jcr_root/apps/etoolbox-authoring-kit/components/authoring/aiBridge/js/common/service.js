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

    const DATA_KEY_UNFILTERED_SETTINGS = 'eak-unfiltered-settings';

    ns.Assistant = ns.Assistant || {};
    Object.assign(ns.Assistant, {

        FACILITY_SETTING_SEPARATOR: '/',

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
            let $sourceField;
            if (sourceField instanceof jQuery) {
                $sourceField = sourceField;
            } else {
                const fieldName = sourceField + (sourceField.endsWith('/') ? SUFFIX_SETTINGS.replace(/^_+/, '') : SUFFIX_SETTINGS);
                $sourceField = $(`[name="${fieldName}"]`);
            }

            const unfilteredSettings = $sourceField.data(DATA_KEY_UNFILTERED_SETTINGS) || JSON.parse($sourceField.val() || '{}');
            if (!prefix) {
                return unfilteredSettings;
            }
            const filteredSettings = {};
            for (const key of Object.keys(unfilteredSettings)) {
                if (!key.includes(this.FACILITY_SETTING_SEPARATOR)) {
                    filteredSettings[key] = unfilteredSettings[key];
                } else if (key.startsWith(prefix + this.FACILITY_SETTING_SEPARATOR)) {
                    filteredSettings[key.split(this.FACILITY_SETTING_SEPARATOR)[1]] = unfilteredSettings[key];
                }
            }
            return filteredSettings;
        },

        storeSetting: function (sourceFieldName, key, value) {
            const settingsObject = {};
            settingsObject[key] = value;
            this.storeSettings(sourceFieldName, settingsObject);
        },

        storeSettings: function (sourceFieldName, settingsObject, transientSettings) {
            const fieldName = sourceFieldName + (sourceFieldName.endsWith('/') ? SUFFIX_SETTINGS.replace(/^_+/, '') : SUFFIX_SETTINGS);
            const $settingsField = $(`[name="${fieldName}"]`);
            const existingSettings = this.getSettings($settingsField);

            let filteredSettingsObject;
            if (!Array.isArray(transientSettings) || !transientSettings.length) {
                filteredSettingsObject = settingsObject;
            } else {
                filteredSettingsObject = {};
                Object.keys(settingsObject)
                    .filter((key) => !transientSettings.includes(key.split(ns.Assistant.FACILITY_SETTING_SEPARATOR)[1]))
                    .forEach((key) => filteredSettingsObject[key] = settingsObject[key]);
            }

            const allSettings = Object.assign({}, existingSettings, settingsObject);
            const filteredSettings = Object.assign({}, existingSettings, filteredSettingsObject);

            $settingsField.val(JSON.stringify(filteredSettings));
            $settingsField.data(DATA_KEY_UNFILTERED_SETTINGS, allSettings);
        }
    });

    async function loadFacilities() {
        return new Promise((resolve) => {
            fetch(FACILITIES_ENDPOINT)
                .then((res) => res.json())
                .then((facilitiesAndVendors) => {
                    const facilitiesMap = {};
                    (facilitiesAndVendors.vendors || []).forEach((item) => facilitiesMap[item.name] = item.logo);
                    if (Array.isArray(facilitiesAndVendors.facilities)) {
                        facilitiesAndVendors.facilities
                            .flatMap((facility) => Array.isArray(facility.variants) ? facility.variants : [facility])
                            .forEach((item) => item.vendorLogo = facilitiesMap[item.vendorName]);
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
