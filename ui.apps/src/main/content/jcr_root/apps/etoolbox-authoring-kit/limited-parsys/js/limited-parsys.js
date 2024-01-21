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
(function ($, author) {
    'use strict';

    const $document = $(document);

    const INSERT_ACTION = 'INSERT';
    const LIMIT_RESOLVER_NAME = 'resolvemaxlimit';
    const LIMIT_RESOLVER_PROPERTY = 'eak-children-limit';

    /** @returns the value of the given property defined in the policy */
    function findPropertyFromPolicy(editable, propertyName) {
        const cell = author.util.resolveProperty(author.pageDesign, editable.config.policyPath);
        return cell && cell[propertyName] ? cell[propertyName] : null;
    }

    /**
     * @see /libs/cq/gui/components/authoring/editors/clientlibs/core/js/storage/components.js _findAllowedComponentsFromDesign
     * @returns the value of the given property from design object
     */
    function findPropertyFromDesign(editable, propertyName) {
        const cellSearchPaths = editable.config.cellSearchPath || [];
        for (let i = 0; i < cellSearchPaths.length; i++) {
            const cell = author.util.resolveProperty(author.pageDesign, cellSearchPaths[i]);
            if (cell && cell[propertyName]) return cell[propertyName];
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

    function getChildrenLimit(editable) {
        if (typeof editable.config.editConfig.listeners[LIMIT_RESOLVER_NAME] === 'function') {
            try {
                const limit = editable.config.editConfig.listeners[LIMIT_RESOLVER_NAME].call(editable);
                if (typeof limit === 'number') return limit;
            } catch (e) {
                console.error('Error while executing resolvemaxlimit listener for editable: ', e);
            }
        }
        if (editable.type.indexOf('parsys') === -1) return Number.POSITIVE_INFINITY;
        const limitCfg = findPropertyFromConfig(editable, LIMIT_RESOLVER_PROPERTY);
        return limitCfg === null ? Number.POSITIVE_INFINITY : +limitCfg;
    }

    function getChildrenCount(editable) {
        if (!editable.dom) return 0;
        const children = editable.dom.children(':not(cq, .par, .newpar, .iparys_inherited)');
        return children ? children.length : 0;
    }

    /**
     * Checks if editable contains equal or less children than defined in 'childrenLimit' property
     */
    function isChildrenLimitReached(editable) {
        const limit = getChildrenLimit(editable);
        const size = getChildrenCount(editable);
        return size >= limit;
    }

    /**
     * Show/hide all editables' insert parsys depending on {@link isChildrenLimitReached} function
     */
    function toggleInsertParsys() {
        const zones = author.editables.filter((editable) => editable && editable.type.endsWith('newpar'));
        for (const zone of zones) {
            const parsys = author.editables.getParent(zone);
            const isBlocked = isChildrenLimitReached(parsys);
            zone.overlay && zone.overlay.setVisible(!isBlocked);
            zone.dom && zone.dom.attr('hidden', isBlocked);
        }
    }

    const toggleInsertParsysDebounced = $.debounce(100, toggleInsertParsys);

    $document.on('cq-layer-activated', function (ev) {
        if (ev.layer === 'Edit') {
            const action = author.edit.EditableActions[INSERT_ACTION];
            const originalCondition = action.condition;
            action.condition = function (editable) {
                try {
                    return !isChildrenLimitReached(editable) && originalCondition.apply(this, arguments);
                } catch (e) {
                    console.error('Error while checking children limit for editable: ', e);
                    return false;
                }
            };

            // set initial visibility state
            toggleInsertParsysDebounced();

            // track editables and overlays updates to hide insert parsys
            $document
                .off('cq-editables-updated.limited-parsys cq-overlays-repositioned.limited-parsys')
                .on('cq-editables-updated.limited-parsys cq-overlays-repositioned.limited-parsys', toggleInsertParsysDebounced);
        }
    });
}(Granite.$, Granite.author));
