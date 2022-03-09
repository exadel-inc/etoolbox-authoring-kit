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
(function ($document, author) {
    'use strict';
    const INSERT_ACTION = 'INSERT';
    const COMPONENTS_LIMIT = 1;

    let rootEditable;
    let rootPath;

    function templateInsertCondition(editableBefore) {
        let canInsert = true;
        const parentPath = editableBefore.getParentPath();
        // Check if we are trying to insert to the root component
        if (parentPath === rootPath) {
            canInsert = isRootChildrenInLimit();
        }
        return canInsert;
    }

    function isRootChildrenInLimit() {
        const children = author.editables.getChildren(rootEditable, false);
        return (children.length <= COMPONENTS_LIMIT);
    }

    function toggleInsertParsys() {
        const insertEditable = author.editables.find(rootPath + '/*')[0];
        insertEditable && insertEditable.overlay.setVisible(isRootChildrenInLimit());
    }

    $document.on('cq-layer-activated', function (ev) {
        if (ev.layer === 'Edit') {
            rootEditable = author.editables.getRoot();
            rootPath = rootEditable.path;

            const action = author.edit.EditableActions[INSERT_ACTION];
            const defaultInsertActionCondition = action.condition;
            action.condition = function (editableBefore, componentPath, componentGroup) {
                return templateInsertCondition(editableBefore) && defaultInsertActionCondition(editableBefore, componentPath, componentGroup);
            };

            // set initial visibility state
            toggleInsertParsys();

            rootEditable.afterChildInsert = function () {
                toggleInsertParsys();
            };

            rootEditable.afterChildDelete = function () {
                toggleInsertParsys();
            };

            $document.on('cq-overlays-repositioned', function () {
                toggleInsertParsys();
            });
        }
    });
}($(document), Granite.author));
