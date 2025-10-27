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
(function (document, $, ns) {
    'use strict';

    /**
     * Handles the "foundation-contentloaded" event
     */
    function onContentLoaded() {
        const list = document.getElementById('config-list');
        Coral.commons.ready(list, function () {
            const extras = document.getElementById('config-list-extras');
            list.innerHTML += extras.content.querySelector('div').innerHTML;
            Coral.commons.nextFrame(function () {
                extras.remove();
            });
        });
    }

    $(document).off('.eak').one('foundation-contentloaded.eak', onContentLoaded);
})(document, Granite.$);
