/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn Coral 3 Basic Actions
 *
 * Defined actions:
 * - visibility - set field visibility (also hide form field wrapper)
 * - tab-visibility - set visibility of tab-panel and related tab-control
 * - required - set the require marker of the field
 * - set - set field value from query result
 * - set-if-blank - set field value from query result if field value is blank
 * */
(function ($, ns) {
    'use strict';

    /**
     * Find related tab panel
     * */
    function getTabPanel($element) {
        return $element.closest('coral-panel[role="tabpanel"]');
    }

    /**
     * Find related tab control
     * */
    function getTabControl($tabPanel) {
        return $tabPanel.closest('coral-tabview').find('coral-tablist > coral-tab').eq($tabPanel.index());
    }

    /**
     * Change visibility of field and form field wrapper
     * query type: boolean
     * */
    ns.ActionRegistry.register('visibility', function setVisibility(state) {
        ns.ElementAccessors.setVisibility(this.$el, state);
    });


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
    });


    /**
     * Change require marker of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('required', function setRequired(state) {
        // Update label according to state
        ns.ElementAccessors.setLabelRequired(this.$el, state);
        ns.ElementAccessors.setRequired(this.$el, state);
    });

    /**
     * Change disable marker of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('disabled', function setDisabled(state) {
        ns.ElementAccessors.setDisabled(this.$el, state);
    });

    /**
     * Set field value from query result
     * query type: string
     * */
    ns.ActionRegistry.register('set', function setValue(value) {
        ns.ElementAccessors.setValue(this.$el, value);
        this.$el.trigger('change');
    });

    /**
     * Set field value from query result if field value is blank
     * query type: string
     * */
    ns.ActionRegistry.register('set-if-blank', function setValueIfBlank(value) {
        const current = ns.ElementAccessors.getValue(this.$el);
        if (current === '' || current === null || current === undefined) {
            ns.ElementAccessors.setValue(this.$el, value);
            this.$el.trigger('change');
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));