package com.exadel.aem.toolkit.core.util;

import java.nio.file.Paths;

public class TestsConstants {
    private TestsConstants() {
    }
    public static final String PATH_TO_EXPECTED_FILES = "src\\test\\resources\\dialog";
    static final String TESTCASE_PACKAGE = "com.exadel.aem.toolkit";

    private static final String API_MODULE_NAME = "aem-authoring-toolkit-api";
    private static final String PLUGIN_MODULE_NAME = "aem-authoring-toolkit-plugin";

    public static final String PLUGIN_MODULE_TARGET = Paths.get("target", "classes").toAbsolutePath().toString();
    public static final String PLUGIN_MODULE_TEST_TARGET = Paths.get( "target", "test-classes").toAbsolutePath().toString();
    public static final String API_MODULE_TARGET = PLUGIN_MODULE_TARGET.replace(PLUGIN_MODULE_NAME, API_MODULE_NAME);

}
