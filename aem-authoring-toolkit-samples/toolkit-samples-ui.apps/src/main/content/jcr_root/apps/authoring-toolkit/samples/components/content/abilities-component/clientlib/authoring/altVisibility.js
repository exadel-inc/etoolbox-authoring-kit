/*
Custom dependsOn action.
The action is intended for deleting hidden by dependsOn fields from content in JCR and not sending them with the form submitting.
 */

(function (Granite, $, DependsOn) {

    'use strict';

    /**
     * Remove custom input with a name field_name@Delete when a field need to be shown
     * @param element {HTMLElement}
     * @private
     */
    function _onShow(element) {
        const customInput = element.parentNode.querySelector('.alt-visibility-input');
        customInput.remove();
    }

    /**
     * Insert custom input with a name field_name@Delete when a field need to be hidden
     * @param element {HTMLElement}
     * @private
     */
    function _onHide(element) {
        const elementName = element.name;
        const customInput = document.createElement('input');

        customInput.classList.add('alt-visibility-input');
        customInput.name = `${elementName}@Delete`;
        customInput.type = 'hidden';

        element.parentNode.appendChild(customInput);
    }


    /**
     * Registry altVisibility custom action
     */
    DependsOn.ActionRegistry.register('alt-visibility', function altVisibility(state) {

        DependsOn.ElementAccessors.setVisibility(this.$el, state);
        DependsOn.ElementAccessors.setDisabled(this.$el, !state);
        const element = this.$el.context;

        if (element) {
            if (state) { _onShow(element); }
            else { _onHide(element); }
        }
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);