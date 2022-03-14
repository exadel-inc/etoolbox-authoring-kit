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

/**
 *  A simple clientlib that disables insert, drag/drop and copy/paste to an editable more components than defined
 *  in 'childrenLimit' property
 */
(function ($document, author) {
    'use strict';
    const INSERT_ACTION = 'INSERT';
    const LIMIT_PROPERTY = 'childrenLimit';

    /**
     * @returns the value of the given property defined in the policy
     */
    function findPropertyFromPolicy(editable, propertyName) {
        const cell = author.util.resolveProperty(author.pageDesign, editable.config.policyPath);
        return cell && cell[propertyName] ? cell[propertyName] : null;
    }

    /**
     * @see /libs/cq/gui/components/authoring/editors/clientlibs/core/js/storage/components.js _findAllowedComponentsFromDesign
     * @returns the value of the given property from design object
     */
    function findPropertyFromDesign(editable, propertyName) {
        const cellSearchPaths = editable.config.cellSearchPath;

        if (cellSearchPaths) {
            for (let i = 0; i < cellSearchPaths.length; i++) {
                const cell = author.util.resolveProperty(author.pageDesign, cellSearchPaths[i]);

                if (cell && cell[propertyName]) {
                    return cell[propertyName];
                }
            }
        }

        return null;
    }

    /**
     * @returns the value of the given property of an editable from policy or design configuration
     */
    function findPropertyFromConfig(editable, propertyName) {
        if (editable && editable.config) {
            if (editable.config.policyPath) {
                return findPropertyFromPolicy(editable, propertyName);
            } else {
                return findPropertyFromDesign(editable, propertyName);
            }
        }
        return null;
    }

    /**
     * Checks if editable contains equal or less children than defined in 'childrenLimit' property
     */
    function isChildrenInLimit(editable) {
        const limitCfg = findPropertyFromConfig(editable, LIMIT_PROPERTY);
        const limit = limitCfg === null ? Number.POSITIVE_INFINITY : +limitCfg;
        const children = author.editables.getChildren(editable, false);
        return (children.length <= limit);
    }

    /**
     * Show/hide all editables' insert parsys depending on {@link isChildrenInLimit} function
     */
    function toggleInsertParsys() {
        author.editables.forEach((editable) => {
            const insertEditable = author.editables.find(editable.path + '/*')[0];
            insertEditable && insertEditable.overlay.setVisible(isChildrenInLimit(editable));
        });
    }

    const toggleInsertParsysDebounced = $.debounce(100, toggleInsertParsys);

    $document.on('cq-layer-activated', function (ev) {
        if (ev.layer === 'Edit') {
            const action = author.edit.EditableActions[INSERT_ACTION];
            const defaultInsertActionCondition = action.condition;

            // override of the original action condition by adding children amount check
            action.condition = function (editableBefore, componentPath, componentGroup) {
                return isChildrenInLimit(editableBefore) && defaultInsertActionCondition(editableBefore, componentPath, componentGroup);
            };

            // set initial visibility state
            toggleInsertParsysDebounced();

            // track editables and overlays updates to hide insert parsys
            $document
                .off('cq-editables-updated.limited-parsys cq-overlays-repositioned.limited-parsys')
                .on('cq-editables-updated.limited-parsys cq-overlays-repositioned.limited-parsys', toggleInsertParsysDebounced);
        }
    });
}($(document), Granite.author));
