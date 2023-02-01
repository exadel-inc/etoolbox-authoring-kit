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

    const SERVICE_ENDPOINT = '/apps/etoolbox-authoring-kit/ai/exec?cmd={command}&text={payload}{params}';
    const FACILITIES_ENDPOINT = '/apps/etoolbox-authoring-kit/ai/facilities';

    // const SERVICE_ENDPOINT = 'https://api.writesonic.com/v1/business/content/{command}?engine={engine}&language={language}';
    // const KEY_ENDPOINT = '/conf/etoolbox-authoring-kit/components/authoring/ai-bridge.json';

    // const DEFAULT_COMMAND_ID = 'content-rephrase';
    // const DEFAULT_PAYLOAD_NAME = 'content_to_rephrase';

    // const MENU_OPTIONS = [
    //     { id: 'sentence-expand', title: 'Expand', icon: 'textEdit', params: { payloadName: 'content_to_expand' } },
    //     { id: 'content-shorten', title: 'Shorten', icon: 'textEdit', params: { payloadName: 'content_to_shorten' } },
    //     { id: DEFAULT_COMMAND_ID, title: 'Rephrase', icon: 'textEdit', params: { payloadName: DEFAULT_PAYLOAD_NAME } },
    //     { id: 'settings', title: 'Settings', icon: 'gears' }
    // ];

    const PROPERTY_ENGINE = 'eak.ai.engine';
    const DEFAULT_ENGINE = 'economy';
    const ENGINE_OPTIONS = ['business', DEFAULT_ENGINE];

    const PROPERTY_LANGUAGE = 'eak.ai.language';
    const DEFAULT_LANGUAGE = 'en';
    const LANGUAGE_OPTIONS = [DEFAULT_LANGUAGE, 'nl', 'fr', 'de', 'it', 'pl', 'es', 'pt-pt', 'pt-br', 'ru', 'ja', 'zh', 'bg', 'cs', 'da', 'el', 'hu', 'lt', 'lv', 'ro', 'sk', 'sl', 'sv', 'fi', 'et'];

    const PROPERTY_TONE = 'eak.ai.tone';
    const DEFAULT_TONE = 'professional';
    const TONE_OPTIONS = ['excited', DEFAULT_TONE, 'funny', 'encouraging', 'dramatic', 'witty', 'sarcastic', 'engaging', 'creative'];

    // const PROPERTY_KEY = 'eak.ai.writesonic-key';

    const SETTINGS = {
        engine: {
            propId: PROPERTY_ENGINE,
            options: ENGINE_OPTIONS,
            defaultValue: DEFAULT_ENGINE
        },
        language: {
            propId: PROPERTY_LANGUAGE,
            options: LANGUAGE_OPTIONS,
            defaultValue: DEFAULT_LANGUAGE
        },
        tone: {
            propId: PROPERTY_TONE,
            options: TONE_OPTIONS,
            defaultValue: DEFAULT_TONE
        }
    };

    ns.Ai = ns.Ai || {};
    Object.assign(ns.Ai, {
        getMenuOptions: async function() {
            if (!this.menuOptions) {
                this.menuOptions = await getFacilities();
            }
            return this.menuOptions;
        },

        settings: SETTINGS,

/*
        defaultPayloadName: DEFAULT_PAYLOAD_NAME,

        getBasicOptions: async function () {
            const key = await getApiKey();
            return {
                engine: localStorage.getItem(PROPERTY_ENGINE) || DEFAULT_ENGINE,
                language: localStorage.getItem(PROPERTY_LANGUAGE) || DEFAULT_LANGUAGE,
                tone: localStorage.getItem(PROPERTY_TONE) || DEFAULT_TONE,
                key: key
            };
        },
*/

        getServiceUrl: function (options = {}) {
            const paramsPart = options.params
                ? Object.keys(options.params).map(k=> '&' + k + '=' + encodeURIComponent(options.params[k]))
                : '';
            return SERVICE_ENDPOINT
                .replace('{command}', options.command)
                .replace('{payload}', encodeURIComponent(options.payload))
                .replace('{params}', paramsPart);
        }
    });

    async function getFacilities() {
        return new Promise((resolve) => {
            fetch(FACILITIES_ENDPOINT)
                .then((res) => res.json())
                .then((facilities) => resolve(facilities))
                .catch(() => {
                    console.error('[AI Bridge] Could not retrieve menu options');
                    resolve([]);
                });
        })
    }

/*
    async function getApiKey() {
        return new Promise((resolve) => {
            const storedKey = sessionStorage.getItem(PROPERTY_KEY);
            if (storedKey && storedKey !== 'undefined') {
                resolve(sessionStorage.getItem(PROPERTY_KEY));
            } else {
                fetch(KEY_ENDPOINT)
                    .then((res) => res.json())
                    .then((json) => {
                        sessionStorage.setItem(PROPERTY_KEY, json['writesonic-key']);
                        resolve(json.json['writesonic-key']);
                    })
                    .catch(() => {
                        console.error('[AI Bridge] Could not retrieve an API key');
                        resolve(null);
                    });
            }
        });
    }
*/
})(window.eak = window.eak || {});
