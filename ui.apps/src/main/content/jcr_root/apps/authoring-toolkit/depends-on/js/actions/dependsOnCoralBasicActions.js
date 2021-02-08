/**
 * @author Alexey Stsefanovich (ala'n), Yana Bernatskaya (YanaBr)
 *
 * DependsOn Coral 3 Basic Actions
 *
 * Defined actions:
 * - visibility - set field visibility (also hide form field wrapper)
 * - required - set the require marker of the field
 * - readonly - set readonly state of field
 * - disabled - set disabled state of field
 * - set - set field value from query result
 * - set-if-blank - set field value from query result if field value is blank
 * */
(function ($, ns) {
    'use strict';

    /**
     * Change visibility of field and form field wrapper
     * query type: boolean
     * */
    ns.ActionRegistry.register('visibility', function setVisibility(state) {
        ns.ElementAccessors.setVisibility(this.$el, state);
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
     * Change readonly marker of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('readonly', function setReadonly(state) {
        this.$el.attr('readonly', state ? 'true' : null);
    });

    /**
     * Change disable marker of the field
     * query type: boolean
     * */
    ns.ActionRegistry.register('disabled', function setDisabled(state) {
        ns.ElementAccessors.setDisabled(this.$el, state);
    });

    /**
     * Set field value from query result, skips undefined query result
     * query type: string
     * */
    ns.ActionRegistry.register('set', function setValue(value) {
        if (value !== undefined) {
            ns.ElementAccessors.setValue(this.$el, value);
        }
    });

    /**
     * Set field value from query result if field value is blank, skips undefined query result
     * query type: string
     * */
    ns.ActionRegistry.register('set-if-blank', function setValueIfBlank(value) {
        const current = ns.ElementAccessors.getValue(this.$el);
        if ((current === '' || current === null || current === undefined) && value !== undefined) {
            ns.ElementAccessors.setValue(this.$el, value);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
