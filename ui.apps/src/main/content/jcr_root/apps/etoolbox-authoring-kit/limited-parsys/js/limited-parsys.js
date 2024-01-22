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
(function (ns, utils, $, author) {
    'use strict';

    ns.LimitedParsys = ns.LimitedParsys || {};

    /** Utility builder for childConfig `resolvemaxlimit` listener to create a fixed limit resolver */
    ns.LimitedParsys.constant = (limit) => () => limit;

    /** The name of listener to resolve parsys children limit */
    ns.LimitedParsys.LIMIT_RESOLVER_NAME = 'resolvemaxlimit';

    /** The name of property to resolve parsys children limit */
    ns.LimitedParsys.LIMIT_RESOLVER_PROPERTY = 'eak-children-limit';

    /**
     * @param {Editable} editable
     * @returns {boolean} true if editable is a parsys
     */
    ns.LimitedParsys.isParsys = (editable) => editable && editable.type && editable.type.indexOf('parsys') !== -1;
    /**
     * @param {Editable} editable
     * @returns {boolean} true if editable is a 'newpar' parsys zone
     */
    ns.LimitedParsys.isParsysZone = (editable) => editable && editable.type && editable.type.endsWith('newpar');

    /**
     * @param editable
     * @returns {number} - children limit for the given editable
     */
    ns.LimitedParsys.getChildrenLimit = function getChildrenLimit(editable) {
        const limit = utils.executeListener(editable, ns.LimitedParsys.LIMIT_RESOLVER_NAME);
        if (typeof limit === 'number' || typeof limit === 'string') return +limit;
        return ns.LimitedParsys.resolveChildrenLimitFromPolicy(editable);
    };

    /**
     * @param editable
     * @returns {number} - children limit for the given editable
     */
    ns.LimitedParsys.resolveChildrenLimitFromPolicy = function resolveChildrenLimitFromPolicy(editable) {
        if (!ns.LimitedParsys.isParsys(editable)) return Number.POSITIVE_INFINITY;
        const limitCfg = utils.findPropertyFromConfig(editable, ns.LimitedParsys.LIMIT_RESOLVER_PROPERTY);
        return limitCfg === null ? Number.POSITIVE_INFINITY : +limitCfg;
    };

    /**
     * @param editable
     * @returns {number} current children count for the given editable
     */
    ns.LimitedParsys.getChildrenCount = function getChildrenCount(editable) {
        if (!editable.dom) return 0;
        const children = editable.dom.children(':not(cq, .par, .newpar, .iparys_inherited)');
        return children ? children.length : 0;
    };

    /**
     * Checks if editable contains equal or less children than defined in 'childrenLimit' property
     * @param editable
     * @returns {boolean} true if children limit is reached
     */
    ns.LimitedParsys.isChildrenLimitReached = function isChildrenLimitReached(editable) {
        const limit = ns.LimitedParsys.getChildrenLimit(editable);
        const size = ns.LimitedParsys.getChildrenCount(editable);
        return size >= limit;
    };

    /**
     * Show/hide all editables' insert parsys depending on {@link isChildrenLimitReached} function
     */
    ns.LimitedParsys.updateParsysZones = function updatetParsysZones() {
        const zones = author.editables.filter(ns.LimitedParsys.isParsysZone);
        for (const zone of zones) {
            const parsys = author.editables.getParent(zone);
            const isBlocked = ns.LimitedParsys.isChildrenLimitReached(parsys);
            zone.overlay && zone.overlay.setVisible(!isBlocked);
            zone.dom && zone.dom.attr('hidden', isBlocked);
        }
    };

    /** Debounced version of {@link updateParsysZones} */
    ns.LimitedParsys.updateParsysZonesDebounced = $.debounce(100, ns.LimitedParsys.updateParsysZones);

    const $document = $(document);
    $document.on('cq-layer-activated', function (ev) {
        if (ev.layer !== 'Edit') return;

        // decorate insert action condition
        const action = author.edit.EditableActions.INSERT;
        action.condition = utils.decorate(action.condition, function (originalCondition, ...args) {
            return !ns.LimitedParsys.isChildrenLimitReached(this) && originalCondition.apply(this, args);
        });

        // Initial call
        ns.LimitedParsys.updateParsysZonesDebounced();

        // track editables and overlays updates to hide insert parsys
        const UPDATE_EVENTS = 'cq-editables-updated.eak.limited-parsys cq-overlays-repositioned.eak.limited-parsys';
        $document.off(UPDATE_EVENTS).on(UPDATE_EVENTS, ns.LimitedParsys.updateParsysZonesDebounced);
    });
}(Granite, Granite.EAK, Granite.$, Granite.author));
