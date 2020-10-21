/**
 * @author Liubou Masiuk
 * @version 2.2.4
 *
 * Accessor for widgets that do not have a surrounding wrapper element
 * */
(function ($, ns) {
    const NO_WRAPPER_FIELDS_SELECTOR =
        '.coral-Form-fieldset, input[type=hidden], .coral-Heading, .coral3-Alert, .coral3-Button';

    ns.ElementAccessors.registerAccessor({
        selector: NO_WRAPPER_FIELDS_SELECTOR,
        findWrapper: function () {
            return $([]);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
