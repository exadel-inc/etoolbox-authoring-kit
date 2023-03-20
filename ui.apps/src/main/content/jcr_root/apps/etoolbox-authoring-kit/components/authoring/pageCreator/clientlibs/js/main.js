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

    const SELECTOR_ACTION_BAR = '.editor-GlobalBar coral-actionbar';
    const SELECTOR_BUTTON = '.js-editor-PageCreator';

    const $actionBar = $(SELECTOR_ACTION_BAR);
    Coral.commons.ready($actionBar[0], function () {
        $actionBar.find(SELECTOR_BUTTON).off('click').on('click', handlePageCreatorClick);
    });

    function handlePageCreatorClick() {
/*
        const registry = $(window).adaptTo('foundation-registry');
        const actions = registry.get('foundation.collection.action.action');
        for (let i = actions.length - 1; i >= 0; i--) {
            const action = actions[i];
            if (action.name === 'foundation.dialog') {
                action.handler.call(
                    this,
                    'foundation-dialog',
                    this,
                    { data: { src: '/mnt/overlay/etoolbox-authoring-kit/components/authoring/pageCreator/content/dialog.html' } },
                    $('#ContentWrapper').get(0),
                    []);
                return;
            }
        }
*/
        ns.Assistant.openPageCreatorDialog();
    }

})(Granite.$, window.eak = window.eak || {});
