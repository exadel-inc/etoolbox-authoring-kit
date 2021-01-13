/**
 * @author Yana Bernatskaya (YanaBr)
 * @version 2.5.0
 *
 * Tabs control utility.
 * */
(function (Granite, $, DependsOn) {
    'use strict';

    class TabsControl {

        /**
         * Find related tab panelё
         * */
        static getTabPanel($element) {
            return $element.closest('coral-panelstack > coral-panel');
        }

        /**
         * Find related tab control
         * */
        static getTabControl($tabPanel) {
            return $tabPanel.closest('coral-tabview').find('coral-tablist > coral-tab').eq($tabPanel.index());
        }

        /**
         * Toggle visibility of every field on the tab
         */
        static tabChildrenVisibility($tabPanel, state) {
            $tabPanel.find('.coral-Form-field').each((index, el) => {
                DependsOn.ElementAccessors.setVisibility($(el), state);
            });
        }
    }

    DependsOn.TabsControl = TabsControl;
})(Granite, Granite.$, Granite.DependsOnPlugin);
