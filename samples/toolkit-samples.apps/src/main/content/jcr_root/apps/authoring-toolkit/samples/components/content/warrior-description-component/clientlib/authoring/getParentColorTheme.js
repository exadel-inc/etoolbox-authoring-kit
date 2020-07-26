/**
 * Custom dependsOn action.
 * The action is intended for receiving color theme of the Warrior component
 * (that is the main container for other components in the samples module).
 */
(function (Granite, $, DependsOn) {

    'use strict';

    const COMPONENT_FORMAT = '.json';

    /**
     * Decide which scope of tags to use (true - dark theme, default - light theme)
     * @param data {*}
     * @returns {boolean}
     * @private
     */
    function _success(data) {
        if (!data) { return false; }
        return (data.colorTheme === 'true');
    }

    /**
     * Register getParentColorTheme custom action
     */
    DependsOn.ActionRegistry.register('getParentColorTheme', function namespaceFilter(parentPath) {
        // Receiving Warrior component structure
        let promise = $.get(Granite.HTTP.externalize(parentPath + COMPONENT_FORMAT));
        promise
            .then(_success, () => false)
            .then((isDark) => DependsOn.ElementAccessors.setValue(this.$el, isDark));
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);