package com.exadel.aem.toolkit.api.annotations.assets.dependson;

/**
 * Represents set of pre-defined action values for {@link DependsOn} and {@link DependsOnTab} annotations
 * For this to work properly, the {@code aem-authoring-toolkit-assets} package must be added to the AEM installation
 */
@SuppressWarnings("unused")
public class DependsOnActions {
    /**
     * Hides the element if the query result is 'falsy'
     */
    public static final String VISIBILITY = "visibility";
    /**
     * Hides the tab or element's parent tab if the query result is 'falsy'
     */
    public static final String TAB_VISIBILITY = "tab-visibility";
    /**
     * Sets the validation state of the field based on the query result
     */
    public static final String VALIDATE = "validate";
    /**
     * Sets the 'required' marker of the field based on the query result
     */
    public static final String REQUIRED = "required";
    /**
     * Sets the 'readonly' marker of the field based on the query result
     */
    public static final String READONLY = "readonly";
    /**
     * Sets the field's disabled state based on the query result
     */
    public static final String DISABLED = "disabled";
    /**
     * Sets the query result as field's value (undefined query result is skipped)
     */
    public static final String SET = "set";
    /**
     * Sets the query result as field's value only if the current value is blank (undefined query result is skipped)
     */
    public static final String SET_IF_BLANK = "set-if-blank";
    private DependsOnActions() {}
}
