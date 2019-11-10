package com.exadel.aem.toolkit.api.annotations.assets.dependson;

/**
 * Represents set of pre-defined action values for {@link DependsOn} and {@link DependsOnTab} annotations
 * For this to work properly, the {@code aem-authoring-toolkit-assets} package must be added to the AEM installation
 */
@SuppressWarnings("unused")
public class DependsOnActions {
    private DependsOnActions() {
    }
    public static final String VISIBILITY = "visibility";
    public static final String TAB_VISIBILITY = "tab-visibility";
    public static final String VALIDATE = "validate";
    public static final String REQUIRED = "required";
    public static final String SET = "set";
    public static final String SET_IF_BLANK = "set-if-blank";
}
