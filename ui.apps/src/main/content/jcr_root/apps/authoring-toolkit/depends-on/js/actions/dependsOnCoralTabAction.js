/**
 * @author Yana Bernatskaya (YanaBr), Alexey Stsefanovich (ala'n)
 * @version 2.5.0
 *
 * DependsOn Coral 3 Tabs Actions
 * Additional action which sets visibility of tab-panel and related tab-control
 */

(function ($, ns) {
    'use strict';

    /**
     * Find related tab panelё
     * */
    function getTabPanel($element) {
        return $element.closest('coral-panelstack > coral-panel');
    }

    /**
     * Find related tab control
     * */
    function getTabControl($tabPanel) {
        return $tabPanel.closest('coral-tabview').find('coral-tablist > coral-tab').eq($tabPanel.index());
    }

    /**
     * Toggle visibility of every field on the tab
     */
    function tabChildrenVisibility($tabPanel, state) {
        $tabPanel.find('.coral-Form-field').each((index, el) => {
            ns.ElementAccessors.setVisibility($(el), state);
        });
    }

    /**
     * Change visibility of tab-panel and related tab-control
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
        tabChildrenVisibility(this.$tabPanel, state);
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
