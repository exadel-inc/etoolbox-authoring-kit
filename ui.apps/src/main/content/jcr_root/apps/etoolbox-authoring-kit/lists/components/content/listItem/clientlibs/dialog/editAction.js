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

(function ($document, author, ns) {
    const ACTION_NAME = 'LIST_ITEM_CONFIGURE';
    const CONFIG_SELECTOR = '.item-config';
    const DIALOG_PATH_ATTR = 'data-dialog-path';

    const LIST_ITEM_CONFIGURE = {
        icon: 'coral-Icon--wrench',
        text: ns.I18n.get('Open Dialog'),
        order: 'before COPY',
        isNonMulti: true,
        condition: function (editable) {
            return editable && editable.type === ns.EToolboxLists.WRAPPER_RES_TYPE;
        },
        handler: function (editable) {
            const config = editable.dom.find(CONFIG_SELECTOR);
            const itemPath = config.attr(DIALOG_PATH_ATTR);
            itemPath && ns.EToolboxLists.launchReferenceDialog(editable, itemPath);
        }
    };

    $document.off('cq-layer-activated.etoolbox-lists').on('cq-layer-activated.etoolbox-lists', function (event) {
        if (event.layer === 'Edit') {
            author.EditorFrame.editableToolbar.registerAction(ACTION_NAME, LIST_ITEM_CONFIGURE);
        }
    });

    $document.off('cq-interaction-fastdblclick.etoolbox-lists').on('cq-interaction-fastdblclick.etoolbox-lists', function (event) {
        if (LIST_ITEM_CONFIGURE.condition(event.editable)) {
            LIST_ITEM_CONFIGURE.handler(event.editable);

            // prevent default event handler
            event.stopImmediatePropagation();
            event.preventDefault();
        }
    });
})($(document), Granite.author, Granite);
