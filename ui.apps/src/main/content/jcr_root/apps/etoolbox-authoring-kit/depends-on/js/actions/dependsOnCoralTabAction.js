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
 * @author Yana Bernatskaya (YanaBr), Alexey Stsefanovich (ala'n)
 *
 * DependsOn Coral 3 Tab Visibility Action.
 *
 * An additional action that sets the visibility of a tab-panel and the related tab-control
 */

(function ($, ns) {
    'use strict';

    const FIELD_SELECTOR = '.coral-Form-field';

    /**
     * Find the related tab panel
     * */
    function getTabPanel($element) {
        return $element.closest('coral-panelstack > coral-panel');
    }

    /**
     * Find the related tab control
     * */
    function getTabControl($tabPanel) {
        return $tabPanel.closest('coral-tabview').find('coral-tablist > coral-tab').eq($tabPanel.index());
    }

    /**
     * Toggle the visibility of every field on the tab
     */
    function tabChildrenVisibility($tabPanel, state, locker) {
        $tabPanel.find(FIELD_SELECTOR).each((index, el) => {
            const $el = $(el);

            // To check it's not an inner auxiliary element
            const isInnerEl = !!$el.parent().closest(FIELD_SELECTOR, ns.ElementAccessors.findWrapper($el)).length;
            if (!$el.is('[data-dependson]') && isInnerEl) return;

            ns.ElementAccessors.setVisibility($el, state, locker);
        });
    }

    /**
     * Change the visibility of the tab-panel and the related tab-control
     * query type: boolean
     * */
    ns.ActionRegistry.register('tab-visibility', function setTabVisibility(state) {
        this.$tabPanel = this.$tabPanel || getTabPanel(this.$el);
        this.$tabControl = this.$tabControl || getTabControl(this.$tabPanel);

        this.$el.attr('hidden', state ? null : 'true'); // If current target is tab
        this.$tabPanel.attr('hidden', state ? null : 'true');
        this.$tabControl.attr('hidden', state ? null : 'true');

        const targetTab = this.$tabControl[0];
        if (targetTab && targetTab.selected && !state) {
            const tabs = targetTab.parentNode.items.getAll();
            tabs.find((tab) => !tab.hidden).selected = true;
            // Last tab is automatically deselected
        }
        tabChildrenVisibility(this.$tabPanel, state, this);
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
