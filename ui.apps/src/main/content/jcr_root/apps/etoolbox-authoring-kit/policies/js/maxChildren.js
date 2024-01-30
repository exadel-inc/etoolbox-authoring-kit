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
 * Utility that disables insertion, drag/drop, and copy/paste actions for container component
 * if there are more child components inserted than the {@link MaxChildrenLimiter.LIMIT_RESOLVER_NAME} listener returns or
 * defined by {@link MaxChildrenLimiter.LIMIT_RESOLVER_PROPERTY} in policies or designs.
 */
(function (ns, $, author) {
    'use strict';

    ns.MaxChildrenLimiter = ns.MaxChildrenLimiter || {};

    /** The name of listener to resolve parsys children limit */
    ns.MaxChildrenLimiter.LIMIT_RESOLVER_NAME = 'resolvemaxchildren';

    /** The name of property to resolve parsys children limit */
    ns.MaxChildrenLimiter.LIMIT_RESOLVER_PROPERTY = 'eak-max-children';

    /** The default limit active message */
    ns.MaxChildrenLimiter.ERROR_MESSAGE = ns.I18n.get('Maximum number of allowed components is reached');

    /** The name of attribute to store limit message and marker */
    ns.MaxChildrenLimiter.ERROR_MESSAGE_ATTR = 'data-max-children-marker';

    /**
     * @param {Editable} editable
     * @returns {boolean} true if editable is a 'newpar' parsys zone
     */
    ns.MaxChildrenLimiter.isPlaceholder = (editable) => editable && editable.type && editable.type.endsWith('newpar');

    /**
     * Resolves max children limit for the given editable using {@link LIMIT_RESOLVER_NAME} listener.
     * If a listener does not exist or return a number, then {@link resolveChildrenLimitFromPolicy} is used.
     * @param editable
     * @returns {number} - max children for the given editable
     */
    ns.MaxChildrenLimiter.getChildrenLimit = function getChildrenLimit(editable) {
        const limit = ns.EAKUtils.executeListener(editable, ns.MaxChildrenLimiter.LIMIT_RESOLVER_NAME);
        if (typeof limit === 'number' || typeof limit === 'string') return +limit;
        return ns.MaxChildrenLimiter.resolveChildrenLimitFromPolicy(editable);
    };

    /**
     * Resolves children limit using desing or policy configuration for given editable
     * @param editable
     * @returns {number} - max children for the given editable
     */
    ns.MaxChildrenLimiter.resolveChildrenLimitFromPolicy = function resolveChildrenLimitFromPolicy(editable) {
        const limitCfg = ns.EAKPolicyUtils.findPropertyFromConfig(editable, ns.MaxChildrenLimiter.LIMIT_RESOLVER_PROPERTY);
        return isNaN(+limitCfg) ? Number.POSITIVE_INFINITY : +limitCfg;
    };

    /**
     * @param editable
     * @returns {number} current children count for the given editable
     */
    ns.MaxChildrenLimiter.getChildrenCount = function getChildrenCount(editable) {
        if (!editable.dom) return 0;
        return editable.dom.children(':not(cq, .par, .newpar, .iparys_inherited)').length;
    };

    /**
     * Checks if editable contains equal or fewer children than defined in 'childrenLimit' property
     * @param editable
     * @returns {boolean} true if children limit is reached
     */
    ns.MaxChildrenLimiter.isChildrenLimitReached = function isChildrenLimitReached(editable) {
        const limit = ns.MaxChildrenLimiter.getChildrenLimit(editable);
        const size = ns.MaxChildrenLimiter.getChildrenCount(editable);
        return size >= limit;
    };

    /**
     * Show/hide all editables' insert parsys depending on {@link isChildrenLimitReached} function
     */
    ns.MaxChildrenLimiter.updateParsysZones = function updatetParsysZones() {
        const placeholders = author.editables.filter(ns.MaxChildrenLimiter.isPlaceholder);
        for (const placeholder of placeholders) {
            const parsys = author.editables.getParent(placeholder);
            const isBlocked = ns.MaxChildrenLimiter.isChildrenLimitReached(parsys);
            placeholder.setDisabled(isBlocked);
            $(placeholder.overlay.dom || []).attr(
                ns.MaxChildrenLimiter.ERROR_MESSAGE_ATTR,
                isBlocked ? ns.MaxChildrenLimiter.ERROR_MESSAGE : null
            );
        }
    };

    /**
     * Checks if insert action is allowed for the given editable
     * @param editable
     * @returns {boolean} true if insert action is allowed
     */
    ns.MaxChildrenLimiter.isInsertionAllowed = function isInsertionAllowed(editable) {
        if (ns.MaxChildrenLimiter.isChildrenLimitReached(editable)) return false;
        const parent = author.editables.getParent(editable);
        return !ns.MaxChildrenLimiter.isChildrenLimitReached(parent);
    };

    /** Debounced version of {@link updateParsysZones} */
    ns.MaxChildrenLimiter.updateParsysZonesDebounced = $.debounce(100, ns.MaxChildrenLimiter.updateParsysZones);

    const $document = $(document);
    $document.on('cq-layer-activated', function (ev) {
        if (ev.layer !== 'Edit') return;

        // decorate insert action condition
        const action = author.edit.EditableActions.INSERT;
        action.condition = ns.EAKUtils.decorate(action.condition, function (originalCondition, ...args) {
            return ns.MaxChildrenLimiter.isInsertionAllowed(...args) && originalCondition.apply(this, args);
        });

        // Initial call
        ns.MaxChildrenLimiter.updateParsysZonesDebounced();

        // track editables and overlays updates to hide container placeholders
        const UPDATE_EVENTS = 'cq-editables-updated.eak.limited-parsys cq-overlays-repositioned.eak.limited-parsys';
        $document.off(UPDATE_EVENTS).on(UPDATE_EVENTS, ns.MaxChildrenLimiter.updateParsysZonesDebounced);
    });
}(Granite, Granite.$, Granite.author));
