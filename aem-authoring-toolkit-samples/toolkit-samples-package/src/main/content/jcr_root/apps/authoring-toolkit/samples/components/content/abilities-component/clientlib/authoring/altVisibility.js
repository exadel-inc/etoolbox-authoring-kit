(function (Granite, $, DependsOn) {

    'use strict';

    function onShow(element) {
        const customInput = element.parentNode.querySelector('.alt-visibility-input');
        customInput.remove();
    }

    function onHide(element) {
        const elementName = element.name;
        const customInput = document.createElement('input');

        customInput.classList.add('alt-visibility-input');
        customInput.name = `${elementName}@Delete`;
        customInput.type = 'hidden';

        element.parentNode.appendChild(customInput);
    }


    DependsOn.ActionRegistry.register('alt-visibility', function altVisibility(state) {

        DependsOn.ElementAccessors.setVisibility(this.$el, state);
        DependsOn.ElementAccessors.setDisabled(this.$el, !state);
        const element = this.$el.context;

        if (element) {
            const targetInput = (element.tagName === 'INPUT')
                ? element
                : element.querySelector('input[data-dependson]');

            if (state) { onShow(element); }
            else { onHide(element); }
        }
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);