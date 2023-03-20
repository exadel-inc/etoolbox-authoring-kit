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
(function ($, ns) {
    'use strict';

    const SELECTOR = 'coral-fileupload[data-eak-plugins*="assistant"]';
    const CLS_PROMPT = 'eak-assistant-prompt';

    $(document).on('coral-overlay:open', function (e) {
        const $dialog = $(e.target);
        $dialog.find(SELECTOR).each(async function () {
            const $imageUpload = $(this);
            const imageUploadName = $imageUpload.attr('name');
            $imageUpload.attr(ns.Assistant.ATTR_TARGET, true);
            $imageUpload.attr(ns.Assistant.ATTR_ACCEPTING_MESSAGE, 'Retrieving image');
            $imageUpload.get(0).acceptDelegate = async function (value) {
                const retrievedImage = await retrieveImage($imageUpload, value);
                const assetSelectedEvent = $.Event('assetselected');
                assetSelectedEvent.path = retrievedImage.path;
                assetSelectedEvent.mimetype = retrievedImage.type;
                assetSelectedEvent.thumbnail = $(`<img src="${retrievedImage.path}?cq_ck=${Date.now()}" alt="${retrievedImage.path}">`);
                $imageUpload.trigger(assetSelectedEvent);
            }

            const $promptWrapper = $(`<div class="eak-assistant-prompt-wrapper"></div>`);
            const textField = new Coral.Textfield();
            textField.set({
                placeholder: 'Image description to use with the generator service',
                value: ns.Assistant.getSettings(imageUploadName)['prompt'] || ''
            });
            const $textField = $(textField)
                .addClass(`${CLS_PROMPT} coral-Form-field`)
                .attr(ns.Assistant.ATTR_SOURCE, true)
                .attr(ns.Assistant.ATTR_SOURCE_SETTING_REF, `${imageUploadName}@prompt`);
            $promptWrapper.append($textField);

            const $imageUploadWrapper = $imageUpload.wrap('<div class="full-width"/>').parent();
            $imageUploadWrapper.append($promptWrapper);

            await ns.Assistant.initAssistantUi($imageUploadWrapper, { facilityPrefix: 'image.', buttonTarget: $promptWrapper });
        });
    });

    async function retrieveImage($imageUpload, src) {
        const action = $imageUpload.attr('action');
        if (!action) {
            return;
        }

        if (!/^\w+:\/\//.test(src)) {
            return Promise.resolve({ path: src, type: 'image' });
        }

        const fieldName = ($imageUpload.attr('name') || '').replace(/(^\.\/|\/+$)/g, '') || 'image';
        const target = `${action}/${fieldName}`;
        const serviceLink = ns.Assistant.getServiceLink({
            command: 'image.util.import',
            settings: {
                from: src,
                to: target
            }
        })
        return new Promise((resolve, reject) => {
            fetch(serviceLink, { method: 'POST' })
                .then((response) => response.json())
                .then((json) => resolve({ path: json.path, type: json.type }))
                .catch((e) => reject(e));

        })
    }

})(Granite.$, window.eak = window.eak || {});
