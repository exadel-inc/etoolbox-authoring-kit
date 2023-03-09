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

    const SELECTOR = '.coral3-Textfield[data-eak-plugins*="assistant"], textarea[data-eak-plugins*="assistant"]';

    $(document).on('coral-overlay:open', function (e) {
        const $dialog = $(e.target);
        $dialog.find(SELECTOR).each(async function () {
            const $input = $(this);
            $input.attr(ns.Assistant.ATTR_SOURCE, true);
            await ns.Assistant.initAssistantUi($input, { facilityPrefix: 'text.' });
        });
    });
})(Granite.$, window.eak = window.eak || {});
