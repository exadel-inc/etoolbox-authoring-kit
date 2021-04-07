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
    const CONFIG_SELECTOR = '.item-config';
    const DIALOG_PATH_ATTR = 'data-dialog-path';

    const actionConfig = {
        icon: 'coral-Icon--wrench',
        text: ns.I18n.get('Open Dialog'),
        order: 'before COPY',
        isNonMulti: true,
        condition: function (editable) {
            return editable.type === ns.EToolboxLists.WRAPPER_RES_TYPE;
        },
        handler: function (editable) {
            const config = editable.dom.find(CONFIG_SELECTOR);
            const itemPath = config.attr(DIALOG_PATH_ATTR);
            itemPath && ns.EToolboxLists.launchReferenceDialog(editable, itemPath);
        }
    };

    $document.on('cq-layer-activated', function (ev) {
        if (ev.layer === 'Edit') {
            author.EditorFrame.editableToolbar.registerAction('OPEN_ITEM_DIALOG', actionConfig);
        }
    });
})($(document), Granite.author, Granite);
